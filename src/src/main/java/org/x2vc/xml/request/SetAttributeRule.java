package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IXMLAttribute;

/**
 * Standard implementation of {@link ISetAttributeRule}.
 */
public class SetAttributeRule extends AbstractGenerationRule implements ISetAttributeRule {

	private static final long serialVersionUID = 1L;
	private UUID attributeID;
	private IRequestedValue requestedValue;

	/**
	 * Creates a new attribute rule with a specified ID and a requested value
	 * specified.
	 *
	 * @param attributeID
	 * @param requestedValue
	 */
	SetAttributeRule(UUID ruleID, UUID attributeID, IRequestedValue requestedValue) {
		super(ruleID);
		this.attributeID = attributeID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a random ID and a requested value
	 * specified.
	 *
	 * @param attributeID
	 * @param requestedValue
	 */
	SetAttributeRule(UUID attributeID, IRequestedValue requestedValue) {
		super();
		this.attributeID = attributeID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a specified ID and a requested value
	 * specified.
	 *
	 * @param attributeID
	 * @param requestedValue
	 */
	SetAttributeRule(UUID ruleID, IXMLAttribute attribute, IRequestedValue requestedValue) {
		super(ruleID);
		this.attributeID = attribute.getID();
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a random ID and a requested value
	 * specified.
	 *
	 * @param attributeID
	 * @param requestedValue
	 */
	SetAttributeRule(IXMLAttribute attribute, IRequestedValue requestedValue) {
		super();
		this.attributeID = attribute.getID();
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a specified ID and without a requested
	 * value specified.
	 *
	 * @param attribute
	 */
	SetAttributeRule(UUID ruleID, UUID attributeID) {
		super(ruleID);
		this.attributeID = attributeID;
	}

	/**
	 * Creates a new attribute rule with a random ID and without a requested value
	 * specified.
	 *
	 * @param attribute
	 */
	SetAttributeRule(UUID attributeID) {
		super();
		this.attributeID = attributeID;
	}

	/**
	 * Creates a new attribute rule with a specified ID and without a requested
	 * value specified.
	 *
	 * @param attribute
	 */
	SetAttributeRule(UUID ruleID, IXMLAttribute attribute) {
		super(ruleID);
		this.attributeID = attribute.getID();
	}

	/**
	 * Creates a new attribute rule with a random ID and without a requested value
	 * specified.
	 *
	 * @param attribute
	 */
	SetAttributeRule(IXMLAttribute attribute) {
		super();
		this.attributeID = attribute.getID();
	}

	@Override
	public UUID getAttributeID() {
		return this.attributeID;
	}

	/**
	 * @param attributeID the attributeID to set
	 */
	public void setAttributeID(UUID attributeID) {
		this.attributeID = attributeID;
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	/**
	 * @param requestedValue the requestedValue to set
	 */
	public void setRequestedValue(IRequestedValue requestedValue) {
		this.requestedValue = requestedValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.attributeID, this.requestedValue);
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SetAttributeRule other = (SetAttributeRule) obj;
		return Objects.equals(this.attributeID, other.attributeID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
