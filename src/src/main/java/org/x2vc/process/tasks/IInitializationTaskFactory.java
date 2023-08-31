package org.x2vc.process.tasks;

import java.io.File;

/**
 * Factory to obtain instances of {@link IInitializationTask}.
 */
public interface IInitializationTaskFactory {

	/**
	 * Creates a new {@link InitializationTask}.
	 *
	 * @param xsltFile
	 * @param mode
	 * @return the task
	 */
	IInitializationTask create(File xsltFile, ProcessingMode mode);

}
