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

/**
 * Factory to obtain {@link IProcessDirector} instances.
 */
public interface IProcessDirectorFactory {

	/**
	 * Creates a new process director
	 *
	 * @param xsltFile
	 * @param mode
	 * @return the new process director
	 */
	IProcessDirector create(File xsltFile, ProcessingMode mode);

}
