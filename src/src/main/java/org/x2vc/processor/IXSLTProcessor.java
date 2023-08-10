package org.x2vc.processor;

import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * A wrapper that encapsulates the XSLT processor and handles the input and
 * output processes as well as the message collection.
 */
public interface IXSLTProcessor {

	/**
	 * Process an XML document and attempt to produce a HTML document.
	 *
	 * @param xmlDocument the input document (also contains the schema and
	 *                    stylesheet reference)
	 * @return the resulting container with the HTML document or error information
	 */
	public IHTMLDocumentContainer processDocument(IXMLDocumentContainer xmlDocument);

}
