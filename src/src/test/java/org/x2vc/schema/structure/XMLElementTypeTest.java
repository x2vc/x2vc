package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.IXMLElementType.ContentType;
import org.x2vc.schema.structure.IXMLElementType.ElementArrangement;
import org.x2vc.schema.structure.XMLElementType.Builder;

class XMLElementTypeTest {

	@Test
	void testBuilderMinimal() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT).build();
		assertFalse(elem.isAttribute());
		assertTrue(elem.isElement());
		assertFalse(elem.isReference());
		assertFalse(elem.isValue());
		assertEquals(0, elem.getAttributes().size());
		assertEquals(ContentType.ELEMENT, elem.getContentType());
		assertFalse(elem.hasDataContent());
		assertTrue(elem.hasElementContent());
		assertFalse(elem.hasMixedContent());
		assertThrows(IllegalStateException.class, () -> elem.getDatatype());
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
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT)
			.withComment("rhubarb").build();
		assertTrue(elem.getComment().isPresent());
		assertEquals("rhubarb", elem.getComment().get());
	}

	@Test
	void testBuilderWithUserModifiable() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).withUserModifiable(true).build();
		assertTrue(elem.isUserModifiable().isPresent());
		assertTrue(elem.isUserModifiable().get());
	}

	@Test
	void testBuilderWithLengthRestrictedString() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).withMaxLength(42).build();
		assertEquals(ContentType.DATA, elem.getContentType());
		assertEquals(XMLDatatype.STRING, elem.getDatatype());
		assertTrue(elem.getMaxLength().isPresent());
		assertEquals(42, elem.getMaxLength().get());

		assertThrows(IllegalArgumentException.class, () -> elem.getMinValue());
		assertThrows(IllegalArgumentException.class, () -> elem.getMaxValue());
	}

	@Test
	void testBuilderWithRestrictedInteger() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.INTEGER).withMinValue(1).withMaxValue(42).build();
		assertEquals(ContentType.DATA, elem.getContentType());
		assertEquals(XMLDatatype.INTEGER, elem.getDatatype());
		assertTrue(elem.getMinValue().isPresent());
		assertEquals(1, elem.getMinValue().get());
		assertTrue(elem.getMaxValue().isPresent());
		assertEquals(42, elem.getMaxValue().get());

		assertThrows(IllegalArgumentException.class, () -> elem.getMaxLength());
	}

	@Test
	void testBuilderWithAttribute() {
		final Builder builder = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING);
		new XMLAttribute.Builder("aName").withType(XMLDatatype.OTHER).addTo(builder);
		final XMLElementType elem = builder.build();
		assertEquals(1, elem.getAttributes().size());
		assertEquals("aName", elem.getAttributes().toArray(new IXMLAttribute[0])[0].getName());
	}

	@Test
	void testBuilderWithDiscreteValue() {
		final Builder builder = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING);
		new XMLDiscreteValue.Builder().withStringValue("foobar").addTo(builder);
		final XMLElementType elem = builder.build();
		assertEquals(1, elem.getDiscreteValues().size());
		assertEquals("foobar", elem.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());
	}

	@Test
	void testBuilderCopyOfString() {
		final XMLAttribute attrib = new XMLAttribute.Builder("aName").withType(XMLDatatype.OTHER).build();
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withStringValue("foobar").build();

		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).addAttribute(attrib).addDiscreteValue(value).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem).build();

		assertEquals(ContentType.DATA, copy.getContentType());
		assertEquals(XMLDatatype.STRING, copy.getDatatype());
		assertEquals("foobar", copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());

		// attributes need to be copies, not the same objects reused
		assertNotSame(value, copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0]);
		// discrete values need to be copies, not the same objects reused
		assertNotSame(value, copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0]);
	}

	@Test
	void testBuilderCopyOfInteger() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.INTEGER).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem).build();

		assertEquals(ContentType.DATA, copy.getContentType());
		assertEquals(XMLDatatype.INTEGER, copy.getDatatype());
	}

	@Test
	void testBuilderCopyOfElement() {
		final XMLElementType elem = new XMLElementType.Builder().withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.CHOICE).build();

		final XMLElementType copy = XMLElementType.builderFrom(elem).build();

		assertEquals(ContentType.ELEMENT, copy.getContentType());
		assertEquals(ElementArrangement.CHOICE, copy.getElementArrangement());
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLElementType elem = new XMLElementType.Builder(id).withContentType(ContentType.MIXED).build();
		assertEquals(id, elem.getID());
	}

}
