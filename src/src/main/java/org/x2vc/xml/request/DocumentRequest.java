package org.x2vc.xml.request;

import java.net.URI;
import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.DocumentValueModifier;
import org.x2vc.xml.document.IDocumentModifier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Standard implementation of {@link IDocumentRequest}.
 */
@XmlRootElement(name = "request")
public class DocumentRequest implements IDocumentRequest {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private URI schemaURI;

	@XmlAttribute
	private int schemaVersion;

	@XmlAttribute
	private URI stylesheetURI;

	@XmlElement(type = AddElementRule.class)
	private IAddElementRule rootElementRule;

	@XmlElement(type = DocumentValueModifier.class)
	private IDocumentModifier modifier;

	@XmlAttribute
	private MixedContentGenerationMode mixedContentGenerationMode;

	DocumentRequest() {
		// used for de-/serialization only
	}

	private DocumentRequest(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.stylesheetURI = builder.stylesheetURI;
		this.rootElementRule = builder.rootElementRule;
		this.modifier = builder.modifier;
		this.mixedContentGenerationMode = builder.mixedContentGenerationMode;
	}

	@Override
	public URI getSchemaURI() {
		return this.schemaURI;
	}

	@Override
	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	@Override
	public URI getStylesheeURI() {
		return this.stylesheetURI;
	}

	@Override
	public IAddElementRule getRootElementRule() {
		return this.rootElementRule;
	}

	@Override
	public MixedContentGenerationMode getMixedContentGenerationMode() {
		return this.mixedContentGenerationMode;
	}

	@Override
	public IGenerationRule getRuleByID(UUID ruleID) throws IllegalArgumentException {
		final ImmutableMap<UUID, IGenerationRule> map = this.ruleByIDSupplier.get();
		if (map.containsKey(ruleID)) {
			return map.get(ruleID);
		} else {
			throw new IllegalAccessError(
					String.format("No rule with the ID %s found in this document request.", ruleID));
		}
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<ImmutableMap<UUID, IGenerationRule>> ruleByIDSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final Map<UUID, IGenerationRule> newMap = new HashMap<>();
		addRuleIDsToMapRecursively(this.rootElementRule, newMap);
		return logger.traceExit(ImmutableMap.copyOf(newMap));
	});

	/**
	 * @param rootElementRule2
	 * @param newMap
	 */
	private void addRuleIDsToMapRecursively(IGenerationRule rule, Map<UUID, IGenerationRule> newMap) {
		newMap.put(rule.getID(), rule);
		if (rule instanceof final IAddElementRule addElementRule) {
			addElementRule.getAttributeRules().forEach(subRule -> addRuleIDsToMapRecursively(subRule, newMap));
			addElementRule.getContentRules().forEach(subRule -> addRuleIDsToMapRecursively(subRule, newMap));
		}
	}

	@Override
	public ImmutableMultimap<UUID, IRequestedValue> getRequestedValues() {
		return this.requestedValuesSupplier.get();
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<ImmutableMultimap<UUID, IRequestedValue>> requestedValuesSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final Multimap<UUID, IRequestedValue> newMap = MultimapBuilder.hashKeys().arrayListValues().build();
		addRequestedValuesToMapRecursively(this.rootElementRule, newMap);
		return logger.traceExit(ImmutableMultimap.copyOf(newMap));
	});

	/**
	 * @param rootElementRule2
	 * @param newMap
	 */
	private void addRequestedValuesToMapRecursively(IAddElementRule rule, Multimap<UUID, IRequestedValue> newMap) {
		rule.getAttributeRules().forEach(attr -> {
			final Optional<IRequestedValue> rv = attr.getRequestedValue();
			if (rv.isPresent()) {
				newMap.put(attr.getAttributeID(), rv.get());
			}
		});
		rule.getContentRules().forEach(subRule -> {
			Optional<IRequestedValue> rv = Optional.empty();
			if (subRule instanceof final IAddElementRule addElementRule) {
				addRequestedValuesToMapRecursively(addElementRule, newMap);
			} else if (subRule instanceof final IAddRawContentRule rawContentRule) {
				rv = rawContentRule.getRequestedValue();
				if (rv.isPresent()) {
					newMap.put(rawContentRule.getElementID(), rv.get());
				}
			} else if (subRule instanceof final IAddDataContentRule textContentRule) {
				rv = textContentRule.getRequestedValue();
				if (rv.isPresent()) {
					newMap.put(textContentRule.getElementID(), rv.get());
				}
			}
		});
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	@Override
	public IDocumentRequest normalize() {
		logger.traceEntry();
		final Builder builder = new Builder(this.schemaURI, this.schemaVersion, this.stylesheetURI,
				(IAddElementRule) this.rootElementRule.normalize());
		if (this.modifier != null) {
			builder.withModifier(this.modifier.normalize());
		}
		return logger.traceExit(builder.build());
	}

	/**
	 * Creates a new builder
	 *
	 * @param schemaURI
	 * @param schemaVersion
	 * @param stylesheetURI
	 * @param rootElementRule
	 *
	 * @return the builder
	 */
	public static Builder builder(URI schemaURI, int schemaVersion, URI stylesheetURI,
			IAddElementRule rootElementRule) {
		return new Builder(schemaURI, schemaVersion, stylesheetURI, rootElementRule);
	}

	/**
	 * Creates a new builder
	 *
	 * @param schema
	 * @param rootElementRule
	 *
	 * @return the builder
	 */
	public static Builder builder(IXMLSchema schema, IAddElementRule rootElementRule) {
		return new Builder(schema, rootElementRule);
	}

	/**
	 * Builder to build {@link DocumentRequest}.
	 */
	public static final class Builder {
		private URI schemaURI;
		private int schemaVersion;
		private URI stylesheetURI;
		private IAddElementRule rootElementRule;
		private IDocumentModifier modifier;
		private MixedContentGenerationMode mixedContentGenerationMode = MixedContentGenerationMode.FULL;

		private Builder(URI schemaURI, int schemaVersion, URI stylesheetURI, IAddElementRule rootElementRule) {
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
			this.stylesheetURI = stylesheetURI;
			this.rootElementRule = rootElementRule;
		}

		private Builder(IXMLSchema schema, IAddElementRule rootElementRule) {
			this.schemaURI = schema.getURI();
			this.schemaVersion = schema.getVersion();
			this.stylesheetURI = schema.getStylesheetURI();
			this.rootElementRule = rootElementRule;
		}

		/**
		 * Builder method for modifier parameter.
		 *
		 * @param modifier field to set
		 * @return builder
		 */
		public Builder withModifier(IDocumentModifier modifier) {
			this.modifier = modifier;
			return this;
		}

		/**
		 * Builder method for mixedContentGenerationMode parameter.
		 *
		 * @param mixedContentGenerationMode field to set
		 * @return builder
		 */
		public Builder withMixedContentGenerationMode(MixedContentGenerationMode mixedContentGenerationMode) {
			this.mixedContentGenerationMode = mixedContentGenerationMode;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public DocumentRequest build() {
			return new DocumentRequest(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.mixedContentGenerationMode, this.modifier, this.rootElementRule, this.schemaURI,
				this.schemaVersion,
				this.stylesheetURI);
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
		final DocumentRequest other = (DocumentRequest) obj;
		return this.mixedContentGenerationMode == other.mixedContentGenerationMode
				&& Objects.equals(this.modifier, other.modifier)
				&& Objects.equals(this.rootElementRule, other.rootElementRule)
				&& Objects.equals(this.schemaURI, other.schemaURI) && this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.stylesheetURI, other.stylesheetURI);
	}

}
