package org.x2vc.utilities;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

/**
 * Utility to convert the output of {@link XMLEvent#getEventType()} to a
 * user-readable text.
 */
public class XMLEventTypeFormatter {

	private XMLEventTypeFormatter() {
	}

	/**
	 * @param eventType an event type as returned by {@link XMLEvent#getEventType()}
	 * @return the corresponding constant name
	 */
	public static String toString(int eventType) {
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

}
