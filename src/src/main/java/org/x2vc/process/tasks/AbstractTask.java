package org.x2vc.process.tasks;

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

import java.io.File;
import java.util.UUID;

import org.apache.logging.log4j.CloseableThreadContext;

/**
 * Base class of task implementations with some common functions.
 */
public abstract class AbstractTask implements ITask {

	private UUID taskID = UUID.randomUUID();
	private File xsltFile;

	protected AbstractTask(File xsltFile) {
		super();
		this.xsltFile = xsltFile;
	}

	@Override
	public UUID getTaskID() {
		return this.taskID;
	}

	/**
	 * @return the stylesheet file to be processed
	 */
	protected File getXSLTFile() {
		return this.xsltFile;
	}

	/**
	 * Prepares a {@link CloseableThreadContext} to annotate the log entries with the stylesheet name.
	 *
	 * @return
	 */
	protected CloseableThreadContext.Instance getThreadContext() {
		return CloseableThreadContext.put("stylesheet", this.xsltFile.toString());
	}

	@Override
	public final void run() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			execute();
		}
	}

	/**
	 * Executes the actual function of the task.
	 */
	protected abstract void execute();

}
