package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link ITemplateParameterValue} that contains a boolean value.
 */
public final class BooleanTemplateParameterValue extends AbstractTemplateParameterValue {

	@XmlElement
	private final Boolean result;

	/**
	 * Creates a new parameter value.
	 *
	 * @param parameterID
	 * @param result
	 */
	public BooleanTemplateParameterValue(UUID parameterID, Boolean result) {
		super(parameterID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
