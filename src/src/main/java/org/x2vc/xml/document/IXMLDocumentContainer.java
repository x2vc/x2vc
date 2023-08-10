package org.x2vc.xml.document;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;

/**
 * A container object that is used to transport a generated XML document, its
 * corresponding descriptor and a reference to the request used to generate the
 * document. The container also references the XML schema version used to
 * generate the document.
 */
public interface IXMLDocumentContainer {

	/**
	 * @return the {@link IXMLSchema} used to generate this document
	 */
	IXMLSchema getSchema();

	/**
	 * Shortcut for <code>getSchema().getStylesheet()</code>
	 *
	 * @return the stylesheet for which this input document was generated
	 */
	IStylesheetInformation getStylesheet();

	/**
	 * @return the descriptor containing the values that were used to generate the
	 *         document.
	 */
	IXMLDocumentDescriptor getDocumentDescriptor();

	/**
	 * @return the generated XML document
	 */
	String getDocument();

	// TODO XML Document: add missing accessors: request
	// TODO XML Document: add missing accessors: schema and version

}
