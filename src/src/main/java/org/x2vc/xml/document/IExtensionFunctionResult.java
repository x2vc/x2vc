package org.x2vc.xml.document;

import java.util.UUID;

import org.x2vc.schema.structure.IExtensionFunction;

import net.sf.saxon.s9api.XdmValue;

/**
 * A representation of a call to an extension function (represented by the function ID as provided by the schema) and
 * the generated return value.
 *
 * In a subsequent version, matchers to restrict the return value to various input parameters might be added here.
 */
public interface IExtensionFunctionResult {

	/**
	 * @return the function ID as provided by the schema
	 * @see IExtensionFunction#getID()
	 */
	UUID getFunctionID();

	/**
	 * @return the result of the function call as {@link XdmValue}
	 */
	XdmValue getXDMValue();

}
