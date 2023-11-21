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
package org.x2vc.schema.structure;


import java.util.Optional;

import net.sf.saxon.s9api.QName;

/**
 * A parameter defined by a top-level <code>xsl:param</code> instruction.
 *
 * @see <a href="https://www.saxonica.com/documentation10/index.html#!extensibility/integratedfunctions">Saxon
 *      documentation</a>
 */
public interface IStylesheetParameter extends ISchemaObject {

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
	 * @return the data type parameter
	 */
	IFunctionSignatureType getType();

}
