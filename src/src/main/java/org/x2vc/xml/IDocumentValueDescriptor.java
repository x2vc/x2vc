package org.x2vc.xml;

import java.io.Serializable;
import java.util.UUID;

/**
 * A description of an input value used to generate an XML document.
 */
public interface IDocumentValueDescriptor extends Serializable {

	/**
	 * @return the ID of the schema element that describes the value
	 */
	UUID getSchemaElementID();

	/**
	 * @return the actual value used to generate the document
	 */
	String getValue();

	/**
	 * @return <code>true</code> if the value was requested by another component
	 */
	boolean isMutated();

	// TODO XML Descriptor: link to XML schema

}
