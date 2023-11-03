package org.x2vc.schema.structure;

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

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.QName;

/**
 * An extension function that has to be simulated in order for the stylesheet to be processed correctly.
 *
 * @see <a href="https://www.saxonica.com/documentation10/index.html#!extensibility/integratedfunctions">Saxon
 *      documentation</a>
 */
public interface IExtensionFunction extends ISchemaObject {

	/**
	 * @return the URI of the namespace for which the extension function is defined.
	 */
	Optional<String> getNamespaceURI();

	/**
	 * @return the function name within the namespace
	 */
	String getLocalName();

	/**
	 * @return the qualified name, consisting of the {@link #getNamespaceURI()} and the {@link #getLocalName()}
	 *
	 */
	QName getQualifiedName();

	/**
	 * @return the return / result type of the function
	 */
	IFunctionSignatureType getResultType();

	/**
	 * @return the types of the function arguments
	 */
	ImmutableList<IFunctionSignatureType> getArgumentTypes();

}
