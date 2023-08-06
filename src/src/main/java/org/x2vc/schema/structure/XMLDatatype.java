package org.x2vc.schema.structure;

/**
 * The base data types suported by the XML schema.
 */
public enum XMLDatatype {

	/**
	 * A character string value. Additional specifications: maxLength.
	 */
	STRING,

	/**
	 * A boolean value (<code>true</code> or <code>false</code>).
	 */
	BOOLEAN,

	/**
	 * An integer value, optionally signed. Additional specifications: minValue,
	 * maxValue).
	 */
	INTEGER,

	// TODO XML Schema: Support other numeric data types.
	// TODO XML Schema: Support date/time/duration data types.
	// TODO XML Schema: Support binary data types.

	/**
	 * Another type not explicitly supported.
	 */
	OTHER

}
