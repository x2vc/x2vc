package org.x2vc.xml.value;

import java.util.Objects;
import java.util.UUID;

/**
 * Standard implementation of {@link IValueDescriptor}.
 */
public class ValueDescriptor implements IValueDescriptor {

	private static final long serialVersionUID = 8574714365246157772L;
	private UUID schemaElementID;
	private UUID generationRuleID;
	private String value;
	private boolean requested = false;

	/**
	 * @param schemaElementID
	 * @param generationRuleID
	 * @param value
	 * @param requested
	 */
	public ValueDescriptor(UUID schemaElementID, UUID generationRuleID, String value, boolean requested) {
		super();
		this.schemaElementID = schemaElementID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.requested = requested;
	}

	/**
	 * @param schemaElementID
	 * @param generationRuleID
	 * @param value
	 */
	public ValueDescriptor(UUID schemaElementID, UUID generationRuleID, String value) {
		super();
		this.schemaElementID = schemaElementID;
		this.generationRuleID = generationRuleID;
		this.value = value;
	}

	@Override
	public UUID getSchemaElementID() {
		return this.schemaElementID;
	}

	@Override
	public UUID getGenerationRuleID() {
		return this.generationRuleID;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public boolean isRequested() {
		return this.requested;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.generationRuleID, this.requested, this.schemaElementID, this.value);
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
		final ValueDescriptor other = (ValueDescriptor) obj;
		return Objects.equals(this.generationRuleID, other.generationRuleID) && this.requested == other.requested
				&& Objects.equals(this.schemaElementID, other.schemaElementID)
				&& Objects.equals(this.value, other.value);
	}

}
