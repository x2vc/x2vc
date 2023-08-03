package org.x2vc.xmldoc;

import java.io.Serializable;

/**
 * A description of an input value used to generate an XML document.
 */
public interface IValueDescriptor extends Serializable {

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
