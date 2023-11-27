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
package org.x2vc.stylesheet;

import java.net.URI;

import com.google.inject.ImplementedBy;

/**
 * The stylesheet manager is responsible for loading the stylesheets from file into memory and providing access to the
 * prepared stylesheet instances.
 *
 * The stylesheet manager can handle both file-based URIs as well as transient stylesheets kept in memory, e.g. for unit
 * testing. Transient stylesheets can be inserted using {@link #insert(String)} and are assigned a unique temporary URI.
 */
@ImplementedBy(StylesheetManager.class)
public interface IStylesheetManager {

	/**
	 * Provides a stylesheet instance. If needed, the stylesheet is loaded from file if a file-based
	 *
	 * @param uri the URI of the stylesheet - either a file location or a temporary URI generated using
	 *            {@link #insert(String)}
	 * @return the stylesheet information
	 */
	IStylesheetInformation get(URI uri);

	/**
	 * Inserts a temporary stylesheet into the stylesheet cache.
	 *
	 * @param source the stylesheet source
	 * @return the URI of the temporary stylesheet, used to retrieve it using {@link #get(URI)}
	 */
	URI insert(String source);

}
