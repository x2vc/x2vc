package org.x2vc.utilities;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Wrapper for an {@link XMLEventReader} that allows for pushback of elements
 * into the reader queue
 */
public class PushbackXMLEventReader implements IPushbackXMLEventReader {

	private XMLEventReader wrappedReader;
	private Deque<XMLEvent> eventStack;

	/**
	 * Create a new pushback reader wrapping the specified {@link XMLEventReader}.
	 *
	 * @param wrappedReader
	 */
	public PushbackXMLEventReader(XMLEventReader wrappedReader) {
		this.wrappedReader = wrappedReader;
		this.eventStack = new LinkedList<XMLEvent>();
	}

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		if (this.eventStack.isEmpty()) {
			return this.wrappedReader.nextEvent();
		} else {
			return this.eventStack.removeLast();
		}
	}

	@Override
	public Object next() {
		try {
			return nextEvent();
		} catch (XMLStreamException streamException) {
			// don't swallow the cause
			NoSuchElementException e = new NoSuchElementException(streamException.getMessage());
			e.initCause(streamException.getCause());
			throw e;
		}
	}

	@Override
	public boolean hasNext() {
		return (!this.eventStack.isEmpty() || this.wrappedReader.hasNext());
	}

	@Override
	public XMLEvent peek() throws XMLStreamException {
		if (this.eventStack.isEmpty()) {
			return this.wrappedReader.peek();
		} else {
			return this.eventStack.getLast();
		}
	}

	@Override
	public String getElementText() throws XMLStreamException {
		// TODO PushbackXMLEventReader: add support for getElementText()
		throw new UnsupportedOperationException("PushbackXMLEventReader does not support getElementText() yet");
	}

	@Override
	public XMLEvent nextTag() throws XMLStreamException {
		// TODO PushbackXMLEventReader: add support for nextTag()
		throw new UnsupportedOperationException("PushbackXMLEventReader does not support nextTag() yet");
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return this.wrappedReader.getProperty(name);
	}

	@Override
	public void close() throws XMLStreamException {
		this.wrappedReader.close();
		this.eventStack.clear();
	}

	@Override
	public void pushback(XMLEvent event) {
		this.eventStack.addLast(event);
	}

}
