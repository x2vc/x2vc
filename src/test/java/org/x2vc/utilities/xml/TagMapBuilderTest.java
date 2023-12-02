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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.xml.ITagInfo.TagType;

@ExtendWith(MockitoExtension.class)
class TagMapBuilderTest {

	@Mock
	ITagMapFactory factory;

	@Captor
	ArgumentCaptor<List<ITagInfo>> tagListCaptor;

	@Mock
	ILocationMap locationMap;

	ITagMapBuilder builder;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.builder = new TagMapBuilder(this.factory);
		when(this.locationMap.getLocation(anyInt())).thenAnswer(invocation -> {
			final PolymorphLocation location = mock(PolymorphLocation.class);
			lenient().when(location.getCharacterOffset()).thenReturn(invocation.getArgument(0));
			lenient().when(location.compareTo(any(PolymorphLocation.class))).thenAnswer(invocation2 -> {
				return Integer.compare(invocation.getArgument(0),
						((PolymorphLocation) invocation2.getArgument(0)).getCharacterOffset());
			});
			return location;
		});
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMapBuilder#buildTagMap(java.lang.String)}.
	 */
	@Test
	void testBuildTagMap() {
		// offset...........00000000001111111.1112.222222222333333333344444444445555555555666666666677777777778888888888999999
		// offset:..........01234567890123456.7890.123456789012345678901234567890123456789012345678901234567890123456789012345
		final String xml = "<aaa>bbb<ccc ddd=\"eee\"/>fff<![CDATA[ggg<hhh/>iii]]>jjj<kkk>lll<mmm>nnn</mmm>ooo</kkk>ppp</aaa>";
		// tag_index:.........0.......1...............................................2.......3........4........5........6

		final ITagMap mockedMap = mock(ITagMap.class);
		when(this.factory.create(any())).thenReturn(mockedMap);

		final ITagMap actualMap = this.builder.buildTagMap(xml, this.locationMap);
		assertSame(mockedMap, actualMap);

		verify(this.factory).create(this.tagListCaptor.capture());
		final List<ITagInfo> tagList = this.tagListCaptor.getValue();
		assertNotNull(tagList);
		assertEquals(7, tagList.size());

		final ITagInfo tag0 = tagList.get(0);
		final ITagInfo tag1 = tagList.get(1);
		final ITagInfo tag2 = tagList.get(2);
		final ITagInfo tag3 = tagList.get(3);
		final ITagInfo tag4 = tagList.get(4);
		final ITagInfo tag5 = tagList.get(5);
		final ITagInfo tag6 = tagList.get(6);

		assertTypeAndLocation(ITagInfo.TagType.START, 0, 5, tag0, "starting tag <aaa>");
		assertTypeAndLocation(ITagInfo.TagType.EMPTY, 8, 24, tag1, "empty-element tag <ccc.../>");
		assertTypeAndLocation(ITagInfo.TagType.START, 54, 59, tag2, "starting tag <kkk>");
		assertTypeAndLocation(ITagInfo.TagType.START, 62, 67, tag3, "starting tag <mmm>");
		assertTypeAndLocation(ITagInfo.TagType.END, 70, 76, tag4, "ending tag </mmm>");
		assertTypeAndLocation(ITagInfo.TagType.END, 79, 85, tag5, "ending tag </kkk>");
		assertTypeAndLocation(ITagInfo.TagType.END, 88, 94, tag6, "ending tag </aaa>");

		assertSame(tag6, tag0.getEndTag().orElseThrow());
		assertSame(tag0, tag6.getStartTag().orElseThrow());

		assertSame(tag5, tag2.getEndTag().orElseThrow());
		assertSame(tag2, tag5.getStartTag().orElseThrow());

		assertSame(tag4, tag3.getEndTag().orElseThrow());
		assertSame(tag3, tag4.getStartTag().orElseThrow());
	}

	private void assertTypeAndLocation(TagType tagType, int startOffset, int endOffset, ITagInfo tag, String info) {
		assertEquals(tagType, tag.getType(), String.format("tag type of %s", info));
		assertEquals(startOffset, tag.getStartLocation().getCharacterOffset(),
				String.format("start offset of %s", info));
		assertEquals(endOffset, tag.getEndLocation().getCharacterOffset(),
				String.format("end offset of %s", info));
	}

}
