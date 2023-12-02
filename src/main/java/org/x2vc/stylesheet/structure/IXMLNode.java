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
package org.x2vc.stylesheet.structure;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.collect.ImmutableMap;

/**
 * A non-XSLT XML element contained in an {@link IStylesheetStructure} tree.
 */
public interface IXMLNode extends IElementNode {

	/**
	 * @return the qualified name of the non-XSLT XML element
	 */
	QName getName();

	/**
	 * @return the location the starting element was found
	 */
	Optional<PolymorphLocation> getStartLocation();

	/**
	 * @return the location the closing element was found
	 */
	Optional<PolymorphLocation> getEndLocation();

	/**
	 * @return the attributes of the non-XSLT XML element
	 */
	ImmutableMap<QName, String> getAttributes();

}
