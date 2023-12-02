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

import java.util.Optional;

/**
 * This object contains tag information for a single XML file. It is able to determine the boundaries and types of tags
 * within the file and locate the corresponding start and end tags.
 */
public interface ITagMap {

	/**
	 * @param location
	 * @return an object describing the tag at the location specified, or nothing if the location is not covered by a
	 *         tag
	 */
	Optional<ITagInfo> getTag(PolymorphLocation location);

}
