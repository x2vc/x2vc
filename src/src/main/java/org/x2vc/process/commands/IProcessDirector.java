package org.x2vc.process.commands;

import java.io.File;

import org.x2vc.process.tasks.ProcessingMode;

/**
 * This component controls the overall schema evolution and vulnerability check process.
 */
public interface IProcessDirector {

	/**
	 * The state of processing of a single stylesheet.
	 */
	enum ProcessState {
		/**
		 * The process has just been created and not started yet.
		 */
		NEW,

		/**
		 * The processing is being initialized. Among other things, the schema for the stylesheet is being loaded or
		 * generated.
		 */
		INITIALIZE,

		/**
		 * Sample documents are being processed in order to determine whether the stylesheet attempts to access document
		 * elements that are not yet represented in the schema.
		 */
		EXPLORE_SCHEMA,

		/**
		 * The results of the schema exploration phase are being consolidated and the schema is being adjusted.
		 */
		EVOLVE_SCHEMA,

		/**
		 * The XSS vulnerability check is being performed (both initial and follow-up pass).
		 */
		CHECK_XSS,

		/**
		 * The report for the stylesheet is being compiled,
		 */
		COMPILE_REPORT,

		/**
		 * The processing has been completed.
		 */
		DONE
	}

	/**
	 * Adds a stylesheet to the set of stylesheets being processed. If the file is already being processed (or the
	 * processing has already completed), this method does nothing.
	 *
	 * @param xsltFile
	 * @param mode
	 */
	void startProcess(File xsltFile, ProcessingMode mode);

	/**
	 * @param xsltFile
	 * @return the processing state of the file in question
	 * @throws IllegalArgumentException if the file has not been submitted for processing
	 */
	ProcessState getProcessState(File xsltFile) throws IllegalArgumentException;

	/**
	 * @return <code>true</code> if all of the files have been processed
	 */
	boolean isCompleted();

	/**
	 * Blocks until all processes have completed.
	 *
	 * @throws InterruptedException
	 */
	void awaitCompletion() throws InterruptedException;

}
