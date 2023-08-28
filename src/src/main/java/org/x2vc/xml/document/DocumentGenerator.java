package org.x2vc.xml.document;

import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.XMLDocumentDescriptor.Builder;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IValueGenerator;
import org.x2vc.xml.value.IValueGeneratorFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IDocumentGenerator}.
 */
public class DocumentGenerator implements IDocumentGenerator {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private IValueGeneratorFactory valueGeneratorFactory;

	// TODO XML Document Generator: add namespace support

	/**
	 * @param schemaManager
	 * @param valueGeneratorFactory
	 */
	@Inject
	public DocumentGenerator(ISchemaManager schemaManager, IValueGeneratorFactory valueGeneratorFactory) {
		this.schemaManager = schemaManager;
		this.valueGeneratorFactory = valueGeneratorFactory;
	}

	@Override
	public IXMLDocumentContainer generateDocument(IDocumentRequest request) {
		logger.traceEntry();
		final IValueGenerator valueGenerator = this.valueGeneratorFactory.createValueGenerator(request);
		final IXMLSchema schema = this.schemaManager.getSchema(request.getSchemaURI(), request.getSchemaVersion());
		final String document = new Worker(request, valueGenerator, schema).generateXMLDocument();
		final IXMLDocumentDescriptor descriptor = generateDescriptor(request, valueGenerator);
		final XMLDocumentContainer container = new XMLDocumentContainer(request, descriptor, document);
		return logger.traceExit(container);
	}

	/**
	 * Generates the {@link IXMLDocumentDescriptor}.
	 *
	 * @param request
	 * @param valueGenerator
	 * @return
	 */
	private IXMLDocumentDescriptor generateDescriptor(IDocumentRequest request, IValueGenerator valueGenerator) {
		logger.traceEntry();
		final Builder builder = new XMLDocumentDescriptor.Builder(valueGenerator.getValuePrefix(),
				valueGenerator.getValueLength());
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
		private XMLEventWriter xmlWriter;

		private Map<UUID, String> rawDataReplacementMap = Maps.newHashMap();

		/**
		 * @param request
		 * @param valueGenerator
		 * @param schema
		 */
		public Worker(IDocumentRequest request, IValueGenerator valueGenerator, IXMLSchema schema) {
			this.request = request;
			this.valueGenerator = valueGenerator;
			this.schema = schema;
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
				processAddElementRule(this.request.getRootElementRule());
				this.xmlWriter.add(this.eventFactory.createEndDocument());
				final String rawXML = stringWriter.toString();
				final String postprocessedXML = replaceRawData(rawXML);
				return logger.traceExit(postprocessedXML);
			} catch (final XMLStreamException e) {
				throw logger.throwing(new IllegalStateException("Error generating XML document", e));
			}
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
			this.xmlWriter.add(this.eventFactory.createCharacters(value));
			logger.traceExit();
		}

		/**
		 * Processes an {@link IAddElementRule}.
		 *
		 * @param rule
		 * @throws XMLStreamException
		 */
		private void processAddElementRule(IAddElementRule rule) throws XMLStreamException {
			logger.traceEntry("for rule {} and element reference {}", rule.getID(), rule.getElementReferenceID());
			final String elementName = this.schema.getObjectByID(rule.getElementReferenceID()).asReference().getName();
			this.xmlWriter.add(this.eventFactory.createStartElement(XMLConstants.DEFAULT_NS_PREFIX,
					XMLConstants.NULL_NS_URI, elementName));
			for (final ISetAttributeRule attributeRule : rule.getAttributeRules()) {
				processSetAttributeRule(attributeRule);
			}
			for (final IContentGenerationRule contentRule : rule.getContentRules()) {
				if (contentRule instanceof final IAddDataContentRule addDataRule) {
					processAddDataContentRule(addDataRule);
				} else if (contentRule instanceof final IAddElementRule addElementRule) {
					processAddElementRule(addElementRule);
				} else if (contentRule instanceof final IAddRawContentRule addRawRule) {
					processAddRawContentRule(addRawRule);
				}
			}
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
			final UUID marker = UUID.randomUUID();
			this.rawDataReplacementMap.put(marker, value);
			this.xmlWriter.add(this.eventFactory.createCharacters(String.format("$%s$", marker)));
			logger.traceExit();
		}

		/**
		 * Processes an {@link ISetAttributeRule}.
		 *
		 * @param rule
		 * @throws XMLStreamException
		 */
		private void processSetAttributeRule(ISetAttributeRule rule) throws XMLStreamException {
			logger.traceEntry("for rule {} and attribute {}", rule.getID(), rule.getAttributeID());
			final String attributeName = this.schema.getObjectByID(rule.getAttributeID()).asAttribute().getName();
			final String value = this.valueGenerator.generateValue(rule);
			this.xmlWriter.add(this.eventFactory.createAttribute(attributeName, value));
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
