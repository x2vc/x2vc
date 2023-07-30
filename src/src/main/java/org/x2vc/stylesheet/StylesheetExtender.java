package org.x2vc.stylesheet;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.XSLTConstants;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Standard implementation of {@link IStylesheetExtender}.
 */
public class StylesheetExtender implements IStylesheetExtender {

	private XMLInputFactory inputFactory = XMLInputFactory.newFactory();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
	private Logger logger = LogManager.getLogger();

	/**
	 * The following directive elements require a trace message BEFORE the actual
	 * directive element.
	 */
	private static final ImmutableSet<String> ELEMENTS_WITH_TRACE_BEFORE = ImmutableSet.of(
			XSLTConstants.ELEMENT_APPLY_IMPORTS, XSLTConstants.ELEMENT_APPLY_TEMPLATES,
			XSLTConstants.ELEMENT_CALL_TEMPLATE, XSLTConstants.ELEMENT_CHOOSE);

	/**
	 * The following directive elements require a trace message INSIDE / after the
	 * actual directive element.
	 */
	private static final ImmutableSet<String> ELEMENTS_WITH_TRACE_AFTER = ImmutableSet.of(XSLTConstants.ELEMENT_IF,
			XSLTConstants.ELEMENT_WHEN, XSLTConstants.ELEMENT_OTHERWISE, XSLTConstants.ELEMENT_TEMPLATE,
			XSLTConstants.ELEMENT_FOR_EACH);

	@Override
	public String extendStylesheet(String originalStylesheet) throws IllegalArgumentException {
		try {
			this.logger.info("begin of stylesheet extension");
			// prepare Stax objects for transformation
			StringWriter stringWriter = new StringWriter();
			XMLEventWriter xmlWriter = this.outputFactory.createXMLEventWriter(stringWriter);
			// the actual processing is performed in a separate worker class to facilitate
			// thread safety
			new Worker(this.inputFactory, xmlWriter).process(originalStylesheet);
			this.logger.info("end of stylesheet extension");
			return stringWriter.toString();
		} catch (XMLStreamException e) {
			throw new IllegalArgumentException("Unable to extend stylesheet.", e);
		}
	}

	private class Worker {

		private static final String EXTENSION_NAMESPACE = "http://www.github.com/vwegert/x2vc/XSLTExtension";

		private Logger logger = LogManager.getLogger();
		private XMLInputFactory inputFactory;
		private XMLEventWriter xmlWriter;
		private XMLEventFactory eventFactory = XMLEventFactory.newFactory();

		private String xsltPrefix;
		private String extensionPrefix;

		public Worker(XMLInputFactory inputFactory, XMLEventWriter xmlWriter) {
			this.inputFactory = inputFactory;
			this.xmlWriter = xmlWriter;
		}

		public void process(String originalStylesheet) throws XMLStreamException {

			// We want to introduce a new global namespace prefix. In order to avoid
			// conflicts with existing prefixes, first build a map of all prefixes that have
			// been used.
			Set<String> usedNamespacePrefixes = collectNamespacePrefixes(originalStylesheet);

			// Invent a new namespace prefix for our extension namespace.
			this.extensionPrefix = findUnusedNamespacePrefix(usedNamespacePrefixes, "ext");
			this.logger.trace("will use prefix {} for extension namespace", this.extensionPrefix);

			XMLEventReader xmlReader = this.inputFactory.createXMLEventReader(new StringReader(originalStylesheet));

			boolean rootElementVisited = false;
			int nextElementID = 1;

			while (xmlReader.hasNext()) {
				XMLEvent event = xmlReader.nextEvent();
				int currentElementID = -1;

				// processing BEFORE the event is written to the output stream
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();

					if (!rootElementVisited) {
						// root element: add namespace
						startElement = extendRootElement(startElement, this.extensionPrefix);
						event = startElement;
						this.logger.trace("extended root element to {}", event.toString());
						rootElementVisited = true;
					} else {
						// other elements: only handle certain XSL elements
						if (startElement.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
							final String elementName = startElement.getName().getLocalPart();

							// add trace ID to element if required
							if (ELEMENTS_WITH_TRACE_BEFORE.contains(elementName)
									|| ELEMENTS_WITH_TRACE_AFTER.contains(elementName)) {
								currentElementID = nextElementID++;
								startElement = addIDToElement(startElement, this.extensionPrefix, currentElementID);
								event = startElement;
								this.logger.trace("added ID {} to element {}", currentElementID, event.toString());
							}

							// write trace message before element if required
							if (ELEMENTS_WITH_TRACE_BEFORE.contains(elementName)) {
								writeElementMessage(startElement, currentElementID);
							}

							// TODO XSLT extension: support select expression variables for apply-templates

							// TODO XSLT extension: support template call parameters for apply-templates and
							// call-template
						}
					}
				}

				this.xmlWriter.add(event);

				// processing AFTER the event is written to the output stream
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// only handle certain XSL elements
					if (startElement.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
						final String elementName = startElement.getName().getLocalPart();

						// write trace message after element if required
						if (ELEMENTS_WITH_TRACE_AFTER.contains(elementName)) {
							writeElementMessage(startElement, currentElementID);
						}
					}

				}
				// TODO XSLT extension: support select expression variables for for-each

