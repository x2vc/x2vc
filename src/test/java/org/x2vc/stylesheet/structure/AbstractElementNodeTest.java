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
package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.xml.ITagInfo;
import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class AbstractElementNodeTest {

	@Mock
	IStylesheetStructure parentStructure;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#getTagSourceRange()}
	 */
	@Test
	void testGetTagSourceRange_EmptyElement() {
		final ITagInfo tagInfo = mock(ITagInfo.class);
		lenient().when(tagInfo.getType()).thenReturn(ITagInfo.TagType.EMPTY);
		lenient().when(tagInfo.isEmptyElement()).thenReturn(true);

		final PolymorphLocation startLocation = mock(PolymorphLocation.class, "start location");
		final PolymorphLocation endLocation = mock(PolymorphLocation.class, "end location");
		lenient().when(startLocation.compareTo(eq(endLocation))).thenReturn(-1);
		lenient().when(endLocation.compareTo(eq(startLocation))).thenReturn(1);
		lenient().when(tagInfo.getStartLocation()).thenReturn(startLocation);
		lenient().when(tagInfo.getEndLocation()).thenReturn(endLocation);

		final AbstractElementNode node = new AbstractElementNode(this.parentStructure, tagInfo, ImmutableList.of()) {
		};

		final Range<PolymorphLocation> actualRange = node.getTagSourceRange();

		assertEquals(Range.closedOpen(startLocation, endLocation), actualRange);
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#getTagSourceRange()}
	 */
	@Test
	void testGetTagSourceRange_StartEndElement() {
		final ITagInfo startTagInfo = mock(ITagInfo.class);
		lenient().when(startTagInfo.getType()).thenReturn(ITagInfo.TagType.START);
		lenient().when(startTagInfo.isStartTag()).thenReturn(true);

		final ITagInfo endTagInfo = mock(ITagInfo.class);
		lenient().when(endTagInfo.getType()).thenReturn(ITagInfo.TagType.END);
		lenient().when(endTagInfo.isEndTag()).thenReturn(true);

		lenient().when(startTagInfo.getEndTag()).thenReturn(Optional.of(endTagInfo));
		lenient().when(endTagInfo.getStartTag()).thenReturn(Optional.of(startTagInfo));

		final PolymorphLocation startLocation = mock(PolymorphLocation.class, "start location");
		final PolymorphLocation endLocation = mock(PolymorphLocation.class, "end location");
		lenient().when(startLocation.compareTo(eq(endLocation))).thenReturn(-1);
		lenient().when(endLocation.compareTo(eq(startLocation))).thenReturn(1);
		lenient().when(startTagInfo.getStartLocation()).thenReturn(startLocation);
		lenient().when(endTagInfo.getEndLocation()).thenReturn(endLocation);

		final AbstractElementNode node = new AbstractElementNode(this.parentStructure, startTagInfo,
				ImmutableList.of()) {
		};

		final Range<PolymorphLocation> actualRange = node.getTagSourceRange();

		assertEquals(Range.closedOpen(startLocation, endLocation), actualRange);
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#getTagSourceRange()}
	 */
	@Test
	void testGetTagSourceRange_EndStartElement() {
		final ITagInfo startTagInfo = mock(ITagInfo.class);
		lenient().when(startTagInfo.getType()).thenReturn(ITagInfo.TagType.START);
		lenient().when(startTagInfo.isStartTag()).thenReturn(true);

		final ITagInfo endTagInfo = mock(ITagInfo.class);
		lenient().when(endTagInfo.getType()).thenReturn(ITagInfo.TagType.END);
		lenient().when(endTagInfo.isEndTag()).thenReturn(true);

		lenient().when(startTagInfo.getEndTag()).thenReturn(Optional.of(endTagInfo));
		lenient().when(endTagInfo.getStartTag()).thenReturn(Optional.of(startTagInfo));

		final PolymorphLocation startLocation = mock(PolymorphLocation.class, "start location");
		final PolymorphLocation endLocation = mock(PolymorphLocation.class, "end location");
		lenient().when(startLocation.compareTo(eq(endLocation))).thenReturn(-1);
		lenient().when(endLocation.compareTo(eq(startLocation))).thenReturn(1);
		lenient().when(startTagInfo.getStartLocation()).thenReturn(startLocation);
		lenient().when(endTagInfo.getEndLocation()).thenReturn(endLocation);

		final AbstractElementNode node = new AbstractElementNode(this.parentStructure, endTagInfo,
				ImmutableList.of()) {
		};

		final Range<PolymorphLocation> actualRange = node.getTagSourceRange();

		assertEquals(Range.closedOpen(startLocation, endLocation), actualRange);
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.AbstractElementNode#hashCode()}
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(AbstractElementNode.class)
			.withRedefinedSubclass(XMLNode.class)
			.usingGetClass()
			.verify();
	}

}
