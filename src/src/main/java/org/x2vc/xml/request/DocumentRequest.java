package org.x2vc.xml.request;

import java.net.URI;
import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.DocumentValueModifier;
import org.x2vc.xml.document.IDocumentModifier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

/**
 * Standard implementation of {@link IDocumentRequest}.
 */
@XmlRootElement(name = "request")
public final class DocumentRequest implements IDocumentRequest {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private final URI schemaURI;

	@XmlAttribute
	private final int schemaVersion;

	@XmlAttribute
	private final URI stylesheetURI;

	@XmlElement(type = AddElementRule.class)
	private final IAddElementRule rootElementRule;

	@XmlElement(type = DocumentValueModifier.class)
	private final IDocumentModifier modifier;

	@XmlElementWrapper(name = "extensionFunctions")
	@XmlElement(name = "function", type = ExtensionFunctionRule.class)
	private final List<IExtensionFunctionRule> extensionFunctionRules;

	@XmlElementWrapper(name = "StylesheetParameters")
	@XmlElement(name = "parameter", type = StylesheetParameterRule.class)
	private final List<IStylesheetParameterRule> StylesheetParameterRules;

	@XmlAttribute
	private final MixedContentGenerationMode mixedContentGenerationMode;

	private DocumentRequest() {
		// used for de-/serialization only
		this.schemaURI = null;
		this.schemaVersion = -1;
		this.stylesheetURI = null;
		this.rootElementRule = null;
		this.modifier = null;
		this.mixedContentGenerationMode = null;
		this.extensionFunctionRules = null;
		this.StylesheetParameterRules = null;
	}

	private DocumentRequest(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.stylesheetURI = builder.stylesheetURI;
		this.rootElementRule = builder.rootElementRule;
		this.modifier = builder.modifier;
		this.mixedContentGenerationMode = builder.mixedContentGenerationMode;
		this.extensionFunctionRules = builder.extensionFunctionRules;
		this.StylesheetParameterRules = builder.StylesheetParameterRules;
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
	public ImmutableCollection<IExtensionFunctionRule> getExtensionFunctionRules() {
		return ImmutableList.copyOf(this.extensionFunctionRules);
	}

	@Override
	public ImmutableCollection<IStylesheetParameterRule> getStylesheetParameterRules() {
		return ImmutableList.copyOf(this.StylesheetParameterRules);
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

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<ImmutableMap<UUID, IGenerationRule>> ruleByIDSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final Map<UUID, IGenerationRule> newMap = new HashMap<>();
		addRuleIDsToMapRecursively(getRootElementRule(), newMap);
		getExtensionFunctionRules().forEach(r -> newMap.put(r.getID(), r));
		getStylesheetParameterRules().forEach(r -> newMap.put(r.getID(), r));
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

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<ImmutableMultimap<UUID, IRequestedValue>> requestedValuesSupplier = Suppliers
		.memoize(() -> {
			logger.traceEntry();
			final Multimap<UUID, IRequestedValue> newMap = MultimapBuilder.hashKeys().arrayListValues().build();
			addRequestedValuesToMapRecursively(getRootElementRule(), newMap);
			for (final IExtensionFunctionRule functionRule : getExtensionFunctionRules()) {
				final Optional<IRequestedValue> oRV = functionRule.getRequestedValue();
				if (oRV.isPresent()) {
					newMap.put(functionRule.getFunctionID(), oRV.get());
				}
			}
			for (final IStylesheetParameterRule parameterRule : getStylesheetParameterRules()) {
				final Optional<IRequestedValue> oRV = parameterRule.getRequestedValue();
				if (oRV.isPresent()) {
					newMap.put(parameterRule.getParameterID(), oRV.get());
				}
			}
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
		private List<IExtensionFunctionRule> extensionFunctionRules = Lists.newArrayList();
		private List<IStylesheetParameterRule> StylesheetParameterRules = Lists.newArrayList();

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
		 * Builder method for extensionFunctionRules parameter.
		 *
		 * @param extensionFunctionRule field to set
		 * @return builder
		 */
		public Builder addExtensionFunctionRule(IExtensionFunctionRule extensionFunctionRule) {
			this.extensionFunctionRules.add(extensionFunctionRule);
			return this;
		}

		/**
		 * Builder method for extensionFunctionRules parameter.
		 *
		 * @param extensionFunctionRules field to set
		 * @return builder
		 */
		public Builder addExtensionFunctionRules(Collection<IExtensionFunctionRule> extensionFunctionRules) {
			this.extensionFunctionRules.addAll(extensionFunctionRules);
			return this;
		}

		/**
		 * Builder method for StylesheetParameterRules parameter.
		 *
		 * @param StylesheetParameterRule field to set
		 * @return builder
		 */
		public Builder addStylesheetParameterRule(IStylesheetParameterRule StylesheetParameterRule) {
			this.StylesheetParameterRules.add(StylesheetParameterRule);
			return this;
		}

		/**
		 * Builder method for StylesheetParameterRules parameter.
		 *
		 * @param StylesheetParameterRules field to set
		 * @return builder
		 */
		public Builder addStylesheetParameterRules(Collection<IStylesheetParameterRule> StylesheetParameterRules) {
			this.StylesheetParameterRules.addAll(StylesheetParameterRules);
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
		return Objects.hash(this.extensionFunctionRules, this.mixedContentGenerationMode, this.modifier,
				this.rootElementRule, this.schemaURI,
				this.schemaVersion, this.stylesheetURI, this.StylesheetParameterRules);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DocumentRequest)) {
			return false;
		}
		final DocumentRequest other = (DocumentRequest) obj;
		return Objects.equals(this.extensionFunctionRules, other.extensionFunctionRules)
				&& this.mixedContentGenerationMode == other.mixedContentGenerationMode
				&& Objects.equals(this.modifier, other.modifier)
				&& Objects.equals(this.rootElementRule, other.rootElementRule)
				&& Objects.equals(this.schemaURI, other.schemaURI) && this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.stylesheetURI, other.stylesheetURI)
				&& Objects.equals(this.StylesheetParameterRules, other.StylesheetParameterRules);
	}

}
