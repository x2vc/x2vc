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

	/**
	 * @param schemaURI
	 * @param schemaVersion
	 * @param rootElementRule
	 */
	DocumentRequest(URI schemaURI, int schemaVersion, URI stylesheetURI, IAddElementRule rootElementRule) {
		super();
		this.schemaURI = schemaURI;
		this.schemaVersion = schemaVersion;
		this.stylesheetURI = stylesheetURI;
		this.rootElementRule = rootElementRule;
	}

	/**
	 * @param schemaURI
	 * @param schemaVersion
	 * @param rootElementRule
	 */
	DocumentRequest(URI schemaURI, int schemaVersion, URI stylesheetURI, IAddElementRule rootElementRule,
			IDocumentModifier modifier) {
		super();
		this.schemaURI = schemaURI;
		this.schemaVersion = schemaVersion;
		this.stylesheetURI = stylesheetURI;
		this.rootElementRule = rootElementRule;
		this.modifier = modifier;
	}

	/**
	 * @param rootElementRule
	 */
	DocumentRequest(IXMLSchema schema, IAddElementRule rootElementRule) {
		super();
		this.schemaURI = schema.getURI();
		this.schemaVersion = schema.getVersion();
		this.stylesheetURI = schema.getStylesheetURI();
		this.rootElementRule = rootElementRule;
	}

	/**
	 * @param rootElementRule
	 */
	DocumentRequest(IXMLSchema schema, IAddElementRule rootElementRule, IDocumentModifier modifier) {
		super();
		this.schemaURI = schema.getURI();
		this.schemaVersion = schema.getVersion();
		this.stylesheetURI = schema.getStylesheetURI();
		this.rootElementRule = rootElementRule;
		this.modifier = modifier;
	}

	DocumentRequest() {
		// used for de-/serialization only
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
		if (this.modifier == null) {
			return new DocumentRequest(this.schemaURI, this.schemaVersion, this.stylesheetURI,
					(IAddElementRule) this.rootElementRule.normalize());
		} else {
			return new DocumentRequest(this.schemaURI, this.schemaVersion, this.stylesheetURI,
					(IAddElementRule) this.rootElementRule.normalize(), this.modifier.normalize());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.modifier, this.rootElementRule, this.schemaURI, this.schemaVersion,
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
		return Objects.equals(this.modifier, other.modifier)
				&& Objects.equals(this.rootElementRule, other.rootElementRule)
				&& Objects.equals(this.schemaURI, other.schemaURI) && this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.stylesheetURI, other.stylesheetURI);
	}

}
