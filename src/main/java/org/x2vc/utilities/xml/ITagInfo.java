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
 * Information about an XML tag that is provided by the {@link ITagMap}.
 */
public interface ITagInfo {

	/**
	 * The type of the tag.
	 */
	enum TagType {
		START,
		END,
		EMPTY
	}

	/**
	 * A pair of start and corresponding end tag.
	 *
	 * @param start the start tag
	 * @param end   the end tag
	 */
	record Pair(ITagInfo start, ITagInfo end) {
	}

	/**
	 * Returns the starting location of the tag. The offset points to the opening angular bracket.
	 *
	 * @return the starting location of the tag
	 */
	PolymorphLocation getStartLocation();

	/**
	 * Returns the ending location of the tag. The offset points to the first character after the closing angular
	 * bracket.
	 *
	 * @return the ending location of the tag
	 */
	PolymorphLocation getEndLocation();

	/**
	 * @return the tag type (start, end or empty-element)
	 */
	TagType getType();

	/**
	 * @return <code>true</code> if the tag is an empty-element tag, also known as a self-closing tag
	 */
	boolean isEmptyElement();

	/**
	 * @return <code>true</code> if the tag is a start tag
	 */
	boolean isStartTag();

	/**
	 * @return <code>true</code> if the tag is an end tag
	 */
	boolean isEndTag();

	/**
	 * @return the corresponding start tag for an end tag, or nothing for a start tag or an empty-element tag
	 */
	Optional<ITagInfo> getStartTag();

	/**
	 * @return the corresponding end tag for a start tag, or nothing for an end tag or an empty-element tag
	 */
	Optional<ITagInfo> getEndTag();

}
