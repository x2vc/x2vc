package org.x2vc.xml.request;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Standard implementation of {@link IDocumentRequest}.
 */
public class DocumentRequest implements IDocumentRequest {

	private static final long serialVersionUID = -7197115634513136166L;
	private static final Logger logger = LogManager.getLogger();

	private URI schemaURI;
	private int schemaVersion;
	private URI stylesheetURI;
	private IAddElementRule rootElementRule;
	private IDocumentModifier modifier;
	private transient ImmutableMultimap<UUID, IRequestedValue> requestedValues;

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
	public ImmutableMultimap<UUID, IRequestedValue> getRequestedValues() {
		if (this.requestedValues == null) {
			buildRequestedValues();
		}
		return this.requestedValues;
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	/**
	 * Creates the map of requested values when required.
	 */
	private void buildRequestedValues() {
		logger.traceEntry();
		final Multimap<UUID, IRequestedValue> newMap = MultimapBuilder.hashKeys().arrayListValues().build();
		addRequestedValuesToMapRecursively(this.rootElementRule, newMap);
		this.requestedValues = ImmutableMultimap.copyOf(newMap);
		logger.traceExit();

	}

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
