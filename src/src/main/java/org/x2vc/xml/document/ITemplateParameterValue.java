package org.x2vc.xml.document;

import java.util.UUID;

import org.x2vc.schema.structure.ITemplateParameter;

import net.sf.saxon.s9api.XdmValue;

/**
 * A representation of template parameter and its actual value .
 */
public interface ITemplateParameterValue {

	/**
	 * @return the parameter ID as provided by the schema
	 * @see ITemplateParameter#getID()
	 */
	UUID getParameterID();

	/**
	 * @return the result of the function call as {@link XdmValue}
	 */
	XdmValue getXDMValue();

}
