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
package org.x2vc.process.tasks;


import java.io.File;
import java.util.function.Consumer;

/**
 * Factory to obtain instances of {@link IReportGeneratorTask}.
 */
public interface IReportGeneratorTaskFactory {

	/**
	 * Creates a new {@link IReportGeneratorTask}
	 *
	 * @param xsltFile
	 * @param callback
	 *
	 * @return the task
	 */
	IReportGeneratorTask create(File xsltFile, Consumer<Boolean> callback);

}
