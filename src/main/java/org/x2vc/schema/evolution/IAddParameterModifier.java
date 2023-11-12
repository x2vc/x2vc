package org.x2vc.schema.evolution;

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
import java.util.UUID;

import org.x2vc.schema.structure.IFunctionSignatureType;

import net.sf.saxon.s9api.QName;

/**
 * An {@link ISchemaModifier} to add a new stylesheet parameter to the schema.
 */
public interface IAddParameterModifier extends ISchemaModifier {

	/**
	 * @return the ID of the parameter
	 */
	UUID getParameterID();

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
	 * @return the comment of the parameter
	 */
	Optional<String> getComment();

	/**
	 * @return the type of the parameter
	 */
	IFunctionSignatureType getType();

}
