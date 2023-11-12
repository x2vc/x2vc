package org.x2vc.process.commands;

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

/**
 * This component controls the overall schema evolution and vulnerability check process for a single stylesheet.
 */
public interface IProcessDirector {

	/**
	 * Starts the initialization phase of the processing.
	 */
	void initialize();

	/**
	 * @return the processing state of the file in question
	 */
	ProcessState getProcessState();

}
