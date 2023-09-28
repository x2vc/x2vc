package org.x2vc.process.commands;

import java.io.File;

import org.x2vc.process.tasks.ProcessingMode;

/**
 * Factory to obtain {@link IProcessDirector} instances.
 */
public interface IProcessDirectorFactory {

	/**
	 * Creates a new process director
	 *
	 * @param xsltFile
	 * @param mode
	 * @return the new process director
	 */
	IProcessDirector create(File xsltFile, ProcessingMode mode);

}
