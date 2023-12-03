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

import java.util.List;

import com.google.inject.assistedinject.Assisted;

/**
 * Factory to obtain instances of {@link ITagMap}.
 */
public interface ITagMapFactory {

	/**
	 * Creates a new {@link ITagMap} instance with tags provided
	 *
	 * @param tags the tags to be contained in the map
	 *
	 * @return the tag map
	 */
	ITagMap create(@Assisted List<ITagInfo> tags);

}
