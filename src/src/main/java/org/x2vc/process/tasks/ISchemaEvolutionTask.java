package org.x2vc.process.tasks;

import java.util.UUID;

/**
 * This task is used to consolidate the schema modifiers generated during the exploration phase and produce a new schema
 * version to be tested.
 */
public interface ISchemaEvolutionTask extends Runnable {

	/**
	 * @return the task ID
	 */
	UUID getTaskID();

}
