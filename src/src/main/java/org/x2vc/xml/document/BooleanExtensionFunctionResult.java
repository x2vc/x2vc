package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IExtensionFunctionResult} that contains a boolean value.
 */
public class BooleanExtensionFunctionResult extends AbstractExtensionFunctionResult {

	@XmlElement
	private final Boolean result;

	protected BooleanExtensionFunctionResult(UUID functionID, Boolean result) {
		super(functionID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
