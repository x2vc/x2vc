package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IExtensionFunctionResult} that contains an integer value.
 */
public class IntegerExtensionFunctionResult extends AbstractExtensionFunctionResult {

	@XmlElement
	private final Integer result;

	protected IntegerExtensionFunctionResult(UUID functionID, Integer result) {
		super(functionID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
