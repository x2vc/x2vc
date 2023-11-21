/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.xml.document;


import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.x2vc.analysis.rules.AnalyzerRulePayload;
import org.x2vc.xml.value.IValueDescriptor;

/**
 * Standard implementation of {@link IDocumentValueModifier}.
 */
public class DocumentValueModifier implements IDocumentValueModifier {

	@XmlElements({
			@XmlElement(name = "analyzerRulePayload", type = AnalyzerRulePayload.class)
	})
	private IModifierPayload payload;

	@XmlAttribute
	private UUID schemaObjectID;

	@XmlAttribute
	private UUID generationRuleID;

	@XmlElement
	private String originalValue;

	@XmlElement
	private String replacementValue;

	@XmlAttribute
	private String analyzerRuleID;

	private DocumentValueModifier(Builder builder) {
		this.payload = builder.payload;
		this.schemaObjectID = builder.schemaObjectID;
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
	public UUID getSchemaObjectID() {
		return this.schemaObjectID;
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
	 * Creates a new builder
	 *
	 * @param valueDescriptor the descriptor of the value to modify
	 * @return the builder
	 */
	public static Builder builder(IValueDescriptor valueDescriptor) {
		return new Builder(valueDescriptor);
	}

	/**
	 * Creates a new builder
	 *
	 * @param schemaObjectID
	 * @param generationRuleID
	 * @return the builder
	 *
	 */
	public static Builder builder(UUID schemaObjectID, UUID generationRuleID) {
		return new Builder(schemaObjectID, generationRuleID);
	}

	/**
	 * Builder to build {@link DocumentValueModifier}.
	 */
	public static final class Builder {
		private IModifierPayload payload;
		private UUID schemaObjectID;
		private UUID generationRuleID;
		private String originalValue;
		private String replacementValue;
		private String analyzerRuleID;

		/**
		 * Creates a new builder
		 *
		 * @param valueDescriptor the descriptor of the value to modify
		 */
		private Builder(IValueDescriptor valueDescriptor) {
			this.schemaObjectID = valueDescriptor.getSchemaObjectID();
			this.generationRuleID = valueDescriptor.getGenerationRuleID();
		}

		/**
		 * Creates a new builder
		 *
		 * @param schemaObjectID
		 * @param generationRuleID
		 *
		 */
		private Builder(UUID schemaObjectID, UUID generationRuleID) {
			this.schemaObjectID = schemaObjectID;
			this.generationRuleID = generationRuleID;
		}

		private Builder(DocumentValueModifier documentValueModifier) {
			this.payload = documentValueModifier.payload;
			this.schemaObjectID = documentValueModifier.schemaObjectID;
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
				this.replacementValue, this.schemaObjectID);
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
				&& Objects.equals(this.schemaObjectID, other.schemaObjectID);
	}

}
