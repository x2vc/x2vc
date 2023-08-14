package org.x2vc.schema.structure;

/**
 * Attribute of an XML Element.
 */
public interface IXMLAttribute extends IXMLDataObject {

	/**
	 * @return the name of the attribute
	 */
	String getName();

	/**
	 * @return <code>true</code> if the attribute is optional
	 */
	boolean isOptional();

	/**
	 * @return <code>true</code> if the value of the attribute can be influenced by
	 *         user input.
	 */
	boolean isUserModifiable();

}
