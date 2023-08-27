package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class XMLDiscreteValueTest {

	@Test
	void testBuilderForString() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withStringValue("foo").build();
		assertFalse(value.isAttribute());
		assertFalse(value.isElement());
		assertFalse(value.isReference());
		assertTrue(value.isValue());

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDatatype.STRING, value.getDatatype());
		assertEquals("foo", value.asString());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderForBoolean() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withBooleanValue(true).build();
		assertFalse(value.isAttribute());
		assertFalse(value.isElement());
		assertFalse(value.isReference());
		assertTrue(value.isValue());

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDatatype.BOOLEAN, value.getDatatype());
		assertEquals(true, value.asBoolean());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asString());
	}

	@Test
	void testBuilderForInteger() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withIntegerValue(42).build();
		assertFalse(value.isAttribute());
		assertFalse(value.isElement());
		assertFalse(value.isReference());
		assertTrue(value.isValue());

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDatatype.INTEGER, value.getDatatype());
		assertEquals(42, value.asInteger());

		assertThrows(IllegalStateException.class, () -> value.asString());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderForOther() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().build();
		assertFalse(value.isAttribute());
		assertFalse(value.isElement());
		assertFalse(value.isReference());
		assertTrue(value.isValue());

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDatatype.OTHER, value.getDatatype());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asString());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderWithComment() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withIntegerValue(42).withComment("rhubarb")
			.build();
		assertTrue(value.isValue());

		assertTrue(value.getComment().isPresent());
		assertEquals("rhubarb", value.getComment().get());
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder(id).withIntegerValue(42).build();
		assertEquals(id, value.getID());
	}

	@Test
	void testBuilderOfCopy() {
		final XMLDiscreteValue value = new XMLDiscreteValue.Builder().withStringValue("foo").build();
		final XMLDiscreteValue copy = XMLDiscreteValue.builderFrom(value).withComment("rhubarb").build();

		assertFalse(copy.isAttribute());
		assertFalse(copy.isElement());
		assertFalse(copy.isReference());
		assertTrue(copy.isValue());

		assertTrue(copy.getComment().isPresent());
		assertEquals("rhubarb", copy.getComment().get());

		assertEquals(XMLDatatype.STRING, copy.getDatatype());
		assertEquals("foo", copy.asString());

		assertThrows(IllegalStateException.class, () -> copy.asInteger());
		assertThrows(IllegalStateException.class, () -> copy.asBoolean());
	}

}
