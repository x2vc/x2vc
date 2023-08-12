package org.x2vc.xml.document;

import java.util.Objects;
import java.util.UUID;

/**
 * Standard implementation of {@link IDocumentValueDescriptor}.
 */
public class DocumentValueDescriptor implements IDocumentValueDescriptor {

	private static final long serialVersionUID = 8574714365246157772L;
	private UUID schemaElementID;
	private UUID generationRuleID;
	private String value;
	private boolean mutated = false;

	/**
	 * @param schemaElementID
	 * @param generationRuleID
	 * @param value
	 * @param mutated
	 */
	DocumentValueDescriptor(UUID schemaElementID, UUID generationRuleID, String value, boolean mutated) {
		super();
		this.schemaElementID = schemaElementID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.mutated = mutated;
	}

	/**
	 * @param schemaElementID
	 * @param generationRuleID
	 * @param value
	 */
	DocumentValueDescriptor(UUID schemaElementID, UUID generationRuleID, String value) {
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
	public boolean isMutated() {
		return this.mutated;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.generationRuleID, this.mutated, this.schemaElementID, this.value);
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
		final DocumentValueDescriptor other = (DocumentValueDescriptor) obj;
		return Objects.equals(this.generationRuleID, other.generationRuleID) && this.mutated == other.mutated
				&& Objects.equals(this.schemaElementID, other.schemaElementID)
				&& Objects.equals(this.value, other.value);
	}

}
