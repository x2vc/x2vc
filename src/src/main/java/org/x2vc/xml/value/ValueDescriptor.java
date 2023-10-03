package org.x2vc.xml.value;

import java.util.Objects;
import java.util.UUID;

/**
 * Standard implementation of {@link IValueDescriptor}.
 */
public final class ValueDescriptor implements IValueDescriptor {

	private final UUID schemaObjectID;
	private final UUID generationRuleID;
	private final String value;
	private final boolean requested;

	/**
	 * @param schemaObjectID
	 * @param generationRuleID
	 * @param value
	 * @param requested
	 */
	public ValueDescriptor(UUID schemaObjectID, UUID generationRuleID, String value, boolean requested) {
		super();
		this.schemaObjectID = schemaObjectID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.requested = requested;
	}

	/**
	 * @param schemaObjectID
	 * @param generationRuleID
	 * @param value
	 */
	public ValueDescriptor(UUID schemaObjectID, UUID generationRuleID, String value) {
		super();
		this.schemaObjectID = schemaObjectID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.requested = false;
	}

	@Override
	public UUID getSchemaObjectID() {
		return this.schemaObjectID;
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
		return Objects.hash(this.generationRuleID, this.requested, this.schemaObjectID, this.value);
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
				&& Objects.equals(this.schemaObjectID, other.schemaObjectID)
				&& Objects.equals(this.value, other.value);
	}

}
