package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Standard implementation of {@link IStylesheetParameterRule}.
 */
public final class StylesheetParameterRule extends AbstractGenerationRule implements IStylesheetParameterRule {

	@XmlAttribute
	private final UUID parameterID;

	@XmlElement(type = RequestedValue.class)
	private final IRequestedValue requestedValue;

	/**
	 * Creates a new {@link StylesheetParameterRule} with a requested value.
	 *
	 * @param ruleID
	 *
	 * @param parameterID
	 * @param requestedValue
	 */
	public StylesheetParameterRule(UUID ruleID, UUID parameterID, IRequestedValue requestedValue) {
		super(ruleID);
		this.parameterID = parameterID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new {@link StylesheetParameterRule} without a requested value.
	 *
	 * @param ruleID
	 *
	 * @param parameterID
	 */
	public StylesheetParameterRule(UUID ruleID, UUID parameterID) {
		super(ruleID);
		this.parameterID = parameterID;
		this.requestedValue = null;
	}

	/**
	 * Creates a new {@link StylesheetParameterRule} with a requested value.
	 *
	 * @param parameterID
	 * @param requestedValue
	 */
	public StylesheetParameterRule(UUID parameterID, IRequestedValue requestedValue) {
		super();
		this.parameterID = parameterID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new {@link StylesheetParameterRule} without a requested value.
	 *
	 * @param parameterID
	 */
	public StylesheetParameterRule(UUID parameterID) {
		super();
		this.parameterID = parameterID;
		this.requestedValue = null;
	}

	@Override
	public Optional<UUID> getSchemaObjectID() {
		return Optional.of(this.parameterID);
	}

	@Override
	public UUID getParameterID() {
		return this.parameterID;
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	@Override
	public IGenerationRule normalize() {
		return new StylesheetParameterRule(this.parameterID, this.requestedValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.parameterID, this.requestedValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof StylesheetParameterRule)) {
			return false;
		}
		final StylesheetParameterRule other = (StylesheetParameterRule) obj;
		return Objects.equals(this.parameterID, other.parameterID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
