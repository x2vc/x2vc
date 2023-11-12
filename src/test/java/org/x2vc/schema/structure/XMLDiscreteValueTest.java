package org.x2vc.schema.structure;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLDiscreteValueTest {

	@Test
	void testBuilderForString() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withStringValue("foo").build();

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDataType.STRING, value.getDataType());
		assertEquals("foo", value.asString());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderForBoolean() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withBooleanValue(true).build();

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDataType.BOOLEAN, value.getDataType());
		assertEquals(true, value.asBoolean());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asString());
	}

	@Test
	void testBuilderForInteger() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withIntegerValue(42).build();

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDataType.INTEGER, value.getDataType());
		assertEquals(42, value.asInteger());

		assertThrows(IllegalStateException.class, () -> value.asString());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderForOther() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().build();

		assertFalse(value.getComment().isPresent());
		assertEquals(XMLDataType.OTHER, value.getDataType());

		assertThrows(IllegalStateException.class, () -> value.asInteger());
		assertThrows(IllegalStateException.class, () -> value.asString());
		assertThrows(IllegalStateException.class, () -> value.asBoolean());
	}

	@Test
	void testBuilderWithComment() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withIntegerValue(42).withComment("rhubarb")
			.build();

		assertTrue(value.getComment().isPresent());
		assertEquals("rhubarb", value.getComment().get());
	}

	@Test
	void testBuilderWithID() {
		final UUID id = UUID.randomUUID();
		final XMLDiscreteValue value = XMLDiscreteValue.builder(id).withIntegerValue(42).build();
		assertEquals(id, value.getID());
	}

	@Test
	void testBuilderOfCopy() {
		final XMLDiscreteValue value = XMLDiscreteValue.builder().withStringValue("foo").build();
		final XMLDiscreteValue copy = XMLDiscreteValue.builderFrom(value).withComment("rhubarb").build();

		assertTrue(copy.getComment().isPresent());
		assertEquals("rhubarb", copy.getComment().get());

		assertEquals(XMLDataType.STRING, copy.getDataType());
		assertEquals("foo", copy.asString());

		assertThrows(IllegalStateException.class, () -> copy.asInteger());
		assertThrows(IllegalStateException.class, () -> copy.asBoolean());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLDiscreteValue#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLDiscreteValue.class)
			.withRedefinedSuperclass()
			.verify();
	}

}
