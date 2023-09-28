package org.x2vc.process.commands;

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

		// hand off to the process director
		for (final File file : this.xsltFiles) {
			this.processDirector.startProcess(file, getProcessingMode());
		}

		this.processDirector.awaitCompletion();

		this.workerProcessManager.shutdown();

		logger.info("processing completed");
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
