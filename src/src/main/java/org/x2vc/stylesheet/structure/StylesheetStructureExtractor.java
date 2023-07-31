package org.x2vc.stylesheet.structure;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.XSLTConstants;

import com.google.common.base.Strings;

/**
 * Standard implementation of {@link IStylesheetStructureExtractor}.
 */
public class StylesheetStructureExtractor implements IStylesheetStructureExtractor {

	private Logger logger = LogManager.getLogger();
	private XMLInputFactory inputFactory;

	/**
	 * Default constructor.
	 */
	public StylesheetStructureExtractor() {
		this.inputFactory = XMLInputFactory.newFactory();
		// have the reader combine adjacent text nodes
		this.inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
	}

	@Override
	public IStylesheetStructure extractStructure(String source) {
		try {
			this.logger.info("begin of stylesheet structure extraction");
			StylesheetStructure structure = new StylesheetStructure();
			XMLEventReader xmlReader = this.inputFactory.createXMLEventReader(new StringReader(source));
			// the actual processing is performed in a separate worker class to facilitate
			// thread safety
			structure.setRootNode(new Worker(structure).process(xmlReader));
			this.logger.info("end of stylesheet structure extraction");
			return structure;
		} catch (XMLStreamException e) {
			throw new IllegalArgumentException("Unable to analyze stylesheet structure.", e);
		}
	}

	private class Worker {

		private Logger logger = LogManager.getLogger();
		private Deque<INodeBuilder> builderChain = new ArrayDeque<>();
		private StylesheetStructure structure;

		/**
		 * @param structure
		 */
		public Worker(StylesheetStructure structure) {
			this.structure = structure;
		}

