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
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.LoggingMixin;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;

import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

/**
 * Base class of processing commands.
 */
public abstract class AbstractProcessCommand implements Callable<Integer> {

	private static final Logger logger = LogManager.getLogger();

	@Mixin
	LoggingMixin loggingMixin;

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
		if (!checkFiles()) {
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

		// TODO Logging: produce statistics output here

		return logger.traceExit(0);

	}

	protected abstract ProcessingMode getProcessingMode();

	/**
	 * Checks whether the files specified on the command line are readable.
	 */
	private boolean checkFiles() {
		int erroneousFiles = 0;
		for (final File file : this.xsltFiles) {
			if (!file.canRead()) {
				logger.error("Unable to read file {}", file);
				erroneousFiles++;
			}
		}
		if (erroneousFiles > 0) {
			logger.fatal("{} of the files specified are inaccessible, aborting", erroneousFiles);
			return false;
		} else {
			return true;
		}
	}
}
