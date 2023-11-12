package org.x2vc.stylesheet.structure;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.StringReader;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.XSLTConstants;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import net.sf.saxon.om.NamespaceUri;

/**
 * Standard implementation of {@link IStylesheetStructureExtractor}.
 */
public class StylesheetStructureExtractor implements IStylesheetStructureExtractor {

	private static final Logger logger = LogManager.getLogger();
	private XMLInputFactory inputFactory;

	/**
	 * Default constructor.
	 */
	@Inject
	public StylesheetStructureExtractor() {
		// nothing to do at the moment
	}

	@Override
	public IStylesheetStructure extractStructure(String source) {
		logger.traceEntry();
		if (this.inputFactory == null) {
			this.inputFactory = XMLInputFactory.newFactory();
			// have the reader combine adjacent text nodes
			this.inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		}
		try {
			final StylesheetStructure structure = new StylesheetStructure();
			final XMLEventReader xmlReader = this.inputFactory.createXMLEventReader(new StringReader(source));
			structure.setRootNode(new Worker(structure).process(xmlReader));
			return logger.traceExit(structure);
		} catch (final XMLStreamException e) {
			throw new IllegalArgumentException("Unable to analyze stylesheet structure.", e);
		}
	}

	/**
	 * This class is used for the actual implementation in order to ensure thread safety.
	 */
	private class Worker {

		private static final Logger logger = LogManager.getLogger();
		private Deque<INodeBuilder> builderChain = new ArrayDeque<>();
		private StylesheetStructure structure;
		private Set<String> namespacePrefixes = Sets.newHashSet();

		/**
		 * Creates a new worker instance.
		 *
		 * @param structure the structure object to populate
		 */
		public Worker(StylesheetStructure structure) {
			this.structure = structure;
		}

