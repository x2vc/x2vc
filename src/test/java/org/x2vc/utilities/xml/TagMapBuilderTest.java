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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
	void testBuildTagMap_SingleLine() {
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

		assertTagsAssigned(tag0, tag6, "<aaa>");
		assertTagsAssigned(tag2, tag5, "<kkk>");
		assertTagsAssigned(tag3, tag4, "<mmm>");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMapBuilder#buildTagMap(java.lang.String)}.
	 */
	@Test
	void testBuildTagMap_MultiLine() {
		final String xml = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="foo">
							<xsl:choose>
							<xsl:when test="fooA">
							<p>test A</p>
							</xsl:when>
							<xsl:when test="fooB">
							<p>test B</p>
							</xsl:when>
							<xsl:otherwise>
							<foo bar="baz" />
							</xsl:otherwise>
							</xsl:choose>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagMap mockedMap = mock(ITagMap.class);
		when(this.factory.create(any())).thenReturn(mockedMap);

		final ITagMap actualMap = this.builder.buildTagMap(xml, this.locationMap);
		assertSame(mockedMap, actualMap);

		verify(this.factory).create(this.tagListCaptor.capture());
		final List<ITagInfo> tagList = this.tagListCaptor.getValue();
		assertNotNull(tagList);
		assertEquals(17, tagList.size());

		final ITagInfo tag0 = tagList.get(0);
		final ITagInfo tag1 = tagList.get(1);
		final ITagInfo tag2 = tagList.get(2);
		final ITagInfo tag3 = tagList.get(3);
		final ITagInfo tag4 = tagList.get(4);
		final ITagInfo tag5 = tagList.get(5);
		final ITagInfo tag6 = tagList.get(6);
		final ITagInfo tag7 = tagList.get(7);
		final ITagInfo tag8 = tagList.get(8);
		final ITagInfo tag9 = tagList.get(9);
		final ITagInfo tag10 = tagList.get(10);
		final ITagInfo tag11 = tagList.get(11);
		final ITagInfo tag12 = tagList.get(12);
		final ITagInfo tag13 = tagList.get(13);
		final ITagInfo tag14 = tagList.get(14);
		final ITagInfo tag15 = tagList.get(15);
		final ITagInfo tag16 = tagList.get(16);

		assertTypeAndLocation(ITagInfo.TagType.START, 22, 101, tag0, "starting tag <xsl:stylesheet...>");
		assertTypeAndLocation(ITagInfo.TagType.START, 102, 127, tag1, "starting tag <xsl:template name=\"foo\">");
		assertTypeAndLocation(ITagInfo.TagType.START, 128, 140, tag2, "starting tag <xsl:choose>");
		assertTypeAndLocation(ITagInfo.TagType.START, 141, 163, tag3, "<xsl:when test=\"fooA\">");
		assertTypeAndLocation(ITagInfo.TagType.START, 164, 167, tag4, "starting tag <p>");
		assertTypeAndLocation(ITagInfo.TagType.END, 173, 177, tag5, "ending tag </p>");
		assertTypeAndLocation(ITagInfo.TagType.END, 178, 189, tag6, "ending tag </xsl:when>");
		assertTypeAndLocation(ITagInfo.TagType.START, 190, 212, tag7, "starting tag <xsl:when test=\"fooB\">");
		assertTypeAndLocation(ITagInfo.TagType.START, 213, 216, tag8, "starting tag <p>");
		assertTypeAndLocation(ITagInfo.TagType.END, 222, 226, tag9, "ending tag </p>");
		assertTypeAndLocation(ITagInfo.TagType.END, 227, 238, tag10, "ending tag </xsl:when>");
		assertTypeAndLocation(ITagInfo.TagType.START, 239, 254, tag11, "starting tag <xsl:otherwise>");
		assertTypeAndLocation(ITagInfo.TagType.EMPTY, 255, 272, tag12, "empty-element tag <foo bar=\"baz\" />");
		assertTypeAndLocation(ITagInfo.TagType.END, 273, 289, tag13, "ending tag </xsl:otherwise>");
		assertTypeAndLocation(ITagInfo.TagType.END, 290, 303, tag14, "ending tag </xsl:choose>");
		assertTypeAndLocation(ITagInfo.TagType.END, 304, 319, tag15, "ending tag </xsl:template>");
		assertTypeAndLocation(ITagInfo.TagType.END, 320, 337, tag16, "ending tag </xsl:stylesheet>");

		assertTagsAssigned(tag0, tag16, "<xsl:stylesheet...>");
		assertTagsAssigned(tag1, tag15, "<xsl:template name=\"foo\">");
		assertTagsAssigned(tag2, tag14, "<xsl:choose>");
		assertTagsAssigned(tag3, tag6, "<xsl:when test=\"fooA\">");
		assertTagsAssigned(tag4, tag5, "<p>");
		assertTagsAssigned(tag7, tag10, "<xsl:when test=\"fooB\">");
		assertTagsAssigned(tag8, tag9, "<p>");
		assertTagsAssigned(tag11, tag13, "<xsl:otherwise>");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMapBuilder#buildTagMap(java.lang.String)}.
	 */
	@Test
	void testBuildTagMap_XMLInAttribute() {
		// offset...........00000000001111.1111112222222222.3333333333444.4444444555555555.5666666666677777777778
		// offset:..........01234567890123.4567890123456789.0123456789012.3456789012345678.9012345678901234567890
		final String xml = "<aaa><bbb ccc=\"ddd<eee/fff>ggg\" /><hhh iii=\"jjj<kkk/lll>mmm\">nnn</hhh></aaa>";
		// tag_index:.........0....1..............................2................................3.....4

		final ITagMap mockedMap = mock(ITagMap.class);
		when(this.factory.create(any())).thenReturn(mockedMap);

		final ITagMap actualMap = this.builder.buildTagMap(xml, this.locationMap);
		assertSame(mockedMap, actualMap);

		verify(this.factory).create(this.tagListCaptor.capture());
		final List<ITagInfo> tagList = this.tagListCaptor.getValue();
		assertNotNull(tagList);
		assertEquals(5, tagList.size());

		final ITagInfo tag0 = tagList.get(0);
		final ITagInfo tag1 = tagList.get(1);
		final ITagInfo tag2 = tagList.get(2);
		final ITagInfo tag3 = tagList.get(3);
		final ITagInfo tag4 = tagList.get(4);

		assertTypeAndLocation(ITagInfo.TagType.START, 0, 5, tag0, "starting tag <aaa>");
		assertTypeAndLocation(ITagInfo.TagType.EMPTY, 5, 34, tag1, "empty-element tag <bbb.../>");
		assertTypeAndLocation(ITagInfo.TagType.START, 34, 61, tag2, "starting tag <hhh>");
		assertTypeAndLocation(ITagInfo.TagType.END, 64, 70, tag3, "ending tag </hhh>");
		assertTypeAndLocation(ITagInfo.TagType.END, 70, 76, tag4, "ending tag </aaa>");

		assertTagsAssigned(tag0, tag4, "<aaa>");
		assertTagsAssigned(tag2, tag3, "<hhh>");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.TagMapBuilder#buildTagMap(java.lang.String)}.
	 */
	@Test
	void testBuildTagMap_AdjacentTags() {
		// offset...........00000000001111.1111.1122222222223333333333.4444.4444445555555555666666666677777777778
		// offset:..........01234567890123.4567.8901234567890123456789.0123.4567890123456789012345678901234567890
		final String xml = "<aaa><bbb ccc=\"eee\"></bbb><xxx><bbb ccc=\"ggg\"></bbb></xxx></aaa>";
		// tag_index:.........0....1.................2....3....4.................5.....6.....7

		final ITagMap mockedMap = mock(ITagMap.class);
		when(this.factory.create(any())).thenReturn(mockedMap);

		final ITagMap actualMap = this.builder.buildTagMap(xml, this.locationMap);
		assertSame(mockedMap, actualMap);

		verify(this.factory).create(this.tagListCaptor.capture());
		final List<ITagInfo> tagList = this.tagListCaptor.getValue();
		assertNotNull(tagList);
		assertEquals(8, tagList.size());

		final ITagInfo tag0 = tagList.get(0);
		final ITagInfo tag1 = tagList.get(1);
		final ITagInfo tag2 = tagList.get(2);
		final ITagInfo tag3 = tagList.get(3);
		final ITagInfo tag4 = tagList.get(4);
		final ITagInfo tag5 = tagList.get(5);
		final ITagInfo tag6 = tagList.get(6);
		final ITagInfo tag7 = tagList.get(7);

		assertTypeAndLocation(ITagInfo.TagType.START, 0, 5, tag0, "starting tag <aaa>");
		assertTypeAndLocation(ITagInfo.TagType.START, 5, 20, tag1, "starting tag <bbb...>");
		assertTypeAndLocation(ITagInfo.TagType.END, 20, 26, tag2, "ending tag </bbb>");
		assertTypeAndLocation(ITagInfo.TagType.START, 26, 31, tag3, "starting tag <xxx>");
		assertTypeAndLocation(ITagInfo.TagType.START, 31, 46, tag4, "starting tag <bbb...>");
		assertTypeAndLocation(ITagInfo.TagType.END, 46, 52, tag5, "ending tag </bbb>");
		assertTypeAndLocation(ITagInfo.TagType.END, 52, 58, tag6, "ending tag </xxx>");
		assertTypeAndLocation(ITagInfo.TagType.END, 58, 64, tag7, "ending tag </aaa>");

		assertTagsAssigned(tag0, tag7, "<aaa>");
		assertTagsAssigned(tag1, tag2, "<bbb>");
		assertTagsAssigned(tag3, tag6, "<xxx>");
		assertTagsAssigned(tag4, tag5, "<bbb>");
	}

	private void assertTypeAndLocation(TagType tagType, int startOffset, int endOffset, ITagInfo tag, String info) {
		assertEquals(tagType, tag.getType(), String.format("tag type of %s", info));
		assertEquals(startOffset, tag.getStartLocation().getCharacterOffset(),
				String.format("start offset of %s", info));
		assertEquals(endOffset, tag.getEndLocation().getCharacterOffset(),
				String.format("end offset of %s", info));
	}

	private void assertTagsAssigned(final ITagInfo startTag, final ITagInfo endTag, String info) {
		assertSame(endTag, startTag.getEndTag().orElseThrow(), String.format("assignment of %s start --> end", info));
		assertSame(startTag, endTag.getStartTag().orElseThrow(), String.format("assignment of %s end --> start", info));
	}

}
