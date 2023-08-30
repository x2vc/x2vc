package org.x2vc.process.tasks;

import java.io.File;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * Factory to obtain instances of the following classes:
 * <ul>
 * <li>{@link InitializationTask}</li>
 * <li>{@link RequestProcessingTask}</li>
 * </ul>
 */
public interface ITaskFactory {

	/**
	 * Creates a new {@link InitializationTask}.
	 *
	 * @param xsltFile
	 * @param mode
	 * @return the task
	 */
	InitializationTask createInitializationTask(File xsltFile, ProcessingMode mode);

	/**
	 * Creates a new {@link RequestProcessingTask}
	 *
	 * @param request
	 * @param mode
	 * @return the task
	 */
	RequestProcessingTask createRequestProcessingTask(IDocumentRequest request, ProcessingMode mode);

}
