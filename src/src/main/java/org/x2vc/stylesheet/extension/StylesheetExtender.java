package org.x2vc.stylesheet.extension;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
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
import org.x2vc.stylesheet.ExtendedXSLTConstants;
import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.IPushbackXMLEventReader;
import org.x2vc.utilities.PushbackXMLEventReader;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IStylesheetExtender}.
 */
public class StylesheetExtender implements IStylesheetExtender {

	private XMLInputFactory inputFactory = XMLInputFactory.newFactory();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
	private static final Logger logger = LogManager.getLogger();

	@Inject
	StylesheetExtender() {
		// nothing to do at the moment
	}

	/**
	 * The following directive elements require a trace message BEFORE the actual
	 * directive element.
	 */
	private static final Set<String> ELEMENTS_WITH_TRACE_BEFORE = Set.of(XSLTConstants.Elements.APPLY_IMPORTS,
			XSLTConstants.Elements.APPLY_TEMPLATES, XSLTConstants.Elements.CALL_TEMPLATE,
			XSLTConstants.Elements.CHOOSE);

	/**
	 * The following directive elements require a trace message INSIDE / after the
	 * actual directive element.
	 */
	private static final Set<String> ELEMENTS_WITH_TRACE_AFTER = Set.of(XSLTConstants.Elements.IF,
			XSLTConstants.Elements.WHEN, XSLTConstants.Elements.OTHERWISE, XSLTConstants.Elements.TEMPLATE,
			XSLTConstants.Elements.FOR_EACH);

	/**
	 * The following directive elements delay the output of a trace message until
	 * the elements have been closed.
	 */
	private static final Set<String> ELEMENTS_DELAYING_MESSAGE = Set.of(XSLTConstants.Elements.SORT,
			XSLTConstants.Elements.PARAM, XSLTConstants.Elements.WITH_PARAM);

	@Override
	public String extendStylesheet(String originalStylesheet) throws IllegalArgumentException {
		logger.traceEntry();
		try {
			final StringWriter stringWriter = new StringWriter();
			final XMLEventWriter xmlWriter = this.outputFactory.createXMLEventWriter(stringWriter);
			new Worker(xmlWriter).process(originalStylesheet);
			final String result = stringWriter.toString();
			return logger.traceExit(result);
		} catch (final XMLStreamException e) {
			throw logger.throwing(new IllegalArgumentException("Unable to extend stylesheet.", e));
		}
	}

	/**
	 * This class is used for the actual implementation in order to ensure thread
	 * safety.
	 */
	private class Worker {

		private static final Logger logger = LogManager.getLogger();
		private XMLEventFactory eventFactory = XMLEventFactory.newFactory();

		private XMLEventWriter xmlWriter;

		/**
		 * The namespace prefix used for the XSLT namespace in this document.
		 */
		private String xsltPrefix;

		/**
		 * The namespace prefix used for the Extension namespace in this document.
		 */
		private String extensionPrefix;

		/**
		 * The next trace ID to assign to an element.
		 *
		 * @see #getNextElementID()
		 */
		int nextElementID = 1;

		/**
		 * Whether the root element has already been processed and extended.
		 *
		 * @see #extendRootElement(StartElement, String)
		 */
		boolean rootElementVisited = false;

		/**
		 * If the current element was assigned a trace ID, it is stored here for the
		 * duration of the loop iteration.
		 */
		Optional<Integer> currentElementID = Optional.empty();

		/**
		 * Reader used to process the document. Only available during the main
		 * processing loop.
		 */
		IPushbackXMLEventReader xmlReader;

		/**
		 * Create a new worker instance.
		 *
		 * @param xmlWriter the output target for the extended stylesheet
		 */
		public Worker(XMLEventWriter xmlWriter) {
			this.xmlWriter = xmlWriter;
		}

