package org.x2vc.schema;

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
