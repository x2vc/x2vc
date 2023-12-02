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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import nl.jqno.equalsverifier.EqualsVerifier;

class TagMapTest {

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMap#getTag(org.x2vc.utilities.xml.PolymorphLocation)}.
	 */
	@Test
	void testGetTag() {
		final ITagInfo tag1 = mockTag(10, 20);
		final ITagInfo tag2 = mockTag(30, 40);
		final ITagInfo tag3 = mockTag(50, 60);
		final ArrayList<ITagInfo> tags = Lists.newArrayList(tag1, tag2, tag3);
		final TagMap map = new TagMap(tags);
		assertTrue(map.getTag(mockLocation(5)).isEmpty());
		assertEquals(tag1, map.getTag(mockLocation(10)).orElseThrow());
		assertEquals(tag1, map.getTag(mockLocation(19)).orElseThrow());
		assertTrue(map.getTag(mockLocation(20)).isEmpty());
		assertTrue(map.getTag(mockLocation(29)).isEmpty());
		assertEquals(tag2, map.getTag(mockLocation(30)).orElseThrow());
		assertEquals(tag2, map.getTag(mockLocation(39)).orElseThrow());
		assertTrue(map.getTag(mockLocation(40)).isEmpty());
		assertTrue(map.getTag(mockLocation(49)).isEmpty());
		assertEquals(tag3, map.getTag(mockLocation(50)).orElseThrow());
		assertEquals(tag3, map.getTag(mockLocation(59)).orElseThrow());
		assertTrue(map.getTag(mockLocation(60)).isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMap#getTag(org.x2vc.utilities.xml.PolymorphLocation, int)}.
	 */
	@Test
	void testGetTagWithOffset() {
		final ITagInfo tag1 = mockTag(10, 20);
		final ITagInfo tag2 = mockTag(30, 40);
		final ITagInfo tag3 = mockTag(50, 60);
		final ArrayList<ITagInfo> tags = Lists.newArrayList(tag1, tag2, tag3);
		final TagMap map = new TagMap(tags);
		assertTrue(map.getTag(mockLocation(6), -1).isEmpty());
		assertEquals(tag1, map.getTag(mockLocation(11), -1).orElseThrow());
		assertEquals(tag1, map.getTag(mockLocation(20), -1).orElseThrow());
		assertTrue(map.getTag(mockLocation(22), -2).isEmpty());
		assertTrue(map.getTag(mockLocation(31), -2).isEmpty());
		assertEquals(tag2, map.getTag(mockLocation(30), 0).orElseThrow());
		assertEquals(tag2, map.getTag(mockLocation(39), 0).orElseThrow());
		assertTrue(map.getTag(mockLocation(39), 1).isEmpty());
		assertTrue(map.getTag(mockLocation(48), 1).isEmpty());
		assertEquals(tag3, map.getTag(mockLocation(48), 2).orElseThrow());
		assertEquals(tag3, map.getTag(mockLocation(57), 2).orElseThrow());
		assertTrue(map.getTag(mockLocation(30), 30).isEmpty());
	}

	/**
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	private ITagInfo mockTag(int startOffset, int endOffset) {
		final ITagInfo tag = mock(ITagInfo.class);
		final PolymorphLocation startLocation = mockLocation(startOffset);
		when(tag.getStartLocation()).thenReturn(startLocation);
		final PolymorphLocation endLocation = mockLocation(endOffset);
		when(tag.getEndLocation()).thenReturn(endLocation);
		return tag;
	}

	/**
	 * @param offset
	 * @return
	 */
	private PolymorphLocation mockLocation(int offset) {
		final PolymorphLocation location = mock(PolymorphLocation.class);
		when(location.getCharacterOffset()).thenReturn(offset);
		return location;
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMap#equals(java.lang.Object)} and
	 * {@link org.x2vc.utilities.xml.TagMap#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(TagMap.class).verify();
	}

}
