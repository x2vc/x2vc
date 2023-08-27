package org.x2vc.schema;

import java.net.URI;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;

/**
 * Standard implementation of {@link IInitialSchemaGenerator}.
 */
public class InitialSchemaGenerator implements IInitialSchemaGenerator {

	@Override
	public IXMLSchema generateSchema(IStylesheetInformation stylesheet, URI schemaURI) {
		// TODO XML Schema: implement InitialSchemaGenerator
		throw new UnsupportedOperationException("initial stylesheet generation not yet implemented");
	}

}
