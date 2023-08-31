package org.x2vc.process.tasks;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * This task is used to process a single {@link IDocumentRequest} and follow up
 * on the results depending on the {@link ProcessingMode}.
 */
public interface IRequestProcessingTask extends Runnable {

}
