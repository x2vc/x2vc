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
package org.x2vc.utilities.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 */
class PushbackXMLEventReaderTest {

	IPushbackXMLEventReader reader;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		final XMLInputFactory factory = XMLInputFactory.newFactory();
		factory.setProperty(XMLInputFactory.IS_COALESCING, true);
		final String xmlSource = """
									<?xml version="1.0"?>
									<a><aa/><ab/></a>
									""";
		final XMLEventReader wrappedReader = factory.createXMLEventReader(new StringReader(xmlSource));
		this.reader = new PushbackXMLEventReader(wrappedReader);
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#nextEvent()}.
	 *
	 * @throws XMLStreamException
	 */
	@Test
	void testNextEvent() throws XMLStreamException {
		// retrieve event 1 - should be the start of document
		final XMLEvent evStartDocument = this.reader.nextEvent();
		assertEquals(XMLStreamConstants.START_DOCUMENT, evStartDocument.getEventType());

		// retrieve event 2 - should be start of <a>
		final XMLEvent evStartA = this.reader.nextEvent();
		assertTrue(evStartA.isStartElement());
		assertEquals("a", evStartA.asStartElement().getName().getLocalPart());

		// retrieve event 3 - should be start of <aa/>
		final XMLEvent evStartAA = this.reader.nextEvent();
		assertTrue(evStartAA.isStartElement());
		assertEquals("aa", evStartAA.asStartElement().getName().getLocalPart());

		// retrieve event 4 - should be end of <aa/>
		final XMLEvent evEndAA = this.reader.nextEvent();
		assertTrue(evEndAA.isEndElement());
		assertEquals("aa", evEndAA.asEndElement().getName().getLocalPart());

		// retrieve event 5 - should be start of <ab/>
		final XMLEvent evStartAB = this.reader.nextEvent();
		assertTrue(evStartAB.isStartElement());
		assertEquals("ab", evStartAB.asStartElement().getName().getLocalPart());

		// retrieve event 6 - should be end of <ab/>
		final XMLEvent evEndAB = this.reader.nextEvent();
		assertTrue(evEndAB.isEndElement());
		assertEquals("ab", evEndAB.asEndElement().getName().getLocalPart());

		// retrieve event 7 - should be end of <a/>
		final XMLEvent evEndA = this.reader.nextEvent();
		assertTrue(evEndA.isEndElement());
		assertEquals("a", evEndA.asEndElement().getName().getLocalPart());

		// retrieve event 8 - should be the end of document
		final XMLEvent evEndDocument = this.reader.nextEvent();
		assertEquals(XMLStreamConstants.END_DOCUMENT, evEndDocument.getEventType());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#nextEvent()}.
	 *
	 * @throws XMLStreamException
	 */
	@Test
	void testNextEventWithPushback() throws XMLStreamException {
		// retrieve event 1 - should be the start of document
		this.reader.nextEvent();

		// retrieve event 2 - should be start of <a>
		final XMLEvent evStartA1 = this.reader.nextEvent();

		// push back event 2 and try to read again
		this.reader.pushback(evStartA1);
		final XMLEvent evStartA2 = this.reader.nextEvent();
		assertEquals(evStartA1, evStartA2);

		// retrieve event 3 - should be start of <aa/>
		final XMLEvent evStartAA1 = this.reader.nextEvent();

		// retrieve event 4 - should be end of <aa/>
		final XMLEvent evEndAA1 = this.reader.nextEvent();

		// retrieve event 5 - should be start of <ab/>
		final XMLEvent evStartAB1 = this.reader.nextEvent();

		// retrieve event 6 - should be end of <ab/>
		final XMLEvent evEndAB1 = this.reader.nextEvent();

		// push back 4 events
		this.reader.pushback(evEndAB1);
		this.reader.pushback(evStartAB1);
		this.reader.pushback(evEndAA1);
		this.reader.pushback(evStartAA1);

		// read again and compare

		final XMLEvent evStartAA2 = this.reader.nextEvent();
		assertEquals(evStartAA1, evStartAA2);

		final XMLEvent evEndAA2 = this.reader.nextEvent();
		assertEquals(evEndAA1, evEndAA2);

		final XMLEvent evStartAB2 = this.reader.nextEvent();
		assertEquals(evStartAB1, evStartAB2);

		final XMLEvent evEndAB2 = this.reader.nextEvent();
		assertEquals(evEndAB1, evEndAB2);

	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#next()}.
	 */
	@Test
	void testNext() {
		// retrieve event 1 - should be the start of document
		final XMLEvent evStartDocument = (XMLEvent) this.reader.next();
		assertEquals(XMLStreamConstants.START_DOCUMENT, evStartDocument.getEventType());

		// retrieve event 2 - should be start of <a>
		final XMLEvent evStartA = (XMLEvent) this.reader.next();
		assertTrue(evStartA.isStartElement());
		assertEquals("a", evStartA.asStartElement().getName().getLocalPart());

		// retrieve event 3 - should be start of <aa/>
		final XMLEvent evStartAA = (XMLEvent) this.reader.next();
		assertTrue(evStartAA.isStartElement());
		assertEquals("aa", evStartAA.asStartElement().getName().getLocalPart());

		// retrieve event 4 - should be end of <aa/>
		final XMLEvent evEndAA = (XMLEvent) this.reader.next();
		assertTrue(evEndAA.isEndElement());
		assertEquals("aa", evEndAA.asEndElement().getName().getLocalPart());

		// retrieve event 5 - should be start of <ab/>
		final XMLEvent evStartAB = (XMLEvent) this.reader.next();
		assertTrue(evStartAB.isStartElement());
		assertEquals("ab", evStartAB.asStartElement().getName().getLocalPart());

		// retrieve event 6 - should be end of <ab/>
		final XMLEvent evEndAB = (XMLEvent) this.reader.next();
		assertTrue(evEndAB.isEndElement());
		assertEquals("ab", evEndAB.asEndElement().getName().getLocalPart());

		// retrieve event 7 - should be end of <a/>
		final XMLEvent evEndA = (XMLEvent) this.reader.next();
		assertTrue(evEndA.isEndElement());
		assertEquals("a", evEndA.asEndElement().getName().getLocalPart());

		// retrieve event 8 - should be the end of document
		final XMLEvent evEndDocument = (XMLEvent) this.reader.next();
		assertEquals(XMLStreamConstants.END_DOCUMENT, evEndDocument.getEventType());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#hasNext()}.
	 */
	@Test
	void testHasNext() {
		assertTrue(this.reader.hasNext());

		// retrieve event 1 - should be the start of document
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 2 - should be start of <a>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 3 - should be start of <aa/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 4 - should be end of <aa/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 5 - should be start of <ab/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 6 - should be end of <ab/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 7 - should be end of <a/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 8 - should be the end of document
		this.reader.next();
		assertFalse(this.reader.hasNext());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#hasNext()}.
	 *
	 * @throws XMLStreamException
	 */
	@Test
	void testHasNextWithPushback() throws XMLStreamException {
		assertTrue(this.reader.hasNext());

		// retrieve event 1 - should be the start of document
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 2 - should be start of <a>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 3 - should be start of <aa/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 4 - should be end of <aa/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 5 - should be start of <ab/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 6 - should be end of <ab/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 7 - should be end of <a/>
		this.reader.next();
		assertTrue(this.reader.hasNext());

		// retrieve event 8 - should be the end of document
		final XMLEvent lastEvent = this.reader.nextEvent();
		assertFalse(this.reader.hasNext());

		// push back last element
		this.reader.pushback(lastEvent);
		assertTrue(this.reader.hasNext());

		// and remove again
		this.reader.next();
		assertFalse(this.reader.hasNext());

	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#peek()}.
	 *
	 * @throws XMLStreamException
	 */
	@Test
	void testPeek() throws XMLStreamException {
		// retrieve event 1 - should be the start of document
		final XMLEvent evStartDocument1 = this.reader.peek();
		final XMLEvent evStartDocument2 = this.reader.nextEvent();
		assertEquals(evStartDocument1, evStartDocument2);

		// retrieve event 2 - should be start of <a>
		final XMLEvent evStartA1 = this.reader.peek();
		final XMLEvent evStartA2 = this.reader.nextEvent();
		assertEquals(evStartA1, evStartA2);

		// retrieve event 3 - should be start of <aa/>
		final XMLEvent evStartAA1 = this.reader.peek();
		final XMLEvent evStartAA2 = this.reader.nextEvent();
		assertEquals(evStartAA1, evStartAA2);

		// retrieve event 4 - should be end of <aa/>
		final XMLEvent evEndAA1 = this.reader.peek();
		final XMLEvent evEndAA2 = this.reader.nextEvent();
		assertEquals(evEndAA1, evEndAA2);

		// retrieve event 5 - should be start of <ab/>
		final XMLEvent evStartAB1 = this.reader.peek();
		final XMLEvent evStartAB2 = this.reader.nextEvent();
		assertEquals(evStartAB1, evStartAB2);

		// retrieve event 6 - should be end of <ab/>
		final XMLEvent evEndAB1 = this.reader.peek();
		final XMLEvent evEndAB2 = this.reader.nextEvent();
		assertEquals(evEndAB1, evEndAB2);

		// retrieve event 7 - should be end of <a/>
		final XMLEvent evEndA1 = this.reader.peek();
		final XMLEvent evEndA2 = this.reader.nextEvent();
		assertEquals(evEndA1, evEndA2);

		// retrieve event 8 - should be the end of document
		final XMLEvent evEndDocument1 = this.reader.peek();
		final XMLEvent evEndDocument2 = this.reader.nextEvent();
		assertEquals(evEndDocument1, evEndDocument2);

		// no more events, should return null
		assertNull(this.reader.peek());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.PushbackXMLEventReader#peek()}.
	 *
	 * @throws XMLStreamException
	 */
	@Test
	void testPeekWithPushback() throws XMLStreamException {
		// retrieve event 1 - should be the start of document
		this.reader.nextEvent();

		// retrieve event 2 - should be start of <a>
		final XMLEvent evStartA1 = this.reader.peek();
		final XMLEvent evStartA2 = this.reader.nextEvent();
		assertEquals(evStartA1, evStartA2);

		// push event back
		this.reader.pushback(evStartA1);

		// and read again
		final XMLEvent evStartA3 = this.reader.peek();
		final XMLEvent evStartA4 = this.reader.nextEvent();
		assertEquals(evStartA1, evStartA3);
		assertEquals(evStartA1, evStartA4);
	}

}
