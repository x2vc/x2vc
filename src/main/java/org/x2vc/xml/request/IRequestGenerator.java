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
package org.x2vc.xml.request;


import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;

/**
 * Generates new {@link IDocumentRequest} objects by randomly exploring a schema or adjusting a previous request
 * according to modifier specifications.
 */
public interface IRequestGenerator {

	/**
	 * Generates a new request by randomly exploring the schema.
	 *
	 * @param schema                     the schema to use
	 * @param mixedContentGenerationMode
	 * @return the new request
	 */
	IDocumentRequest generateNewRequest(IXMLSchema schema, MixedContentGenerationMode mixedContentGenerationMode);

	/**
	 * Generates a new request by applying a modifier to another request.
	 *
	 * @param originalRequest            the request to modify
	 * @param modifier                   the modifier to apply
	 * @param mixedContentGenerationMode
	 * @return the modified request
	 */
	IDocumentRequest modifyRequest(IDocumentRequest originalRequest, IDocumentModifier modifier,
			MixedContentGenerationMode mixedContentGenerationMode);

}
