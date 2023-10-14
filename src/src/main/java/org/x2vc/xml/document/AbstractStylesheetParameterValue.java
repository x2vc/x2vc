package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base implementation of {@link IStylesheetParameterValue}.
 */
public abstract class AbstractStylesheetParameterValue implements IStylesheetParameterValue {

	@XmlAttribute
	private final UUID parameterID;

	protected AbstractStylesheetParameterValue(UUID parameterID) {
		super();
		this.parameterID = parameterID;
	}

	@Override
	public UUID getParameterID() {
		return this.parameterID;
	}

}
