package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IStylesheetParameterValue} that contains a string value.
 */
public final class StringStylesheetParameterValue extends AbstractStylesheetParameterValue {

	@XmlElement
	private final String result;

	/**
	 * Creates a new parameter value.
	 *
	 * @param parameterID
	 * @param result
	 */
	public StringStylesheetParameterValue(UUID parameterID, String result) {
		super(parameterID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
