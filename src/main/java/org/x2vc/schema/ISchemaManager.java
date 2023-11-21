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
package org.x2vc.schema;


import java.net.URI;
import java.util.Collection;

import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.structure.IXMLSchema;

/**
 * This component handles the XML schema objects. It is able to provide the current (most recent) version of the schema.
 * It can apply schema change operations to the current schema to generate a new version. It also keeps the evolution
 * log current.
 *
 * The schema manager is responsible for storing the schema whenever a new version is created. Only the most recent
 * version of a schema is saved since the version history only exists at runtime.
 *
 * It is possible to apply a set of schema change operations to an older version of the schema to bring it to the
 * current version. Applying the same set of operations to the same basic schema always yields the same target schema
 * and same evolution log.
 *
 */
public interface ISchemaManager {

	/**
	 * Determines whether the schema associated with a style sheet exists or has to be generated.
	 *
	 * @param stylesheetURI the URI of the stylesheet
	 * @return <code>true</code> if the corresponding schema exists.
	 */
	boolean schemaExists(URI stylesheetURI);

	/**
	 * Returns the most recent version of the schema associated with a stylesheet.
	 *
	 * @param stylesheetURI the URI of the stylesheet
	 * @return the schema corresponding to the stylesheet
	 */
	IXMLSchema getSchema(URI stylesheetURI);

	/**
	 * Determines the URI of the given version of the schema associated with a stylesheet.
	 *
	 * @param stylesheetURI the URI of the stylesheet
	 * @param schemaVersion
	 * @return the schema corresponding to the stylesheet
	 * @throws IllegalStateException if that particular version was not found
	 */
	IXMLSchema getSchema(URI stylesheetURI, int schemaVersion) throws IllegalStateException;

	/**
	 * Applies the modifiers to the input schema, generates a new schema, stores it in memory and updates the file
	 * version.
	 *
	 * @param inputSchema
	 * @param modifiers
	 * @return the next version of the schema
	 */
	IXMLSchema modifySchema(IXMLSchema inputSchema, Collection<ISchemaModifier> modifiers);

}
