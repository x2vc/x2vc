package org.x2vc.process;

/**
 * This component controls the main worker thread pool, reports on its usage
 * periodically, ensures that it is properly terminated when the JVM is shut
 * down and provides a method to wait for the completion of all tasks.
 */
public interface IWorkerProcessManager {

	/**
	 * Adds a task to the task queue.
	 *
	 * @param task
	 */
	void submit(Runnable task);

	/**
	 * Blocks until all tasks have completed execution.
	 *
	 * @throws InterruptedException
	 */
	void awaitCompletion() throws InterruptedException;

	/**
	 * Blocks until all tasks have completed execution and shuts the queue down so
	 * that no more tasks are accepted.
	 *
	 * @throws InterruptedException
	 */
	void shutdown() throws InterruptedException;

}
