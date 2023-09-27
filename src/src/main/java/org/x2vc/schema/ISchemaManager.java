package org.x2vc.schema;

import java.net.URI;

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

	// TODO XML Schema Manager: Add schema evolution methods

}