		/**
		 * Perform the actual stylesheet extension.
		 *
		 * @param originalStylesheet the source code of the stylesheet to extend
		 * @throws XMLStreamException
		 */
		public void process(String originalStylesheet) throws XMLStreamException {
			logger.traceEntry();

			// We want to introduce a new global namespace prefix. In order to avoid
			// conflicts with existing prefixes, first build a map of all prefixes that have
			// been used.
			final Set<String> usedNamespacePrefixes = collectNamespacePrefixes(originalStylesheet);

			// Invent a new namespace prefix for our extension namespace.
			this.extensionPrefix = findUnusedNamespacePrefix(usedNamespacePrefixes, "ext");
			Worker.logger.debug("will use prefix {} for extension namespace", this.extensionPrefix);

			this.xmlReader = new PushbackXMLEventReader(
					StylesheetExtender.this.inputFactory.createXMLEventReader(new StringReader(originalStylesheet)));

			while (this.xmlReader.hasNext()) {
				XMLEvent event = this.xmlReader.nextEvent();
				this.currentElementID = Optional.empty();

				// processing BEFORE the event is written to the output stream
				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					startElement = processBeforeStartElement(startElement);
					// correct reference in case the object was exchanged
					event = startElement;
				}

				this.xmlWriter.add(event);

				// processing AFTER the event is written to the output stream
				if (event.isStartElement()) {
					final StartElement startElement = event.asStartElement();
					processAfterStartElement(startElement);
				}
				// TODO XSLT extension: support select expression variables for for-each

				// TODO XSLT extension: support condition tracing for if and when

				// TODO XSLT extension: support condition tracing

				// TODO XSLT extension: support condition tracing (match) for template

				// TODO XSLT extension: support template actual parameters for template

			}

			logger.traceExit();
		}

		/**
		 * Perform processing before a start element event is written to the output.
		 *
		 * @param startElement the start element event being processed
		 * @return the changed start element event
		 * @throws XMLStreamException
		 */
		private StartElement processBeforeStartElement(StartElement startElement) throws XMLStreamException {
			logger.traceEntry();
			if (!this.rootElementVisited) {
				// root element: add namespace
				startElement = extendRootElement(startElement, this.extensionPrefix);
				Worker.logger.debug("extended root element to {}", startElement);
			} else {
				// other elements: only handle certain XSLT elements
				if (startElement.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
					final String elementName = startElement.getName().getLocalPart();

					// add trace ID to element if required
					if (ELEMENTS_WITH_TRACE_BEFORE.contains(elementName)
							|| ELEMENTS_WITH_TRACE_AFTER.contains(elementName)) {
						this.currentElementID = Optional.of(getNextElementID());
						startElement = addIDToElement(startElement, this.extensionPrefix);
						Worker.logger.debug("added ID {} to element {}", this.currentElementID.get(), startElement);
					}

					// write trace message before element if required
					if (ELEMENTS_WITH_TRACE_BEFORE.contains(elementName)) {
						writeElementMessageDirect(startElement);
					}

					// TODO XSLT extension: support select expression variables for apply-templates

					// TODO XSLT extension: support template call parameters for apply-templates and
					// call-template
				}
			}
			return logger.traceExit(startElement);
		}

		/**
		 * Perform processing after a start element event has been written to the
		 * output.
		 *
		 * @param startElement the start element event being processed
		 * @return the changed start element event
		 * @throws XMLStreamException
		 */
		private StartElement processAfterStartElement(StartElement startElement) throws XMLStreamException {
			logger.traceEntry();
			// only handle certain XSL elements
			if (startElement.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
				final String elementName = startElement.getName().getLocalPart();

				// write trace message after element if required
				if (ELEMENTS_WITH_TRACE_AFTER.contains(elementName)) {
					writeElementMessageDelayed(startElement);
				}
			}
			return logger.traceExit(startElement);
		}

