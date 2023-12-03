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
package org.x2vc.process.commands;

import java.io.File;

import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.ImplementedBy;

/**
 * This component controls the overall schema evolution and vulnerability check process by coordinating multiple
 * {@link IProcessDirector} instances.
 */
@ImplementedBy(ProcessDirectorManager.class)
public interface IProcessDirectorManager {

	/**
	 * Adds a stylesheet to the set of stylesheets being processed. If the file is already being processed (or the
	 * processing has already completed), this method does nothing.
	 *
	 * @param xsltFile
	 * @param mode
	 */
	void startProcess(File xsltFile, ProcessingMode mode);

	/**
	 * @param xsltFile
	 * @return the processing state of the file in question
	 * @throws IllegalArgumentException if the file has not been submitted for processing
	 */
	ProcessState getProcessState(File xsltFile) throws IllegalArgumentException;

	/**
	 * @return <code>true</code> if all of the files have been processed
	 */
	boolean isCompleted();

	/**
	 * Blocks until all processes have completed.
	 *
	 * @throws InterruptedException
	 */
	void awaitCompletion() throws InterruptedException;

}
