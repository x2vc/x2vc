package org.x2vc.xml.document;

import java.io.StringWriter;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.XMLUtilities;
import org.x2vc.xml.document.XMLDocumentDescriptor.Builder;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IValueGenerator;
import org.x2vc.xml.value.IValueGeneratorFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IDocumentGenerator}.
 */
public class DocumentGenerator implements IDocumentGenerator {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private IStylesheetManager stylesheetManager;
	private IValueGeneratorFactory valueGeneratorFactory;

	// TODO XML Document Generator: add namespace support

	/**
	 * @param schemaManager
	 * @param stylesheetManager
	 * @param valueGeneratorFactory
	 */
	@Inject
	public DocumentGenerator(ISchemaManager schemaManager, IStylesheetManager stylesheetManager,
			IValueGeneratorFactory valueGeneratorFactory) {
		this.schemaManager = schemaManager;
		this.stylesheetManager = stylesheetManager;
		this.valueGeneratorFactory = valueGeneratorFactory;
	}

	@Override
	public IXMLDocumentContainer generateDocument(IDocumentRequest request) {
		logger.traceEntry();
		final IValueGenerator valueGenerator = this.valueGeneratorFactory.createValueGenerator(request);
		final IXMLSchema schema = this.schemaManager.getSchema(request.getStylesheeURI(), request.getSchemaVersion());
		final IStylesheetInformation stylesheetInformation = this.stylesheetManager.get(request.getStylesheeURI());
		final Worker worker = new Worker(request, valueGenerator, schema, stylesheetInformation);
		final String document = worker.generateXMLDocument();
		final Collection<IExtensionFunctionResult> functionResults = worker.generateFunctionResults();
		final Collection<IStylesheetParameterValue> parameterValues = worker.generateParameterValues();
		final IXMLDocumentDescriptor descriptor = generateDescriptor(request, valueGenerator,
				functionResults, parameterValues, worker.getTraceIDToRuleIDMap());
		final XMLDocumentContainer container = new XMLDocumentContainer(request, descriptor, document);
		return logger.traceExit(container);
	}

	/**
	 * Generates the {@link IXMLDocumentDescriptor}.
	 *
	 * @param request
	 * @param valueGenerator
	 * @param traceIDToRuleIDMap
	 * @param functionResults
	 * @param parameterValues
	 * @return
	 */
	private IXMLDocumentDescriptor generateDescriptor(IDocumentRequest request, IValueGenerator valueGenerator,
			Collection<IExtensionFunctionResult> functionResults, Collection<IStylesheetParameterValue> parameterValues,
			Map<UUID, UUID> traceIDToRuleIDMap) {
		logger.traceEntry();
		final Builder builder = XMLDocumentDescriptor
			.builder(valueGenerator.getValuePrefix(), valueGenerator.getValueLength())
			.withExtensionFunctionResults(functionResults)
			.withStylesheetParameterValues(parameterValues)
			.withTraceIDToRuleIDMap(traceIDToRuleIDMap);
		valueGenerator.getValueDescriptors().forEach(builder::addValueDescriptor);
		final Optional<IDocumentModifier> modifier = request.getModifier();
		if (modifier.isPresent()) {
			builder.withModifier(modifier.get());
		}
		return logger.traceExit(builder.build());
	}

	private class Worker {

		private static final Logger logger = LogManager.getLogger();
		private XMLEventFactory eventFactory = XMLEventFactory.newFactory();
		private XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();

		private IDocumentRequest request;
		private IValueGenerator valueGenerator;
		private IXMLSchema schema;
		private IStylesheetInformation stylesheetInformation;
		private XMLEventWriter xmlWriter;

		private Map<UUID, String> rawDataReplacementMap = Maps.newHashMap();
		private Map<UUID, UUID> traceIDToRuleIDMap = Maps.newHashMap();

		/**
		 * @param request
		 * @param valueGenerator
		 * @param schema
		 * @param stylesheetInformation
		 */
		public Worker(IDocumentRequest request, IValueGenerator valueGenerator, IXMLSchema schema,
				IStylesheetInformation stylesheetInformation) {
			this.request = request;
			this.valueGenerator = valueGenerator;
			this.schema = schema;
			this.stylesheetInformation = stylesheetInformation;
		}

