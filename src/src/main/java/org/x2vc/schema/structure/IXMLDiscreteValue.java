package org.x2vc.schema.structure;

/**
 * A discrete value used to represent either "interesting" values or hard
 * restrictions.
 */
public interface IXMLDiscreteValue extends IXMLSchemaObject {

	/**
	 * @return the data type of the value, represented as {@link XMLDataType}.
	 */
	XMLDataType getDataType();

	/**
	 * @return the value represented as String, if supported
	 */
	String asString();

	/**
	 * @return the value represented as Boolean, if supported
	 */
	Boolean asBoolean();

	/**
	 * @return the value represented as Integer, if supported
	 */
	Integer asInteger();

}
