package org.x2vc.xml.document;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.x2vc.xml.value.IValueDescriptor;

/**
 * Standard implementation of {@link IDocumentValueModifier}.
 */
public class DocumentValueModifier implements IDocumentValueModifier {

	@XmlTransient
	private IModifierPayload payload;

	@XmlAttribute
	private UUID schemaElementID;

	@XmlAttribute
	private UUID generationRuleID;

	@XmlAttribute
	private String originalValue;

	@XmlAttribute
	private String replacementValue;

	@XmlAttribute
	private String analyzerRuleID;

	private DocumentValueModifier(Builder builder) {
		this.payload = builder.payload;
		this.schemaElementID = builder.schemaElementID;
		this.generationRuleID = builder.generationRuleID;
		this.originalValue = builder.originalValue;
		this.replacementValue = builder.replacementValue;
		this.analyzerRuleID = builder.analyzerRuleID;
	}

	@Override
	public Optional<IModifierPayload> getPayload() {
		return Optional.ofNullable(this.payload);
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
	public Optional<String> getOriginalValue() {
		return Optional.ofNullable(this.originalValue);
	}

	@Override
	public String getReplacementValue() {
		return this.replacementValue;
	}

	@Override
	public Optional<String> getAnalyzerRuleID() {
		return Optional.ofNullable(this.analyzerRuleID);
	}

	@Override
	public IDocumentModifier normalize() {
		return new Builder(this).withGenerationRuleID(UUID.fromString("0000-00-00-00-000000")).withPayload(null)
			.withOriginalValue(null).withAnalyzerRuleID(null).build();
	}

	@Override
	public void sendTo(Consumer<IDocumentModifier> consumer) {
		consumer.accept(this);
	}

	/**
	 * Builder to build {@link DocumentValueModifier}.
	 */
	public static final class Builder {
		private IModifierPayload payload;
		private UUID schemaElementID;
		private UUID generationRuleID;
		private String originalValue;
		private String replacementValue;
		private String analyzerRuleID;

		/**
		 * Creates a new builder
		 *
		 * @param valueDescriptor the descriptor of the value to modify
		 */
		public Builder(IValueDescriptor valueDescriptor) {
			this.schemaElementID = valueDescriptor.getSchemaElementID();
			this.generationRuleID = valueDescriptor.getGenerationRuleID();
		}

		/**
		 * Creates a new builder
		 *
		 * @param schemaElementID
		 * @param generationRuleID
		 *
		 */
		public Builder(UUID schemaElementID, UUID generationRuleID) {
			this.schemaElementID = schemaElementID;
			this.generationRuleID = generationRuleID;
		}

		private Builder(DocumentValueModifier documentValueModifier) {
			this.payload = documentValueModifier.payload;
			this.schemaElementID = documentValueModifier.schemaElementID;
			this.generationRuleID = documentValueModifier.generationRuleID;
			this.originalValue = documentValueModifier.originalValue;
			this.replacementValue = documentValueModifier.replacementValue;
			this.analyzerRuleID = documentValueModifier.analyzerRuleID;
		}

		/**
		 * Builder method for generationRuleID parameter.
		 *
		 * @param generationRuleID field to set
		 * @return builder
		 */
		public Builder withGenerationRuleID(UUID generationRuleID) {
			this.generationRuleID = generationRuleID;
			return this;
		}

		/**
		 * Builder method for payload parameter.
		 *
		 * @param payload field to set
		 * @return builder
		 */
		public Builder withPayload(IModifierPayload payload) {
			this.payload = payload;
			return this;
		}

		/**
		 * Builder method for originalValue parameter.
		 *
		 * @param originalValue field to set
		 * @return builder
		 */
		public Builder withOriginalValue(String originalValue) {
			this.originalValue = originalValue;
			return this;
		}

		/**
		 * Builder method for replacementValue parameter.
		 *
		 * @param replacementValue field to set
		 * @return builder
		 */
		public Builder withReplacementValue(String replacementValue) {
			this.replacementValue = replacementValue;
			return this;
		}

		/**
		 * Builder method for analyzerRuleID parameter.
		 *
		 * @param analyzerRuleID field to set
		 * @return builder
		 */
		public Builder withAnalyzerRuleID(String analyzerRuleID) {
			this.analyzerRuleID = analyzerRuleID;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public DocumentValueModifier build() {
			return new DocumentValueModifier(this);
		}

	}

	@Override
	public int hashCode() {
		return Objects.hash(this.analyzerRuleID, this.generationRuleID, this.originalValue, this.payload,
				this.replacementValue, this.schemaElementID);
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
		final DocumentValueModifier other = (DocumentValueModifier) obj;
		return Objects.equals(this.analyzerRuleID, other.analyzerRuleID)
				&& Objects.equals(this.generationRuleID, other.generationRuleID)
				&& Objects.equals(this.originalValue, other.originalValue)
				&& Objects.equals(this.payload, other.payload)
				&& Objects.equals(this.replacementValue, other.replacementValue)
				&& Objects.equals(this.schemaElementID, other.schemaElementID);
	}

}
