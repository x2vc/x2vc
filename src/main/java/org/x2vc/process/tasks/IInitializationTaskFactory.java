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
import java.util.function.Consumer;

/**
 * Factory to obtain instances of {@link IInitializationTask}.
 */
public interface IInitializationTaskFactory {

	/**
	 * Creates a new {@link InitializationTask}.
	 *
	 * @param xsltFile the stylesheet file to prepare
	 * @param mode     the {@link ProcessingMode}
	 * @param callback the instance to be notified when the initialization is performed. Will be called with
	 *                 <code>true</code> if the initialization was successful or <code>false</code> if the processing
	 *                 cannot be continued.
	 * @return the task
	 */
	IInitializationTask create(File xsltFile, ProcessingMode mode, Consumer<Boolean> callback);

}