		/**
		 * @return a map that allows for assigning a trace ID found in the XML document to the ID of the rule that
		 *         contributed the element
		 */
		public Map<UUID, UUID> getTraceIDToRuleIDMap() {
			return this.traceIDToRuleIDMap;
		}

		/**
		 * Generates the XML source code of the document.
		 *
		 * @return the XML source code
		 * @throws IllegalStateException
		 */
		public String generateXMLDocument() throws IllegalStateException {
			logger.traceEntry();
			try {
				final StringWriter stringWriter = new StringWriter();
				this.xmlWriter = this.outputFactory.createXMLEventWriter(stringWriter);
				this.xmlWriter.add(this.eventFactory.createStartDocument());
				processAddElementRule(this.request.getRootElementRule(), true);
				this.xmlWriter.add(this.eventFactory.createEndDocument());
				final String rawXML = stringWriter.toString();
				final String postprocessedXML = replaceRawData(rawXML);
				final String formattedXML = XMLUtilities.prettyPrint(postprocessedXML, format -> {
					format.setIndentSize(4);
					format.setSuppressDeclaration(true);
				}).replaceAll("^\\s", "");
				return logger.traceExit(formattedXML);
			} catch (final XMLStreamException e) {
				throw logger.throwing(new IllegalStateException("Error generating XML document", e));
			}
		}

		/**
		 * Generates the extension function return values.
		 *
		 * @return the extension function return values
		 */
		public Collection<IExtensionFunctionResult> generateFunctionResults() {
			logger.traceEntry();
			final List<IExtensionFunctionResult> results = Lists.newArrayList();
			for (final IExtensionFunctionRule rule : this.request.getExtensionFunctionRules()) {
				logger.debug("generating return value for function {} as requested by rule {}", rule.getFunctionID(),
						rule.getID());
				results.add(this.valueGenerator.generateValue(rule));
			}
			return logger.traceExit(results);
		}

		/**
		 * Generates the template parameter values.
		 *
		 * @return the template parameter values
		 */
		public Collection<IStylesheetParameterValue> generateParameterValues() {
			logger.traceEntry();
			final List<IStylesheetParameterValue> results = Lists.newArrayList();
			for (final IStylesheetParameterRule rule : this.request.getStylesheetParameterRules()) {
				logger.debug("generating value for template parameter {} as requested by rule {}",
						rule.getParameterID(),
						rule.getID());
				results.add(this.valueGenerator.generateValue(rule));
			}
			return logger.traceExit(results);
		}

		/**
		 * Processes an {@link IAddDataContentRule}.
		 *
		 * @param rule
		 * @throws XMLStreamException
		 */
		private void processAddDataContentRule(IAddDataContentRule rule) throws XMLStreamException {
			logger.traceEntry("for rule {} and element {}", rule.getID(), rule.getElementID());
			final String value = this.valueGenerator.generateValue(rule);
			if (value.contains("&")) {
				// This is used e.g. by DisabledOutputEscapingCheckRule.
				// We can't add this value through the xmlWriter because it will be escaped.
				// Therefore we add a marker here that will be replaced in post-processing.
				addRawDataForReplacement(value);
			} else {
				this.xmlWriter.add(this.eventFactory.createCharacters(value));
			}
			logger.traceExit();
		}

