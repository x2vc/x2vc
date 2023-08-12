package org.x2vc.xml.request;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLSchema;

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
	private IAddElementRule rootElementRule;
	private transient ImmutableMultimap<UUID, IRequestedValue> requestedValues;

	/**
	 * @param schemaURI
	 * @param schemaVersion
	 * @param rootElementRule
	 */
	DocumentRequest(URI schemaURI, int schemaVersion, IAddElementRule rootElementRule) {
		super();
		this.schemaURI = schemaURI;
		this.schemaVersion = schemaVersion;
		this.rootElementRule = rootElementRule;
	}

	/**
	 * @param rootElementRule
	 */
	DocumentRequest(IXMLSchema schema, IAddElementRule rootElementRule) {
		super();
		this.schemaURI = schema.getURI();
		this.schemaVersion = schema.getVersion();
		this.rootElementRule = rootElementRule;
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
			} else if (subRule instanceof final IAddDataContentRule textContentRule) {
				rv = textContentRule.getRequestedValue();
			}
			if (rv.isPresent()) {
				newMap.put(rule.getElementReferenceID(), rv.get());
			}
		});
	}

}
