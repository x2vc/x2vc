package org.x2vc.schema;

import java.net.URI;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;

/**
 * Generates an initial version of a schema for a stylesheet. Used by the
 * {@link ISchemaManager} if no schema exists.
 */
public interface IInitialSchemaGenerator {

	/**
	 * Generates an initial version of a schema for the stylesheet.
	 *
	 * @param stylesheet
	 * @param schemaURI
	 * @return a newly generated stylesheet
	 */
	IXMLSchema generateSchema(IStylesheetInformation stylesheet, URI schemaURI);

}