		/**
		 * Processes an {@link IAddElementRule}.
		 *
		 * @param rule
		 * @param isRoot if this the first call of the method that generates the root element
		 * @throws XMLStreamException
		 */
		private void processAddElementRule(IAddElementRule rule, boolean isRoot) throws XMLStreamException {
			logger.traceEntry("for rule {} and element reference {}", rule.getID(), rule.getElementReferenceID());
			final IElementReference reference = this.schema.getObjectByID(rule.getElementReferenceID(),
					IElementReference.class);
			final String elementName = reference.getName();

			// produce element including trace ID
			final String traceNamespacePrefix = this.stylesheetInformation.getTraceNamespacePrefix();
			final UUID traceElementID = UUID.randomUUID();
			this.traceIDToRuleIDMap.put(traceElementID, rule.getID());
			Set<Namespace> namespaces = null;
			if (isRoot) {
				// namespace is only added to root element
				namespaces = Set.of(this.eventFactory
					.createNamespace(traceNamespacePrefix, TRACE_ELEMENT_NAMESPACE));
			} else {
				namespaces = Set.of();
			}
			final Set<Attribute> attributes = Set
				.of(this.eventFactory.createAttribute(traceNamespacePrefix, TRACE_ELEMENT_NAMESPACE,
						TRACE_ATTRIBUTE_ELEMENT_ID, traceElementID.toString()));
			this.xmlWriter.add(this.eventFactory.createStartElement(XMLConstants.DEFAULT_NS_PREFIX,
					XMLConstants.NULL_NS_URI, elementName, attributes.iterator(), namespaces.iterator()));

			// add attributes if required
			for (final ISetAttributeRule attributeRule : rule.getAttributeRules()) {
				processSetAttributeRule(attributeRule);
			}

			// add content
			for (final IContentGenerationRule contentRule : rule.getContentRules()) {
				if (contentRule instanceof final IAddDataContentRule addDataRule) {
					processAddDataContentRule(addDataRule);
				} else if (contentRule instanceof final IAddElementRule addElementRule) {
					processAddElementRule(addElementRule, false);
				} else if (contentRule instanceof final IAddRawContentRule addRawRule) {
					processAddRawContentRule(addRawRule);
				}
			}

			// and end element
			this.xmlWriter.add(this.eventFactory.createEndElement(XMLConstants.DEFAULT_NS_PREFIX,
					XMLConstants.NULL_NS_URI, elementName));

			logger.traceExit();
		}

		/**
		 * Processes an {@link IAddRawContentRule}.
		 *
		 * @param rule
		 * @throws XMLStreamException
		 */
		private void processAddRawContentRule(IAddRawContentRule rule) throws XMLStreamException {
			logger.traceEntry("for rule {} and element {}", rule.getID(), rule.getElementID());
			final String value = this.valueGenerator.generateValue(rule);
			// We can't add raw data through the xmlWriter because it will be escaped.
			// Therefore we add a marker here that will be replaced in post-processing.
			addRawDataForReplacement(value);
			logger.traceExit();
		}

		/**
		 * @param value
		 * @throws XMLStreamException
		 */
		protected void addRawDataForReplacement(final String value) throws XMLStreamException {
			final UUID marker = UUID.randomUUID();
			this.rawDataReplacementMap.put(marker, value);
			this.xmlWriter.add(this.eventFactory.createCharacters(String.format("$%s$", marker)));
		}

		/**
		 * Processes an {@link ISetAttributeRule}.
		 *
		 * @param rule
		 * @throws XMLStreamException
		 */
		private void processSetAttributeRule(ISetAttributeRule rule) throws XMLStreamException {
			logger.traceEntry("for rule {} and attribute {}", rule.getID(), rule.getAttributeID());
			final IAttribute attribute = this.schema.getObjectByID(rule.getAttributeID(), IAttribute.class);
			final String value = this.valueGenerator.generateValue(rule);
			if (value.contains("&")) {
				// This is used e.g. by DisabledOutputEscapingCheckRule.
				// We can't add this value through the xmlWriter because it will be escaped.
				// Therefore we add a marker here that will be replaced in post-processing.
				final UUID marker = UUID.randomUUID();
				this.rawDataReplacementMap.put(marker, value);
				this.xmlWriter
					.add(this.eventFactory.createAttribute(attribute.getName(), String.format("$%s$", marker)));
			} else {
				this.xmlWriter.add(this.eventFactory.createAttribute(attribute.getName(), value));
			}
			logger.traceExit();
		}

		/**
		 * Replaces the raw data markers with the actual data
		 *
		 * @param rawXML
		 * @return
		 */
		private String replaceRawData(String rawXML) {
			logger.traceEntry(rawXML);
			String value = rawXML;
			int replacedValues = 0;
			for (final var entry : this.rawDataReplacementMap.entrySet()) {
				value = value.replaceAll(String.format("\\$%s\\$", entry.getKey()), entry.getValue());
				replacedValues++;
			}
			if (replacedValues != this.rawDataReplacementMap.size()) {
				logger.warn("not all raw data markers were replaced (expected {}, actual {})",
						this.rawDataReplacementMap.size(), replacedValues);
			}
			return logger.traceExit(value);
		}

	}

}
