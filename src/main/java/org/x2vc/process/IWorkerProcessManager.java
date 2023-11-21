/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.process;


import org.x2vc.process.tasks.ITask;

/**
 * This component controls the main worker thread pool, reports on its usage periodically, ensures that it is properly
 * terminated when the JVM is shut down and provides a method to wait for the completion of all tasks.
 */
public interface IWorkerProcessManager {

	/**
	 * Prepares the worker thread pool. May be used to keep the log tidy, but will automatically be called upon
	 * submission of the first task if forgotten.
	 */
	void initialize();

	/**
	 * Adds a task to the task queue.
	 *
	 * @param task
	 */
	void submit(ITask task);

	/**
	 * Blocks until all tasks have completed execution.
	 *
	 * @throws InterruptedException
	 */
	void awaitCompletion() throws InterruptedException;

	/**
	 * Blocks until all tasks have completed execution and shuts the queue down so that no more tasks are accepted.
	 *
	 * @throws InterruptedException
	 */
	void shutdown() throws InterruptedException;

}
