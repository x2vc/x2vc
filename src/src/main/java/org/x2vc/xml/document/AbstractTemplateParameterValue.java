package org.x2vc.xml.document;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base implementation of {@link ITemplateParameterValue}.
 */
public abstract class AbstractTemplateParameterValue implements ITemplateParameterValue {

	@XmlAttribute
	private final UUID parameterID;

	protected AbstractTemplateParameterValue(UUID parameterID) {
		super();
		this.parameterID = parameterID;
	}

	@Override
	public UUID getParameterID() {
		return this.parameterID;
	}

}