		/**
		 * Processes an extended stylesheet document and populates the structure information.
		 *
		 * @param xmlReader the input stream to read the extended stylesheet from
		 * @return the root node of the structure extracted from the stylesheet
		 * @throws XMLStreamException
		 */
		public IXSLTDirectiveNode process(XMLEventReader xmlReader) throws XMLStreamException {
			logger.traceEntry();
			while (xmlReader.hasNext()) {
				final XMLEvent event = xmlReader.nextEvent();
				switch (event.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					logger.trace("processing START_ELEMENT event: {}", event);
					processStartElement(event.asStartElement());
					break;

				case XMLStreamConstants.END_ELEMENT:
					logger.trace("processing END_ELEMENT event: {}", event);
					processEndElement(event.asEndElement());
					break;

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					logger.warn("processing PROCESSING_INSTRUCTION event ignored: {}", event);
					// TODO #24 XSLT structure extraction: decide how to handle processing instructions
					break;

				case XMLStreamConstants.CHARACTERS:
					logger.trace("processing CHARACTERS event: {}", event);
					processCharacterEvent(event.asCharacters());
					break;

				case XMLStreamConstants.COMMENT:
					logger.trace("ignoring COMMENT event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.SPACE:
					logger.trace("ignoring SPACE event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.START_DOCUMENT:
					logger.trace("ignoring START_DOCUMENT event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.END_DOCUMENT:
					logger.trace("processing END_DOCUMENT event: {}", event);
					if (this.builderChain.size() != 1) {
						logger.warn("expected a single remaining element at end of document, but {} elements remained",
								this.builderChain.size());
					}
					final IXSLTDirectiveNode result = ((XSLTDirectiveNode.Builder) this.builderChain
						.getFirst()).build();
					return logger.traceExit(result);

				case XMLStreamConstants.ENTITY_REFERENCE:
					logger.warn("ignoring ENTITY_REFERENCE event: {}", event);
					// TODO #27 XSLT structure extraction: decide how to handle entity references
					// (never seen them occur, though)
					break;

				case XMLStreamConstants.ATTRIBUTE:
					logger.warn("ignoring ATTRIBUTE event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.DTD:
					// TODO #27 XSLT structure extraction: decide how to handle entity references
					logger.warn("ignoring DTD event: {}", event);
					break;

				case XMLStreamConstants.CDATA:
					logger.trace("ignoring CDATA event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.NAMESPACE:
					logger.warn("ignoring NAMESPACE event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.NOTATION_DECLARATION:
					// TODO #27 XSLT structure extraction: decide how to handle notation declarations
					logger.warn("ignoring NOTATION_DECLARATION event: {}", event);
					break;

				case XMLStreamConstants.ENTITY_DECLARATION:
					// TODO #27 XSLT structure extraction: decide how to handle entity declarations
					logger.warn("ignoring ENTITY_DECLARATION event: {}", event);
					break;

				default:
					logger.warn("ignoring unknown event type {}: {}", event.getEventType(), event);
				}
			}
			throw logger
				.throwing(new IllegalArgumentException("End of event stream reached before end of document event"));
		}

		/**
		 * Processes a start element event to dispatch according to element type.
		 *
		 * @param element
		 */
		private void processStartElement(StartElement element) {
			logger.traceEntry();

			// ensure all namespace prefixes are recorded
			element.getNamespaces().forEachRemaining(ns -> this.namespacePrefixes.add(ns.getPrefix()));

			// is this an XSLT element?
			if (element.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
				// yes - what kind of element?
				switch (element.getName().getLocalPart()) {
				case XSLTConstants.Elements.PARAM:
					processStartOfParameter(element);
					break;
				case XSLTConstants.Elements.WITH_PARAM:
					processStartOfParameter(element);
					break;
				case XSLTConstants.Elements.SORT:
					processStartOfSort(element);
					break;
				default:
					processStartOfDirective(element);
				}
			} else {
				// no - another XML element
				processStartOfXMLNode(element);
			}

			logger.traceExit();
		}

		/**
		 * Processes a start element event for an xsl:param or xsl:with-param element.
		 *
		 * @param element
		 */
		private void processStartOfParameter(StartElement element) {
			logger.traceEntry();
			final Optional<String> oAttribName = getAttributeValue(element, "name");
			if (oAttribName.isEmpty()) {
				throw logger
					.throwing(new IllegalArgumentException("Parameter element without name attribute encountered."));
			}
			final String attribName = oAttribName.get();

			// the attribute name may be a QName - let's see whether we have to pick this apart
			String localName = null;
			String namespaceURI = null;
			final List<String> nameParts = Splitter.on(":").splitToList(attribName);
			if (nameParts.size() == 1) {
				localName = nameParts.get(0);
			} else {
				localName = nameParts.get(1);
				namespaceURI = element.getNamespaceURI(nameParts.get(0));
				if (namespaceURI == null) {
					// no namespace found - revert to old behaviour
					localName = attribName;
				}
			}

			final Optional<String> attribSelect = getAttributeValue(element, "select");
			logger.trace("start of parameter ({}) {}", element.getName().getLocalPart(), localName);
			final XSLTParameterNode.Builder paramBuilder = XSLTParameterNode.builder(this.structure,
					localName);
			if (namespaceURI != null) {
				paramBuilder.withNamespaceURI(namespaceURI);
			}
			paramBuilder.withStartLocation(element.getLocation());
			if (attribSelect.isPresent()) {
				paramBuilder.withSelection(attribSelect.get());
			}
			this.builderChain.add(paramBuilder);
			logger.traceExit();
		}

		/**
		 * Processes a start element event for an xsl:sort element.
		 *
		 * @param element
		 */
		private void processStartOfSort(StartElement element) {
			logger.traceEntry();
			logger.trace("start of sort specification ({})", element.getName().getLocalPart());
			final Optional<String> attribSelect = getAttributeValue(element, "select");
			final Optional<String> attribLang = getAttributeValue(element, "lang");
			final Optional<String> attribDataType = getAttributeValue(element, "data-type");
			final Optional<String> attribOrder = getAttributeValue(element, "order");
			final Optional<String> attribCaseOrder = getAttributeValue(element, "case-order");
			final XSLTSortNode.Builder sortBuilder = XSLTSortNode.builder(this.structure);
			sortBuilder.withStartLocation(element.getLocation());
			if (attribSelect.isPresent()) {
				sortBuilder.withSortingExpression(attribSelect.get());
			}
			if (attribLang.isPresent()) {
				sortBuilder.withLanguage(attribLang.get());
			}
			if (attribDataType.isPresent()) {
				sortBuilder.withDataType(attribDataType.get());
			}
			if (attribOrder.isPresent()) {
				sortBuilder.withSortOrder(attribOrder.get());
			}
			if (attribCaseOrder.isPresent()) {
				sortBuilder.withCaseOrder(attribCaseOrder.get());
			}
			this.builderChain.add(sortBuilder);
			logger.traceExit();
		}

		/**
		 * Processes a start element event for any other XSLT directive.
		 *
		 * @param element
		 */
		private void processStartOfDirective(StartElement element) {
			logger.traceEntry();
			final String elementName = element.getName().getLocalPart();
			logger.trace("start of XSLT directive {}", elementName);
			final XSLTDirectiveNode.Builder directiveBuilder = XSLTDirectiveNode.builder(this.structure,
					elementName);
			directiveBuilder.withStartLocation(element.getLocation());

			// add the namespaces defined up to this element
			final NamespaceContext namespaceContext = element.getNamespaceContext();
			for (final String prefix : this.namespacePrefixes) {
				final String namespaceUri = namespaceContext.getNamespaceURI(prefix);
				if ((namespaceUri != null) && !namespaceUri.equals(XMLConstants.NULL_NS_URI)) {
					directiveBuilder.withNamespace(prefix, NamespaceUri.of(namespaceUri));
				}
			}

			for (final Iterator<Attribute> iterator = element.getAttributes(); iterator.hasNext();) {
				final Attribute attrib = iterator.next();
				final String attribNamespace = attrib.getName().getNamespaceURI();
				if (attribNamespace.equals(XSLTConstants.NAMESPACE) || Strings.isNullOrEmpty(attribNamespace)) {
					directiveBuilder.addXSLTAttribute(attrib.getName().getLocalPart(), attrib.getValue());
				} else {
					directiveBuilder.addOtherAttribute(attrib.getName(), attrib.getValue());
				}
			}
			this.builderChain.add(directiveBuilder);
			logger.traceExit();
		}

		/**
		 * Processes a start element event for any other XML node.
		 *
		 * @param element
		 */
		private void processStartOfXMLNode(StartElement element) {
			logger.traceEntry();
			logger.trace("start of XML node {}", element.getName());
			final XMLNode.Builder nodeBuilder = XMLNode.builder(this.structure, element.getName());
			nodeBuilder.withStartLocation(element.getLocation());
			for (final Iterator<Attribute> iterator = element.getAttributes(); iterator.hasNext();) {
				final Attribute attrib = iterator.next();
				nodeBuilder.addAttribute(attrib.getName(), attrib.getValue());
			}
			this.builderChain.add(nodeBuilder);
			logger.traceExit();
		}

		/**
		 * Processes an end element event to dispatch according to element type.
		 *
		 * @param element
		 */
		private void processEndElement(EndElement element) {
			logger.traceEntry();
			// is this an XSLT element?
			if (element.getName().getNamespaceURI().equals(XSLTConstants.NAMESPACE)) {
				// yes - what kind of element?
				switch (element.getName().getLocalPart()) {
				case XSLTConstants.Elements.PARAM:
					processEndOfParameter(element);
					break;
				case XSLTConstants.Elements.WITH_PARAM:
					processEndOfParameter(element);
					break;
				case XSLTConstants.Elements.SORT:
					processEndOfSort(element);
					break;
				default:
					processEndOfDirective(element);
				}
			} else {
				// no - another XML element
				processEndOfXMLNode(element);
			}
			logger.traceExit();
		}

		/**
		 * Processes an end element event for an xsl:param or xsl:with-param element.
		 *
		 * @param element
		 */
		private void processEndOfParameter(EndElement element) {
			logger.traceEntry();
			final XSLTParameterNode.Builder paramBuilder = (XSLTParameterNode.Builder) this.builderChain.removeLast();
			paramBuilder.withEndLocation(element.getLocation());
			final XSLTParameterNode paramNode = paramBuilder.build();
			logger.trace("end of parameter {}", paramNode.getQualifiedName());

			final INodeBuilder parentBuilder = this.builderChain.getLast();
			// parameters may only occur directly beneath an XSLT element
			if (element.getName().getLocalPart().equals(XSLTConstants.Elements.PARAM)) {
				((XSLTDirectiveNode.Builder) parentBuilder).addFormalParameter(paramNode);
			} else {
				((XSLTDirectiveNode.Builder) parentBuilder).addActualParameter(paramNode);
			}
			logger.traceExit();
		}

		/**
		 * Processes an end element event for an xsl:sort element.
		 *
		 * @param element
		 */
		private void processEndOfSort(EndElement element) {
			logger.traceEntry();
			final XSLTSortNode.Builder sortBuilder = (XSLTSortNode.Builder) this.builderChain.removeLast();
			sortBuilder.withEndLocation(element.getLocation());
			final XSLTSortNode sortNode = sortBuilder.build();

			final INodeBuilder parentBuilder = this.builderChain.getLast();
			// sort specifications may only occur directly beneath an XSLT element
			((XSLTDirectiveNode.Builder) parentBuilder).addSorting(sortNode);
			logger.traceExit();
		}

		/**
		 * Processes an end element event for any other XSLT directive.
		 *
		 * @param element
		 */
		private void processEndOfDirective(EndElement element) {
			logger.traceEntry();
			if (this.builderChain.size() > 1) {
				final XSLTDirectiveNode.Builder directiveBuilder = (XSLTDirectiveNode.Builder) this.builderChain
					.removeLast();
				directiveBuilder.withEndLocation(element.getLocation());
				final IXSLTDirectiveNode directiveNode = directiveBuilder.build();
				logger.trace("end of XSLT directive {}", directiveNode.getName());
				addChildNodeToLastBuilder(directiveNode);
			} else {
				// store the end location, but leave the last builder in the queue to be
				// processed during the end-of-document event
				final XSLTDirectiveNode.Builder directiveBuilder = (XSLTDirectiveNode.Builder) this.builderChain
					.getLast();
				directiveBuilder.withEndLocation(element.getLocation());

			}
			logger.traceExit();
		}

		/**
		 * Processes an end element event for any other XML node.
		 *
		 * @param element
		 */
		private void processEndOfXMLNode(EndElement element) {
			logger.traceEntry();
			final XMLNode.Builder nodeBuilder = (XMLNode.Builder) this.builderChain.removeLast();
			nodeBuilder.withEndLocation(element.getLocation());
			final XMLNode node = nodeBuilder.build();
			addChildNodeToLastBuilder(node);
			logger.traceExit();
		}

		/**
		 * Processes a character (text) event.
		 *
		 * @param characters
		 */
		private void processCharacterEvent(Characters characters) {
			logger.traceEntry();
			// for this tree, ignore whitespace-only nodes
			if (!characters.isWhiteSpace()) {
				final TextNode node = TextNode.builder(this.structure).withText(characters.getData()).build();
				logger.trace("processing text of length {}", node.getText().length());
				addChildNodeToLastBuilder(node);
			}
			logger.traceExit();
		}

		/**
		 * Adds the node as a child node to the last builder in the chain
		 *
		 * @param node the child node to add
		 */
		private void addChildNodeToLastBuilder(IStructureTreeNode node) {
			logger.traceEntry();
			final INodeBuilder parentBuilder = this.builderChain.getLast();
			if (parentBuilder instanceof final XMLNode.Builder nodeBuilder) {
				nodeBuilder.addChildElement(node);
			} else if (parentBuilder instanceof final XSLTDirectiveNode.Builder directiveBuilder) {
				directiveBuilder.addChildElement(node);
			} else if (parentBuilder instanceof final XSLTParameterNode.Builder paramBuilder) {
				paramBuilder.addChildElement(node);
			} else {
				throw logger
					.throwing(new IllegalStateException("The child element cannot be added to this parent element"));
			}
			logger.traceExit();
		}

		/**
		 * Retrieves the value of an XSLT attribute. This method will first try to look for a fully prefixed attribute
		 * name (which is rather unusual) and then fallback to an attribute without a namespace prefix.
		 *
		 * @param element       the element to examine
		 * @param attributeName the name of the attribute to retrieve
		 * @return an {@link Optional} containing the value or nothing if the attribute was not found
		 */
		private Optional<String> getAttributeValue(StartElement element, String attributeName) {
			Attribute attrib = element.getAttributeByName(new QName(XSLTConstants.NAMESPACE, attributeName));
			if (attrib == null) {
				attrib = element.getAttributeByName(new QName(XMLConstants.NULL_NS_URI, attributeName));
			}
			if (attrib == null) {
				return Optional.empty();
			} else {
				return Optional.ofNullable(attrib.getValue());
			}
		}

	}

}
