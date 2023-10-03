package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.IElementType.ContentType;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLElementReferenceTest {

	@Test
	void testBuilderMinimal() {
		final XMLElementType innerElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = XMLElementReference.builder("aName", innerElem).build();
		assertFalse(ref.getComment().isPresent());
		assertSame(innerElem, ref.getElement());
		assertEquals(innerElem.getID(), ref.getElementID());
		assertEquals(0, ref.getMinOccurrence());
		assertFalse(ref.getMaxOccurrence().isPresent());
	}

	@Test
	void testBuilderWithComment() {
		final XMLElementType innerElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = XMLElementReference.builder("aName", innerElem).withComment("rhubarb")
			.build();
		assertTrue(ref.getComment().isPresent());
		assertEquals("rhubarb", ref.getComment().get());
	}

	@Test
	void testBuilderWithOccurrence() {
		final XMLElementType innerElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = XMLElementReference.builder("aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();
		assertEquals(1, ref.getMinOccurrence());
		assertTrue(ref.getMaxOccurrence().isPresent());
		assertEquals(42, ref.getMaxOccurrence().get());
	}

	@Test
	void testBuilderCopyOf() {

		final XMLElementType innerElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = XMLElementReference.builder("aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();

		final XMLElementReference copy = XMLElementReference.builderFrom(ref).build();

		// copied element has to be in "incomplete" state
		assertThrows(IllegalStateException.class, () -> copy.getElement());
		assertEquals(innerElem.getID(), copy.getElementID());

		// attempt to connect with other element that has different ID has to fail
		final XMLElementType otherElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
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
		final XMLElementType innerElem = XMLElementType.builder().withContentType(ContentType.ELEMENT).build();
		final XMLElementReference ref = XMLElementReference.builder(id, "aName", innerElem).withMinOccurrence(1)
			.withMaxOccurrence(42).build();
		assertEquals(id, ref.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLElementReference#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLElementReference.class)
			.withRedefinedSuperclass()
			.verify();
	}

}
