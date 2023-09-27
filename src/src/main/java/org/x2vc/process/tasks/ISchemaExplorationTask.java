package org.x2vc.process.tasks;

import java.util.UUID;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * This task is used to process a single {@link IDocumentRequest} and collect the relevant results for schema evolution.
 */
public interface ISchemaExplorationTask extends Runnable {

	/**
	 * @return the task ID
	 */
	UUID getTaskID();

}