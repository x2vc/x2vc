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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * Base type of an object that describes a modification to an XML schema.
 */
public interface ISchemaModifier {

	/**
	 * @return the URI of the schema to be modified
	 */
	URI getSchemaURI();

	/**
	 * @return the version of the schema the modification refers to
	 */
	int getSchemaVersion();

	/**
	 * @return the ID of the schema element being modified, or an empty value if the document is being modified
	 */
	Optional<UUID> getElementID();

	/**
	 * @param otherModifier
	 * @return <code>true</code> if the two modifiers are equal except for the newly generated schema object IDs and
	 *         dependencies
	 */
	boolean equalsIgnoringIDs(ISchemaModifier otherModifier);

	/**
	 * @return a hashCode for the modifier
	 */
	int hashCodeIgnoringIDs();

	/**
	 * @return the total number of modifiers including this modifier and all sub-modifiers
	 */
	int count();

}
