package org.x2vc.xml;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Standard implementation of {@link IDocumentValueModifier}.
 */
public class DocumentValueModifier implements IDocumentValueModifier {

	private static final long serialVersionUID = -1698974074806350109L;
	private IModifierPayload payload;
	private UUID schemaElementID;
	private String originalValue;
	private String replacementValue;
	private String analyzerRuleID;

	private DocumentValueModifier(Builder builder) {
		checkNotNull(builder.replacementValue);
		this.payload = builder.payload;
		this.schemaElementID = builder.schemaElementID;
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

	/**
	 * Builder to build {@link DocumentValueModifier}.
	 */
	public static final class Builder {
		private IModifierPayload payload;
		private UUID schemaElementID;
		private String originalValue;
		private String replacementValue;
		private String analyzerRuleID;

		/**
		 * Creates a new builder
		 *
		 * @param schemaElementID
		 */
		public Builder(UUID schemaElementID) {
			this.schemaElementID = schemaElementID;
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

		/**
		 * Creates the modifier and sends it directly to a consumer.
		 *
		 * @param collector
		 */
		public void sendTo(Consumer<IDocumentModifier> collector) {
			collector.accept(build());
		}

	}

}