		/**
		 * Determine the namespace prefixes already in use throughout the document.
		 *
		 * @param originalStylesheet
		 * @return a set of all namespace prefixes used anywhere in the document
		 * @throws XMLStreamException
		 */
		private Set<String> collectNamespacePrefixes(String originalStylesheet) throws XMLStreamException {
			Worker.logger.traceEntry();
			final XMLEventReader scanningReader = StylesheetExtender.this.inputFactory
				.createXMLEventReader(new StringReader(originalStylesheet));
			final Set<String> prefixes = new HashSet<>();
			while (scanningReader.hasNext()) {
				final XMLEvent event = scanningReader.nextEvent();
				if (event.isStartElement()) {
					event.asStartElement().getNamespaces().forEachRemaining(ns -> {
						if (ns.getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
							this.xsltPrefix = ns.getPrefix();
						}
						final String prefix = ns.getPrefix();
						Worker.logger.debug("encountered namespace prefix {}", prefix);
						prefixes.add(prefix);
					});
				}
			}
			return logger.traceExit(prefixes);
		}

		/**
		 * Determine a unique prefix (startsWith + N) that does not collide with any of
		 * the existing prefixes.
		 *
		 * @param usedNamespacePrefixes a set of all namespace prefixes used anywhere in
		 *                              the document
		 * @param startsWith            a prefix of the prefix :-)
		 * @return a unique prefix
		 */
		private String findUnusedNamespacePrefix(Set<String> usedNamespacePrefixes, String startsWith) {
			int prefixNumber = 0;
			do {
				final String prefixCandidate = String.format("%s%s", startsWith, prefixNumber);
				if (!usedNamespacePrefixes.contains(prefixCandidate)) {
					return prefixCandidate;
				}
				prefixNumber += 1;
			} while (true);
		}

		/**
		 * Adds the extension namespace to the root element.
		 *
		 * @param originalElement    the original root element
		 * @param newExtensionPrefix the namespace prefix to use
		 * @return the extended root element
		 */
		private StartElement extendRootElement(StartElement originalElement, String newExtensionPrefix) {
			logger.traceEntry();
			final ArrayList<Namespace> namespaces = Lists.newArrayList(originalElement.getNamespaces());
			namespaces.add(this.eventFactory.createNamespace(newExtensionPrefix, ExtendedXSLTConstants.NAMESPACE));
			this.rootElementVisited = true;
			final StartElement result = this.eventFactory.createStartElement(originalElement.getName(),
					originalElement.getAttributes(), namespaces.iterator());
			return logger.traceExit(result);
		}

		/**
		 * Adds the trace ID to an element.
		 *
		 * @param originalElement the original element
		 * @param extensionPrefix the prefix of the extension namespace
		 * @return the extended element
		 */
		private StartElement addIDToElement(StartElement originalElement, String extensionPrefix) {
			logger.traceEntry();
			final ArrayList<Attribute> attributes = Lists.newArrayList(originalElement.getAttributes());
			attributes.add(this.eventFactory.createAttribute(extensionPrefix, ExtendedXSLTConstants.NAMESPACE,
					ExtendedXSLTConstants.Attributes.TRACE_ID, Integer.toString(this.currentElementID.get())));
			final StartElement result = this.eventFactory.createStartElement(originalElement.getName(),
					attributes.iterator(), originalElement.getNamespaces());
			return logger.traceExit(result);
		}

		/**
		 * Add a message element corresponding to an existing element, observing the
		 * rules to delay the message in case one of xsl:sort, xsl:param or
		 * xsl:with-param is encountered.
		 *
		 * @param referredElement the element the message refers to
		 * @throws XMLStreamException
		 */
		private void writeElementMessageDelayed(StartElement referredElement) throws XMLStreamException {
			logger.traceEntry();

			// read ahead to the next event that is not a whitespace-only character event,
			// storing the events in the process
			final Deque<XMLEvent> storedEvents = new LinkedList<>();
			XMLEvent nextEvent = this.xmlReader.peek();
			while (nextEvent.isCharacters() && nextEvent.asCharacters().isWhiteSpace()) {
				storedEvents.addFirst(this.xmlReader.nextEvent());
				nextEvent = this.xmlReader.peek();
			}

			// at this point, the next event could be...

			// ...a non-whitespace text node or an end event of the containing element
			// push back all events and write message immediately
			if (nextEvent.isCharacters() || nextEvent.isEndElement()) {
				storedEvents.forEach(e -> this.xmlReader.pushback(e));
				writeElementMessageDirect(referredElement);
			}

			// ...a start event of another element:
			else if (nextEvent.isStartElement()) {
				final StartElement startElement = nextEvent.asStartElement();

				if (ELEMENTS_DELAYING_MESSAGE.contains(startElement.getName().getLocalPart())) {
					// ...a start event of a delaying element
					// push all events up to and including the corresponding end event onto the
					// stack
					moveSubtreeToStack(storedEvents);

					// push the message events onto the stack
					for (final XMLEvent event : generateMessageEvents(referredElement)) {
						storedEvents.addFirst(event);
					}

					// push the entire stack back into the reader
					storedEvents.forEach(e -> this.xmlReader.pushback(e));
				} else {

					// ...a start element of another element: push back all events and write message
					// immediately
					storedEvents.forEach(e -> this.xmlReader.pushback(e));
					writeElementMessageDirect(referredElement);
				}
			} else {
				// any other event - don't know what to do yet
				Worker.logger.warn("Unclear whether to delay message output, check situation");
				// push back all events and write message immediately
				storedEvents.forEach(e -> this.xmlReader.pushback(e));
				writeElementMessageDirect(referredElement);
			}

			logger.traceExit();
		}

