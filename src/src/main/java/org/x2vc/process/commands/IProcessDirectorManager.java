package org.x2vc.process.commands;

import java.io.File;

import org.x2vc.process.tasks.ProcessingMode;

/**
 * This component controls the overall schema evolution and vulnerability check process by coordinating multiple
 * {@link IProcessDirector} instances.
 */
public interface IProcessDirectorManager {

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
