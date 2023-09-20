package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.XMLAttribute.Builder;

class XMLAttributeTest {

	@Test
	void testBuilderMinimal() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.OTHER).build();
		assertTrue(attrib.isAttribute());
		assertFalse(attrib.isElement());
		assertFalse(attrib.isReference());
		assertFalse(attrib.isValue());
		assertEquals("aName", attrib.getName());
		assertEquals(XMLDatatype.OTHER, attrib.getDatatype());
		assertFalse(attrib.getComment().isPresent());
		assertFalse(attrib.isOptional());
		assertThrows(IllegalStateException.class, () -> attrib.getMaxLength());
		assertThrows(IllegalStateException.class, () -> attrib.getMinValue());
		assertThrows(IllegalStateException.class, () -> attrib.getMaxValue());

	}

	@Test
	void testBuilderWithComment() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withComment("rhubarb").build();
		assertTrue(attrib.getComment().isPresent());
		assertEquals("rhubarb", attrib.getComment().get());
	}

	@Test
	void testBuilderWithOptional() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withOptional(true).build();
		assertTrue(attrib.isOptional());
	}

	@Test
	void testBuilderUnrestrictedString() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.STRING).build();
		assertEquals(XMLDatatype.STRING, attrib.getDatatype());
		assertFalse(attrib.getMaxLength().isPresent());
	}

	@Test
	void testBuilderLengthRestrictedString() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.STRING).withMaxLength(42)
			.build();
		assertEquals(XMLDatatype.STRING, attrib.getDatatype());
		assertTrue(attrib.getMaxLength().isPresent());
		assertEquals(42, attrib.getMaxLength().get());
	}

	@Test
	void testBuilderUnrestrictedInteger() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.INTEGER).build();
		assertEquals(XMLDatatype.INTEGER, attrib.getDatatype());
		assertFalse(attrib.getMinValue().isPresent());
		assertFalse(attrib.getMaxValue().isPresent());
	}

	@Test
	void testBuilderRestrictedInteger() {
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.INTEGER).withMinValue(1)
			.withMaxValue(42).build();
		assertEquals(XMLDatatype.INTEGER, attrib.getDatatype());
		assertTrue(attrib.getMinValue().isPresent());
		assertEquals(1, attrib.getMinValue().get());
		assertTrue(attrib.getMaxValue().isPresent());
		assertEquals(42, attrib.getMaxValue().get());
	}

	@Test
	void testBuilderWithDiscreteValue() {
		final Builder builder = XMLAttribute.builder("aName").withType(XMLDatatype.STRING);
		XMLDiscreteValue.builder().withStringValue("foobar").addTo(builder);
		final XMLAttribute attrib = builder.build();
		assertEquals(1, attrib.getDiscreteValues().size());
		assertEquals("foobar", attrib.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());
	}

	@Test
	void testBuilderCopyOf() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withStringValue("foobar").build();
		final XMLAttribute attrib = XMLAttribute.builder("aName").withType(XMLDatatype.STRING).withMaxLength(42)
			.addDiscreteValue(value).build();

		final XMLAttribute copy = XMLAttribute.builderFrom(attrib).build();

		assertEquals(XMLDatatype.STRING, copy.getDatatype());
		assertTrue(copy.getMaxLength().isPresent());
		assertEquals(42, copy.getMaxLength().get());
		assertEquals(1, copy.getDiscreteValues().size());
		assertEquals("foobar", copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0].asString());

		// discrete values need to be copies, not the same objects reused
		assertNotSame(value, copy.getDiscreteValues().toArray(new IXMLDiscreteValue[0])[0]);
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLAttribute attrib = XMLAttribute.builder(id, "aName").withType(XMLDatatype.OTHER).build();
		assertEquals(id, attrib.getID());
	}
}