		/**
		 * Takes the elements from the XML Reader, starting with a start element event
		 * and ending with the corresponding end element event.
		 *
		 * @param storedEvents the stack to push the elements onto
		 * @throws XMLStreamException
		 */
		private void moveSubtreeToStack(Deque<XMLEvent> storedEvents) throws XMLStreamException {
			logger.traceEntry();
			int elementDepth = 0;
			do {
				final XMLEvent transferEvent = this.xmlReader.nextEvent();
				if (transferEvent.isStartElement()) {
					elementDepth++;
				} else if (transferEvent.isEndElement()) {
					elementDepth--;
				}
				storedEvents.addFirst(transferEvent);
			} while (elementDepth > 0);
			logger.traceExit();
		}

		/**
		 * Add a message element corresponding to an existing element directly
		 *
		 * @param referredElement the element the message refers to
		 * @throws XMLStreamException
		 */
		private void writeElementMessageDirect(StartElement referredElement) throws XMLStreamException {
			logger.traceEntry();
			for (final XMLEvent event : generateMessageEvents(referredElement)) {
				this.xmlWriter.add(event);
			}
			logger.traceExit();
		}

		/**
		 * Creates the message elements corresponding to an existing element
		 *
		 * @param referredElement the element the message refers to
		 * @throws XMLStreamException
		 */
		private Iterable<XMLEvent> generateMessageEvents(StartElement referredElement) {
			logger.traceEntry();
			final ArrayList<Attribute> traceAttributes = Lists.newArrayList(
					this.eventFactory.createAttribute(ExtendedXSLTConstants.Attributes.ELEMENT,
							referredElement.getName().getLocalPart()),
					this.eventFactory.createAttribute(ExtendedXSLTConstants.Attributes.TRACE_ID,
							this.currentElementID.get().toString()));
			final ArrayList<XMLEvent> result = Lists.newArrayList(
					// <xsl:message>
					this.eventFactory.createStartElement(this.xsltPrefix, XSLTConstants.NAMESPACE,
							XSLTConstants.Elements.MESSAGE),
					// <extN:trace element="foobar" id="X">
					this.eventFactory.createStartElement(this.extensionPrefix, ExtendedXSLTConstants.NAMESPACE,
							ExtendedXSLTConstants.Elements.TRACE, traceAttributes.iterator(),
							referredElement.getNamespaces()),
					// </extN:trace>
					this.eventFactory.createEndElement(this.extensionPrefix, ExtendedXSLTConstants.NAMESPACE,
							ExtendedXSLTConstants.Elements.TRACE),
					// </xsl:message>
					this.eventFactory.createEndElement(this.xsltPrefix, XSLTConstants.NAMESPACE,
							XSLTConstants.Elements.MESSAGE));
			return logger.traceExit(result);
		}

		/**
		 * @return the next trace ID to use for an element
		 */
		private int getNextElementID() {
			return this.nextElementID++;
		}

	}

}
