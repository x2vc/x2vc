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

import net.sf.saxon.s9api.QName;

/**
 * This object is used to represent both formal parameters (xsl:param) and actual parameters (xsl:with-param).
 */
public interface IXSLTParameterNode extends IElementNode {

	/**
	 * @return the URI of the namespace for which the parameter name is defined.
	 */
	Optional<String> getNamespaceURI();

	/**
	 * @return the parameter name within the namespace
	 */
	String getLocalName();

	/**
	 * @return the qualified name, consisting of the {@link #getNamespaceURI()} and the {@link #getLocalName()}
	 *
	 */
	QName getQualifiedName();

	/**
	 * @return the value of the select attribute if it exists
	 */
	Optional<String> getSelection();

}
