package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

/**
 * Factory to obtain instances of {@link IInitializationTask}.
 */
public interface IInitializationTaskFactory {

	/**
	 * Creates a new {@link InitializationTask}.
	 *
	 * @param xsltFile the stylesheet file to prepare
	 * @param mode     the {@link ProcessingMode}
	 * @param callback the instance to be notified when the initialization is performed. Will be called with
	 *                 <code>true</code> if the initialization was successful or <code>false</code> if the processing
	 *                 cannot be continued.
	 * @return the task
	 */
	IInitializationTask create(File xsltFile, ProcessingMode mode, Consumer<Boolean> callback);

}
