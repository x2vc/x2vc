package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.IXMLElementType.ContentType;
import org.x2vc.schema.structure.IXMLElementType.ElementArrangement;
import org.x2vc.schema.structure.XMLElementType.Builder;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLElementTypeTest {

	@Test
	void testBuilderMinimal() {
		final XMLElementType elem = XMLElementType.builder()
			.withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.ALL)
			.build();
		assertEquals(0, elem.getAttributes().size());
		assertEquals(ContentType.ELEMENT, elem.getContentType());
		assertFalse(elem.hasDataContent());
		assertTrue(elem.hasElementContent());
		assertFalse(elem.hasMixedContent());
		assertThrows(IllegalStateException.class, () -> elem.getDataType());
		assertThrows(IllegalStateException.class, () -> elem.getMaxLength());
		assertThrows(IllegalStateException.class, () -> elem.getMinValue());
		assertThrows(IllegalStateException.class, () -> elem.getMaxValue());
		assertThrows(IllegalStateException.class, () -> elem.getDiscreteValues());
		assertThrows(IllegalStateException.class, () -> elem.isFixedValueset());
		assertEquals(0, elem.getElements().size());
		assertEquals(ElementArrangement.ALL, elem.getElementArrangement());
		assertThrows(IllegalStateException.class, () -> elem.isUserModifiable());
	}

	@Test
	void testBuilderWithComment() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.ELEMENT)
			.withComment("rhubarb").build();
		assertTrue(elem.getComment().isPresent());
		assertEquals("rhubarb", elem.getComment().get());
	}

	@Test
	void testBuilderWithUserModifiable() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING).withUserModifiable(true).build();
		assertTrue(elem.isUserModifiable().isPresent());
		assertTrue(elem.isUserModifiable().get());
	}

	@Test
	void testBuilderWithLengthRestrictedString() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING).withMaxLength(42).build();
		assertEquals(ContentType.DATA, elem.getContentType());
		assertEquals(XMLDataType.STRING, elem.getDataType());
		assertTrue(elem.getMaxLength().isPresent());
		assertEquals(42, elem.getMaxLength().get());

		assertThrows(IllegalStateException.class, () -> elem.getMinValue());
		assertThrows(IllegalStateException.class, () -> elem.getMaxValue());
	}

	@Test
	void testBuilderWithRestrictedInteger() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.INTEGER).withMinValue(1).withMaxValue(42).build();
		assertEquals(ContentType.DATA, elem.getContentType());
		assertEquals(XMLDataType.INTEGER, elem.getDataType());
		assertTrue(elem.getMinValue().isPresent());
		assertEquals(1, elem.getMinValue().get());
		assertTrue(elem.getMaxValue().isPresent());
		assertEquals(42, elem.getMaxValue().get());

		assertThrows(IllegalStateException.class, () -> elem.getMaxLength());
	}

	@Test
	void testBuilderWithAttribute() {
		final Builder builder = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING);
		XMLAttribute.builder("aName").withType(XMLDataType.OTHER).addTo(builder);
		final XMLElementType elem = builder.build();
		assertEquals(1, elem.getAttributes().size());
		assertEquals("aName", elem.getAttributes().toArray(new IXMLAttribute[0])[0].getName());
	}

	@Test
	void testBuilderWithDiscreteValue() {
		final Builder builder = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING);
		XMLDiscreteValue.builder().withStringValue("foobar").addTo(builder);
		final XMLElementType elem = builder.build();
		assertEquals(1, elem.getDiscreteValues().size());
		assertEquals("foobar", elem.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());
	}

	@Test
	void testBuilderCopyOfString() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDataType.OTHER).build();
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withStringValue("foobar").build();

		final XMLElementType elem = XMLElementType.builder()
			.withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING)
			.addAttribute(attrib)
			.addDiscreteValue(value).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem, true, true).build();

		assertEquals(ContentType.DATA, copy.getContentType());
		assertEquals(XMLDataType.STRING, copy.getDataType());
		assertEquals("foobar", copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());

		// attributes need to be copies, not the same objects reused
		assertNotSame(value, copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0]);
		// discrete values need to be copies, not the same objects reused
		assertNotSame(value, copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0]);
	}

	@Test
	void testBuilderCopyOfInteger() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.DATA)
			.withDataType(XMLDataType.INTEGER).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem, true, true).build();

		assertEquals(ContentType.DATA, copy.getContentType());
		assertEquals(XMLDataType.INTEGER, copy.getDataType());
	}

	@Test
	void testBuilderCopyOfElement() {
		final XMLElementType elem = XMLElementType.builder().withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.CHOICE).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem, true, true).build();

		assertEquals(ContentType.ELEMENT, copy.getContentType());
		assertEquals(ElementArrangement.CHOICE, copy.getElementArrangement());
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLElementType elem = XMLElementType.builder(id).withContentType(ContentType.MIXED).build();
		assertEquals(id, elem.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLElementType#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLElementType.class)
			.withRedefinedSuperclass()
			.verify();
	}

}
