package org.x2vc.process.commands;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Base class of processing commands.
 */
public abstract class AbstractProcessCommand implements Callable<Integer> {

	private static final Logger logger = LogManager.getLogger();

	@Option(names = "-D", mapFallbackValue = "", description = "Set system property.")
	private Map<String, String> ignoredValues1; // handled by CommonOptions

	@Option(names = { "-v", "--verbose" }, description = { "Specify multiple -v options to increase verbosity.",
			"For example, `-v -v` or `-vv`" })
	private boolean[] ignoredValue2; // handled by CommonOptions

	@Option(names = "--logConfig", description = "Specify an alternate Log4j2 configuration file to use.")
	private File ignoredValue3; // handled by CommonOptions

	@Parameters(description = "XSLT files to check", arity = "1..*")
	private List<File> xsltFiles;

	private IProcessDirectorManager processDirector;
	private IWorkerProcessManager workerProcessManager;

	@Inject
	AbstractProcessCommand(IProcessDirectorManager processDirector, IWorkerProcessManager workerProcessManager) {
		this.processDirector = processDirector;
		this.workerProcessManager = workerProcessManager;
	}

	@Override
	public Integer call() throws Exception {
		logger.traceEntry();

		// ensure that all files are accessible
		if (!resolveAndCheckFiles()) {
			return logger.traceExit(1);
		}

		final long startTime = System.nanoTime();

		// pre-initialize thread pool and watcher (keeps logs organized)
		this.workerProcessManager.initialize();

		// hand off to the process director
		for (final File file : this.xsltFiles) {
			this.processDirector.startProcess(file, getProcessingMode());
		}

		this.processDirector.awaitCompletion();

		this.workerProcessManager.shutdown();

		final long endTime = System.nanoTime();
		final long totalTime = endTime - startTime;

		logger.info("Processing completed in {} seconds", String.format("%.2f", (totalTime / 1000000000.0)));

		// TODO #38 Logging: produce statistics output here

		return logger.traceExit(0);

	}

	protected abstract ProcessingMode getProcessingMode();

	/**
	 * Resolves wildcards in file names and checks whether the files specified on the command line are readable.
	 */
	private boolean resolveAndCheckFiles() {
		logger.traceEntry();
		int erroneousFiles = 0;

		// Resolves any wildcards left over in the file names specified in @{@link #xsltFiles}.
		// The reason for this is that wildcard expansion is only performed on Windows (see #3).

		final List<File> resolvedFiles = Lists.newArrayList();
		for (final File originalFile : this.xsltFiles) {
			if ((originalFile.getPath().contains("*")) || (originalFile.getPath().contains("?"))) {
				Path path = Paths.get("");
				if (originalFile.getParentFile() != null) {
					path = originalFile.getParentFile().toPath();
				}
				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, originalFile.getName())) {
					dirStream.forEach(p -> {
						logger.debug("resolving wildcard {} to filename {}", originalFile.getPath(), p.toFile());
						resolvedFiles.add(p.toFile());
					});
				} catch (final IOException e) {
					logger.error("Unable to resolve file specification: {}", originalFile.getPath(), e);
					erroneousFiles++;
				}
			} else {
				resolvedFiles.add(originalFile);
			}
		}

		for (final File file : resolvedFiles) {
			if (!file.canRead()) {
				logger.error("Unable to read file {}", file);
				erroneousFiles++;
			}
		}

		if (erroneousFiles > 0) {
			logger.fatal("{} of the files specified are inaccessible, aborting", erroneousFiles);
			return logger.traceExit(false);
		} else {
			this.xsltFiles = resolvedFiles;
			return logger.traceExit(true);
		}
	}
}
