package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link ITemplateParameterValue} that contains an integer value.
 */
public final class IntegerTemplateParameterValue extends AbstractTemplateParameterValue {

	@XmlElement
	private final Integer result;

	/**
	 * Creates a new parameter value.
	 *
	 * @param parameterID
	 * @param result
	 */
	public IntegerTemplateParameterValue(UUID parameterID, Integer result) {
		super(parameterID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
