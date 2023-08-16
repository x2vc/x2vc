package org.x2vc.xml.value;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * Factory to obtain {@link IValueGenerator} instances.
 */
public interface IValueGeneratorFactory {

	/**
	 * Creates a new value generator for the request provided
	 *
	 * @param request
	 * @return the value generator
	 */
	IValueGenerator createValueGenerator(IDocumentRequest request);

}
