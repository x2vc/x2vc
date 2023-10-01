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
