package org.x2vc.process.tasks;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * Factory to obtain instances of {@link IRequestProcessingTask}.
 */
public interface IRequestProcessingTaskFactory {

	/**
	 * Creates a new {@link IRequestProcessingTask}
	 *
	 * @param request
	 * @param mode
	 * @return the task
	 */
	IRequestProcessingTask create(IDocumentRequest request, ProcessingMode mode);

}
