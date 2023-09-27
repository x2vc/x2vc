package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

/**
 * Factory to obtain instances of {@link IReportGeneratorTask}.
 */
public interface IReportGeneratorTaskFactory {

	/**
	 * Creates a new {@link IReportGeneratorTask}
	 *
	 * @param xsltFile
	 * @param callback
	 *
	 * @return the task
	 */
	IReportGeneratorTask create(File xsltFile, Consumer<Boolean> callback);

}
