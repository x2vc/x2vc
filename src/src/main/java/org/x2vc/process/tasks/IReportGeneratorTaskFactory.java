package org.x2vc.process.tasks;

import java.io.File;

/**
 * Factory to obtain instances of {@link IReportGeneratorTask}.
 */
public interface IReportGeneratorTaskFactory {

	/**
	 * Creates a new {@link IReportGeneratorTask}
	 *
	 * @param xsltFile
	 *
	 * @return the task
	 */
	IReportGeneratorTask create(File xsltFile);

}
