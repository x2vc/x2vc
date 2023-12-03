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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.x2vc.utilities.xml.ITagInfo.TagType;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class TagInfoTest {

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createEmptyTag(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateEmptyTag() {
		final PolymorphLocation startLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endLocation = mock(PolymorphLocation.class);
		when(startLocation.compareTo(endLocation)).thenReturn(-1);

		final ITagInfo tag = TagInfo.createEmptyTag(startLocation, endLocation);
		assertSame(startLocation, tag.getStartLocation());
		assertSame(endLocation, tag.getEndLocation());
		assertEquals(TagType.EMPTY, tag.getType());
		assertTrue(tag.isEmptyElement());
		assertFalse(tag.isStartTag());
		assertFalse(tag.isEndTag());
		assertTrue(tag.getStartTag().isEmpty());
		assertTrue(tag.getEndTag().isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createEmptyTag(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateEmptyTag_InvalidLocations() {
		final PolymorphLocation startLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endLocation = mock(PolymorphLocation.class);
		when(startLocation.compareTo(endLocation)).thenReturn(1); // wrong order
		assertThrows(IllegalArgumentException.class, () -> TagInfo.createEmptyTag(startLocation, endLocation));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createTagPair(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateTagPair() {
		final PolymorphLocation startTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation startTagEndLocation = mock(PolymorphLocation.class);
		when(startTagStartLocation.compareTo(startTagEndLocation)).thenReturn(-1);
		final PolymorphLocation endTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endTagEndLocation = mock(PolymorphLocation.class);
		when(endTagStartLocation.compareTo(endTagEndLocation)).thenReturn(-1);
		when(startTagEndLocation.compareTo(endTagStartLocation)).thenReturn(-1);

		final ITagInfo.Pair pair = TagInfo.createTagPair(startTagStartLocation, startTagEndLocation,
				endTagStartLocation, endTagEndLocation);

		assertSame(startTagStartLocation, pair.start().getStartLocation());
		assertSame(startTagEndLocation, pair.start().getEndLocation());
		assertEquals(TagType.START, pair.start().getType());
		assertFalse(pair.start().isEmptyElement());
		assertTrue(pair.start().isStartTag());
		assertFalse(pair.start().isEndTag());
		assertTrue(pair.start().getStartTag().isEmpty());
		assertSame(pair.end(), pair.start().getEndTag().orElseThrow());

		assertSame(endTagStartLocation, pair.end().getStartLocation());
		assertSame(endTagEndLocation, pair.end().getEndLocation());
		assertEquals(TagType.END, pair.end().getType());
		assertFalse(pair.end().isEmptyElement());
		assertFalse(pair.end().isStartTag());
		assertTrue(pair.end().isEndTag());
		assertSame(pair.start(), pair.end().getStartTag().orElseThrow());
		assertTrue(pair.end().getEndTag().isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createTagPair(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateTagPair_NoContent() {
		final PolymorphLocation startTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation startTagEndLocation = mock(PolymorphLocation.class);
		when(startTagStartLocation.compareTo(startTagEndLocation)).thenReturn(-1);
		final PolymorphLocation endTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endTagEndLocation = mock(PolymorphLocation.class);
		when(endTagStartLocation.compareTo(endTagEndLocation)).thenReturn(-1);
		// this simulates an empty tag, like <foo></foo> - here the startTagEndLocation and endTagStartLocation are
		// identical
		when(startTagEndLocation.compareTo(endTagStartLocation)).thenReturn(0);

		final ITagInfo.Pair pair = TagInfo.createTagPair(startTagStartLocation, startTagEndLocation,
				endTagStartLocation, endTagEndLocation);

		assertSame(startTagStartLocation, pair.start().getStartLocation());
		assertSame(startTagEndLocation, pair.start().getEndLocation());
		assertEquals(TagType.START, pair.start().getType());
		assertFalse(pair.start().isEmptyElement());
		assertTrue(pair.start().isStartTag());
		assertFalse(pair.start().isEndTag());
		assertTrue(pair.start().getStartTag().isEmpty());
		assertSame(pair.end(), pair.start().getEndTag().orElseThrow());

		assertSame(endTagStartLocation, pair.end().getStartLocation());
		assertSame(endTagEndLocation, pair.end().getEndLocation());
		assertEquals(TagType.END, pair.end().getType());
		assertFalse(pair.end().isEmptyElement());
		assertFalse(pair.end().isStartTag());
		assertTrue(pair.end().isEndTag());
		assertSame(pair.start(), pair.end().getStartTag().orElseThrow());
		assertTrue(pair.end().getEndTag().isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createTagPair(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateTagPair_InvalidStartTagLocation() {
		final PolymorphLocation startTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation startTagEndLocation = mock(PolymorphLocation.class);
		when(startTagStartLocation.compareTo(startTagEndLocation)).thenReturn(1); // wrong order
		final PolymorphLocation endTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endTagEndLocation = mock(PolymorphLocation.class);
		when(endTagStartLocation.compareTo(endTagEndLocation)).thenReturn(-1);
		when(startTagEndLocation.compareTo(endTagStartLocation)).thenReturn(-1);

		assertThrows(IllegalArgumentException.class, () -> TagInfo.createTagPair(
				startTagStartLocation, startTagEndLocation,
				endTagStartLocation, endTagEndLocation));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createTagPair(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateTagPair_InvalidEndTagLocation() {
		final PolymorphLocation startTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation startTagEndLocation = mock(PolymorphLocation.class);
		when(startTagStartLocation.compareTo(startTagEndLocation)).thenReturn(-1);
		final PolymorphLocation endTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endTagEndLocation = mock(PolymorphLocation.class);
		when(endTagStartLocation.compareTo(endTagEndLocation)).thenReturn(1); // wrong order
		when(startTagEndLocation.compareTo(endTagStartLocation)).thenReturn(-1);

		assertThrows(IllegalArgumentException.class, () -> TagInfo.createTagPair(
				startTagStartLocation, startTagEndLocation,
				endTagStartLocation, endTagEndLocation));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.xml.TagInfo#createTagPair(org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation, org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testCreateTagPair_InvalidTagLocation() {
		final PolymorphLocation startTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation startTagEndLocation = mock(PolymorphLocation.class);
		when(startTagStartLocation.compareTo(startTagEndLocation)).thenReturn(-1);
		final PolymorphLocation endTagStartLocation = mock(PolymorphLocation.class);
		final PolymorphLocation endTagEndLocation = mock(PolymorphLocation.class);
		when(endTagStartLocation.compareTo(endTagEndLocation)).thenReturn(-1);
		when(startTagEndLocation.compareTo(endTagStartLocation)).thenReturn(1); // invalid order

		assertThrows(IllegalArgumentException.class, () -> TagInfo.createTagPair(
				startTagStartLocation, startTagEndLocation,
				endTagStartLocation, endTagEndLocation));
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagInfo#equals(java.lang.Object)} and
	 * {@link org.x2vc.utilities.xml.TagInfo#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(TagInfo.class).suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
