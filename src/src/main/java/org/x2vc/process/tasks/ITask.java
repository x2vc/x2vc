package org.x2vc.process.tasks;

import java.util.UUID;

/**
 * Common functions of all tasks.
 */
public interface ITask extends Runnable {

	/**
	 * @return the task ID
	 */
	UUID getTaskID();

}
