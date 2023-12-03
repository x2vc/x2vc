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
package org.x2vc.utilities.xml;

import com.google.inject.ImplementedBy;

/**
 * A factory that creates the {@link ILocationMap} instances
 */
@ImplementedBy(LocationMapBuilder.class)
public interface ILocationMapBuilder {

	/**
	 * Creates an {@link ILocationMap} instance based on an input file.
	 *
	 * @param xmlSource the XML source
	 * @return the location map
	 * @throws IllegalArgumentException
	 */
	ILocationMap buildLocationMap(String xmlSource) throws IllegalArgumentException;

}
