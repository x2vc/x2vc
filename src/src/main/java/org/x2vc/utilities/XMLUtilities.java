package org.x2vc.utilities;

import java.io.StringWriter;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Utility to perform various XML-related tasks.
 */
public class XMLUtilities {

	private static final Logger logger = LogManager.getLogger();

	private XMLUtilities() {
	}

	/**
	 * Converts the output of {@link XMLEvent#getEventType()} to a user-readable
	 * text.
	 *
	 * @param eventType an event type as returned by {@link XMLEvent#getEventType()}
	 * @return the corresponding constant name
	 */
	public static String convertEventTypeToString(int eventType) {
		switch (eventType) {
		case XMLStreamConstants.START_ELEMENT:
			return "START_ELEMENT";
		case XMLStreamConstants.END_ELEMENT:
			return "END_ELEMENT";
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			return "PROCESSING_INSTRUCTION";
		case XMLStreamConstants.CHARACTERS:
			return "CHARACTERS";
		case XMLStreamConstants.COMMENT:
			return "COMMENT";
		case XMLStreamConstants.SPACE:
			return "SPACE";
		case XMLStreamConstants.START_DOCUMENT:
			return "START_DOCUMENT";
		case XMLStreamConstants.END_DOCUMENT:
			return "END_DOCUMENT";
		case XMLStreamConstants.ENTITY_REFERENCE:
			return "ENTITY_REFERENCE";
		case XMLStreamConstants.ATTRIBUTE:
			return "ATTRIBUTE";
		case XMLStreamConstants.DTD:
			return "DTD";
		case XMLStreamConstants.CDATA:
			return "CDATA";
		case XMLStreamConstants.NAMESPACE:
			return "NAMESPACE";
		case XMLStreamConstants.NOTATION_DECLARATION:
			return "NOTATION_DECLARATION";
		case XMLStreamConstants.ENTITY_DECLARATION:
			return "ENTITY_DECLARATION";
		default:
			return String.format("UNKNOWN(%d)", eventType);
		}
	}

	/**
	 * Format an XML document using DOM4J.
	 *
	 * @param xmlString           the XML contents to format
	 * @param formatConfiguration a callback to configure the target format if so
	 *                            desired
	 * @return the formatted XML
	 */
	public static String prettyPrint(String xmlString, Consumer<OutputFormat> formatConfiguration) {
		logger.traceEntry();
		try {
			final OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			formatConfiguration.accept(format);
			final org.dom4j.Document document = DocumentHelper.parseText(xmlString);
			final StringWriter sw = new StringWriter();
			final XMLWriter writer = new XMLWriter(sw, format);
			writer.write(document);
			return logger.traceExit(sw.toString());
		} catch (final Exception e) {
			throw logger.throwing(new RuntimeException("Error occurs when pretty-printing XML", e));
		}
	}

}
