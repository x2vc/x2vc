package org.x2vc.process.commands;

/**
 * This component controls the overall schema evolution and vulnerability check process for a single stylesheet.
 */
public interface IProcessDirector {

	/**
	 * @return the processing state of the file in question
	 */
	ProcessState getProcessState();

}
