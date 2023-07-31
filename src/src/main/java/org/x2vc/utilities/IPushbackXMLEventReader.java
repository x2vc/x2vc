package org.x2vc.utilities;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

/**
 *
 */
public interface IPushbackXMLEventReader extends XMLEventReader {

	/**
	 * Pushes an element back into the reader. This element will be returned as next
	 * element when the reader is read. This method can be called multiple times to
	 * push back any number of events.
	 *
	 * @param event
	 */
	void pushback(XMLEvent event);

}
