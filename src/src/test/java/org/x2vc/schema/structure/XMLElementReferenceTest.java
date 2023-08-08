package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.IXMLElementType.ContentType;

class XMLElementReferenceTest {

	@Test
	void testBuilderMinimal() {
		final XMLElementType innerElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = new XMLElementReference.Builder("aName", innerElem).build();
		assertFalse(ref.isAttribute());
		assertFalse(ref.isElement());
		assertTrue(ref.isReference());
		assertFalse(ref.isValue());
		assertFalse(ref.getComment().isPresent());
		assertSame(innerElem, ref.getElement());
		assertEquals(innerElem.getID(), ref.getElementID());
		assertEquals(0, ref.getMinOccurrence());
		assertFalse(ref.getMaxOccurrence().isPresent());
	}

	@Test
	void testBuilderWithComment() {
		final XMLElementType innerElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = new XMLElementReference.Builder("aName", innerElem).withComment("rhubarb")
			.build();
		assertTrue(ref.getComment().isPresent());
		assertEquals("rhubarb", ref.getComment().get());
	}

	@Test
	void testBuilderWithOccurrence() {
		final XMLElementType innerElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = new XMLElementReference.Builder("aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();
		assertEquals(1, ref.getMinOccurrence());
		assertTrue(ref.getMaxOccurrence().isPresent());
		assertEquals(42, ref.getMaxOccurrence().get());
	}

	@Test
	void testBuilderCopyOf() {

		final XMLElementType innerElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = new XMLElementReference.Builder("aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();

		final XMLElementReference copy = XMLElementReference.builderFrom(ref).build();

		// copied element has to be in "incomplete" state
		assertThrows(IllegalStateException.class, () -> copy.getElement());
		assertEquals(innerElem.getID(), copy.getElementID());

		// attempt to connect with other element that has different ID has to fail
		final XMLElementType otherElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		assertThrows(IllegalArgumentException.class, () -> copy.fixElementReference(otherElem));

		// connect with correct element
		copy.fixElementReference(innerElem);
		assertSame(innerElem, ref.getElement());

		// attempt to connect with other element has to fail now
		assertThrows(IllegalStateException.class, () -> copy.fixElementReference(innerElem));
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLElementType innerElem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = new XMLElementReference.Builder(id, "aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();
		assertEquals(id, ref.getID());
	}

}