				// TODO XSLT extension: support condition tracing for if and when

				// TODO XSLT extension: support condition tracing

				// TODO XSLT extension: support condition tracing (match) for template

				// TODO XSLT extension: support template actual parameters for template

			}
		}

		private Set<String> collectNamespacePrefixes(String originalStylesheet) throws XMLStreamException {
			this.logger.trace("collecting existing namspace prefixes");
			XMLEventReader xmlReader = this.inputFactory.createXMLEventReader(new StringReader(originalStylesheet));
			Set<String> prefixes = new HashSet<String>();
			while (xmlReader.hasNext()) {
				XMLEvent event = xmlReader.nextEvent();
				if (event.isStartElement()) {
					event.asStartElement().getNamespaces().forEachRemaining(ns -> {
						if (ns.getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
							this.xsltPrefix = ns.getPrefix();
						}
						String prefix = ns.getPrefix();
						this.logger.trace("encountered namespace prefix {}", prefix);
						prefixes.add(prefix);
					});
				}
			}
			return prefixes;
		}

		private String findUnusedNamespacePrefix(Set<String> usedNamespacePrefixes, String startsWith) {
			int prefixNumber = 0;
			do {
				String prefixCandidate = String.format("%s%s", startsWith, prefixNumber);
				if (!usedNamespacePrefixes.contains(prefixCandidate)) {
					return prefixCandidate;
				}
				prefixNumber += 1;
			} while (true);
		}

		private StartElement extendRootElement(StartElement originalElement, String newExtensionPrefix) {
			ArrayList<Namespace> namespaces = Lists.newArrayList(originalElement.getNamespaces());
			namespaces.add(this.eventFactory.createNamespace(newExtensionPrefix, EXTENSION_NAMESPACE));
			return this.eventFactory.createStartElement(originalElement.getName(), originalElement.getAttributes(),
					namespaces.iterator());
		}

		private StartElement addIDToElement(StartElement originalElement, String extensionPrefix, int elementID) {
			ArrayList<Attribute> attributes = Lists.newArrayList(originalElement.getAttributes());
			attributes.add(this.eventFactory.createAttribute(extensionPrefix, EXTENSION_NAMESPACE, "trace-id",
					Integer.toString(elementID)));
			return this.eventFactory.createStartElement(originalElement.getName(), attributes.iterator(),
					originalElement.getNamespaces());
		}

		private void writeElementMessage(StartElement startElement, int elementID) throws XMLStreamException {
			String traceMessage = String.format("trace type=elem name=%s id=%s", startElement.getName().getLocalPart(),
					elementID);
			this.xmlWriter.add(this.eventFactory.createStartElement(this.xsltPrefix, XSLTConstants.NAMESPACE,
					XSLTConstants.ELEMENT_MESSAGE));
			this.xmlWriter.add(this.eventFactory.createCharacters(traceMessage));
			this.xmlWriter.add(this.eventFactory.createEndElement(this.xsltPrefix, XSLTConstants.NAMESPACE,
					XSLTConstants.ELEMENT_MESSAGE));

		}

	}

}
