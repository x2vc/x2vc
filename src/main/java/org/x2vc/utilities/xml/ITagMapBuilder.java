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
 * A factory that creates the {@link ITagMap} instances
 */
@ImplementedBy(TagMapBuilder.class)
public interface ITagMapBuilder {

	/**
	 * Creates an {@link ITagMap} instance based on an input file.
	 *
	 * @param xmlSource   the XML source
	 * @param locationMap the {@link ILocationMapFactory} used to create the {@link PolymorphLocation}s
	 * @return the tag map
	 * @throws IllegalArgumentException
	 */
	ITagMap buildTagMap(String xmlSource, ILocationMap locationMap) throws IllegalArgumentException;

}