		/**
		 * @param xmlReader
		 * @return the root node of the structure extracted from the stylesheet
		 * @throws XMLStreamException
		 */
		public IXSLTDirectiveNode process(XMLEventReader xmlReader) throws XMLStreamException {
			while (xmlReader.hasNext()) {
				XMLEvent event = xmlReader.nextEvent();
				switch (event.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					this.logger.trace("processing START_ELEMENT event: {}", event);
					processStartElement(event.asStartElement());
					break;

				case XMLStreamConstants.END_ELEMENT:
					this.logger.trace("processing END_ELEMENT event: {}", event);
					processEndElement(event.asEndElement());
					break;

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					this.logger.warn("processing PROCESSING_INSTRUCTION event ignored: {}", event);
					// TODO XSLT structure extraction: decide how to handle processing instructions
					break;

				case XMLStreamConstants.CHARACTERS:
					this.logger.trace("processing CHARACTERS event: {}", event);
					processCharacterEvent(event.asCharacters());
					break;

				case XMLStreamConstants.COMMENT:
					this.logger.trace("ignoring COMMENT event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.SPACE:
					this.logger.trace("ignoring SPACE event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.START_DOCUMENT:
					this.logger.trace("ignoring START_DOCUMENT event: {}", event);
					// nothing to do here
					break;

				case XMLStreamConstants.END_DOCUMENT:
					this.logger.trace("processing END_DOCUMENT event: {}", event);
					if (this.builderChain.size() != 1) {
						this.logger.warn(
								"expected a single remaining element at end of document, but {} elements remained",
								this.builderChain.size());
					}
					return ((XSLTDirectiveNode.Builder) this.builderChain.getFirst()).build();

				case XMLStreamConstants.ENTITY_REFERENCE:
					this.logger.warn("ignoring ENTITY_REFERENCE event: {}", event);
					// TODO XSLT structure extraction: decide how to handle entity references
					// (never seen them occur, though)
					break;

				case XMLStreamConstants.ATTRIBUTE:
					this.logger.warn("ignoring ATTRIBUTE event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.DTD:
					// TODO XSLT structure extraction: decide how to handle entity references
					this.logger.warn("ignoring DTD event: {}", event);
					break;

				case XMLStreamConstants.CDATA:
					this.logger.trace("ignoring CDATA event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.NAMESPACE:
					this.logger.warn("ignoring NAMESPACE event: {}", event);
					// should not be produced by this parser, issue warning just in case
					break;

				case XMLStreamConstants.NOTATION_DECLARATION:
					// TODO XSLT structure extraction: decide how to handle notation declarations
					this.logger.warn("ignoring NOTATION_DECLARATION event: {}", event);
					break;

				case XMLStreamConstants.ENTITY_DECLARATION:
					// TODO XSLT structure extraction: decide how to handle entity declarations
					this.logger.warn("ignoring ENTITY_DECLARATION event: {}", event);
					break;

				default:
					this.logger.warn("ignoring unknown event type {}: {}", event.getEventType(), event);
				}
			}
			throw new IllegalArgumentException("End of event stream reached before end of document event");
		}

		/**
		 * @param element
		 */
		private void processStartElement(StartElement element) {
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
		}

		/**
		 * @param element
		 */
		private void processStartOfParameter(StartElement element) {
			Optional<String> attribName = getAttributeValue(element, "name");
			if (attribName.isEmpty()) {
				throw new IllegalArgumentException("Parameter element without name attribute encountered.");
			}
			Optional<String> attribSelect = getAttributeValue(element, "select");
			this.logger.trace("start of parameter ({}) {}", element.getName().getLocalPart(), attribName.get());
			XSLTParameterNode.Builder paramBuilder = new XSLTParameterNode.Builder(this.structure, attribName.get());
			if (attribSelect.isPresent()) {
				paramBuilder.withSelection(attribSelect.get());
			}
			this.builderChain.add(paramBuilder);
		}

		/**
		 * @param element
		 */
		private void processStartOfSort(StartElement element) {
			Optional<String> attribSelect = getAttributeValue(element, "select");
			Optional<String> attribLang = getAttributeValue(element, "lang");
			Optional<String> attribDataType = getAttributeValue(element, "data-type");
			Optional<String> attribOrder = getAttributeValue(element, "order");
			Optional<String> attribCaseOrder = getAttributeValue(element, "case-order");
			this.logger.trace("start of sort specification ({})", element.getName().getLocalPart());
			XSLTSortNode.Builder sortBuilder = new XSLTSortNode.Builder(this.structure);
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
		}

		/**
		 * @param element
		 */
		private void processStartOfDirective(StartElement element) {
			String elementName = element.getName().getLocalPart();
			this.logger.trace("start of XSLT directive {}", elementName);
			XSLTDirectiveNode.Builder directiveBuilder = new XSLTDirectiveNode.Builder(this.structure, elementName);
			for (Iterator<Attribute> iterator = element.getAttributes(); iterator.hasNext();) {
				Attribute attrib = iterator.next();
				String attribNamespace = attrib.getName().getNamespaceURI();
				if (attribNamespace.equals(XSLTConstants.NAMESPACE) || Strings.isNullOrEmpty(attribNamespace)) {
					directiveBuilder.addXSLTAttribute(attrib.getName().getLocalPart(), attrib.getValue());
				} else {
					directiveBuilder.addOtherAttribute(attrib.getName(), attrib.getValue());
				}
			}
			this.builderChain.add(directiveBuilder);
		}

		/**
		 * @param element
		 */
		private void processStartOfXMLNode(StartElement element) {
			this.logger.trace("start of XML node {}", element.getName());
			XMLNode.Builder nodeBuilder = new XMLNode.Builder(this.structure, element.getName());
			for (Iterator<Attribute> iterator = element.getAttributes(); iterator.hasNext();) {
				Attribute attrib = iterator.next();
				nodeBuilder.addAttribute(attrib.getName(), attrib.getValue());
			}
			this.builderChain.add(nodeBuilder);
		}

		/**
		 * @param element
		 */
		private void processEndElement(EndElement element) {
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
					processEndOfSort();
					break;
				default:
					processEndOfDirective();
				}
			} else {
				// no - another XML element
				processEndOfXMLNode();
			}

		}

		/**
		 * @param element
		 */
		private void processEndOfParameter(EndElement element) {
			XSLTParameterNode.Builder paramBuilder = (XSLTParameterNode.Builder) this.builderChain.removeLast();
			XSLTParameterNode paramNode = paramBuilder.build();
			this.logger.trace("end of parameter {}", paramNode.getName());

			INodeBuilder parentBuilder = this.builderChain.getLast();
			// parameters may only occur directly beneath an XSLT element
			if (element.getName().getLocalPart().equals(XSLTConstants.Elements.PARAM)) {
				((XSLTDirectiveNode.Builder) parentBuilder).addFormalParameter(paramNode);
			} else {
				((XSLTDirectiveNode.Builder) parentBuilder).addActualParameter(paramNode);
			}
		}

		/**
		 * @param element
		 */
		private void processEndOfSort() {
			XSLTSortNode.Builder sortBuilder = (XSLTSortNode.Builder) this.builderChain.removeLast();
			XSLTSortNode sortNode = sortBuilder.build();
			this.logger.trace("end of sort specification");

			INodeBuilder parentBuilder = this.builderChain.getLast();
			// sort specifications may only occur directly beneath an XSLT element
			((XSLTDirectiveNode.Builder) parentBuilder).addSorting(sortNode);
		}

		/**
		 * @param element
		 */
		private void processEndOfDirective() {
			// leave the last builder in the queue to be processed during the
			// end-of-document event
			if (this.builderChain.size() > 1) {
				XSLTDirectiveNode.Builder directiveBuilder = (XSLTDirectiveNode.Builder) this.builderChain.removeLast();
				XSLTDirectiveNode directiveNode = directiveBuilder.build();
				this.logger.trace("end of XSLT directive {}", directiveNode.getName());
				addChildNodeToLastBuilder(directiveNode);
			}
		}

		/**
		 * @param element
		 */
		private void processEndOfXMLNode() {
			XMLNode.Builder nodeBuilder = (XMLNode.Builder) this.builderChain.removeLast();
			XMLNode node = nodeBuilder.build();
			this.logger.trace("end of XML node {}", node.getName());
			addChildNodeToLastBuilder(node);
		}

		/**
		 * @param characters
		 */
		private void processCharacterEvent(Characters characters) {
			// for this tree, ignore whitespace-only nodes
			if (!characters.isWhiteSpace()) {
				TextNode node = new TextNode.Builder(this.structure).withText(characters.getData()).build();
				this.logger.trace("processing text of length {}", node.getText().length());
				addChildNodeToLastBuilder(node);
			}
		}

		/**
		 * @param node
		 */
		private void addChildNodeToLastBuilder(IStructureTreeNode node) {
			INodeBuilder parentBuilder = this.builderChain.getLast();
			if (parentBuilder instanceof XMLNode.Builder nodeBuilder) {
				nodeBuilder.addChildElement(node);
			}
			if (parentBuilder instanceof XSLTDirectiveNode.Builder directiveBuilder) {
				directiveBuilder.addChildElement(node);
			}
			if (parentBuilder instanceof XSLTParameterNode.Builder paramBuilder) {
				paramBuilder.addChildElement(node);
			}
		}

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
