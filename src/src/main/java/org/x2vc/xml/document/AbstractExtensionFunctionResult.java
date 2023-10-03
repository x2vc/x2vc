package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base implementation of {@link IExtensionFunctionResult}.
 */
public abstract class AbstractExtensionFunctionResult implements IExtensionFunctionResult {

	@XmlAttribute
	private final UUID functionID;

	protected AbstractExtensionFunctionResult(UUID functionID) {
		super();
		this.functionID = functionID;
	}

	@Override
	public UUID getFunctionID() {
		return this.functionID;
	}

}
