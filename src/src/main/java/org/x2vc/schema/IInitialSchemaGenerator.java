package org.x2vc.schema;

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
	 * @return a newly generated stylesheet
	 */
	IXMLSchema generateSchema(IStylesheetInformation stylesheet);

}
