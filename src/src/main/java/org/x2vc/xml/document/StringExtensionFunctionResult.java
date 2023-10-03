package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IExtensionFunctionResult} that contains a string value.
 */
public final class StringExtensionFunctionResult extends AbstractExtensionFunctionResult {

	@XmlElement
	private final String result;

	/**
	 * Creates a new function result.
	 *
	 * @param functionID
	 * @param result
	 */
	public StringExtensionFunctionResult(UUID functionID, String result) {
		super(functionID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
