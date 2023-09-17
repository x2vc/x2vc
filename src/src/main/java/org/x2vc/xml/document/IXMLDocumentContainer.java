package org.x2vc.xml.document;

import java.net.URI;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * A container object that is used to transport a generated XML document, its
 * corresponding descriptor and a reference to the request used to generate the
 * document. The container also references the XML schema version used to
 * generate the document.
 */
public interface IXMLDocumentContainer {

	/**
	 * @return the URI of the schema that was used to generate this document
	 */
	URI getSchemaURI();

	/**
	 * @return the version of the schema that was used to generate this document
	 */
	int getSchemaVersion();

	/**
	 * @return the ID of the stylesheet for which this input document was generated
	 */
	URI getStylesheeURI();

	/**
	 * @return the request that was used to generate the document
	 */
	IDocumentRequest getRequest();

	/**
	 * @return the descriptor containing the values that were used to generate the
	 *         document.
	 */
	IXMLDocumentDescriptor getDocumentDescriptor();

	/**
	 * @return the generated XML document
	 */
	String getDocument();

}
