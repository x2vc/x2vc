package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IXMLAttribute;

/**
 *
 */
public class SetAttributeRule implements ISetAttributeRule {

	private static final long serialVersionUID = 1L;
	private UUID attributeID;
	private IRequestedValue requestedValue;

	/**
	 * Creates a new attribute rule with a requested value specified.
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
	 * Creates a new attribute rule with a requested value specified.
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
	 * Creates a new attribute rule without a requested value specified.
	 *
	 * @param attribute
	 */
	SetAttributeRule(UUID attributeID) {
		super();
		this.attributeID = attributeID;
	}

	/**
	 * Creates a new attribute rule without a requested value specified.
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
		return Objects.hash(this.attributeID, this.requestedValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
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
