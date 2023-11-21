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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class XSLTDirectiveNodeTest {

	@Mock
	private IStylesheetStructure parentStructure;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getChildDirectives()}.
	 */
	@Test
	void testGetChildDirectives_WithChildDirectivesAbsent() {
		final IXMLNode nonDirective1 = mock();
		when(nonDirective1.getChildElements()).thenReturn(ImmutableList.of());
		final IXMLNode nonDirective2 = mock();
		when(nonDirective2.getChildElements()).thenReturn(ImmutableList.of());

		final IXSLTDirectiveNode node = XSLTDirectiveNode
			.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1)
			.addChildElement(nonDirective2)
			.build();

		assertTrue(node.getChildDirectives().isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getChildDirectives()}.
	 */
	@Test
	void testGetChildDirectives_WithChildDirectivesPresent() {
		final IXMLNode nonDirective1 = mock();
		when(nonDirective1.getChildElements()).thenReturn(ImmutableList.of());
		final IXSLTDirectiveNode directive1 = mock();
		final IXMLNode nonDirective2 = mock();
		when(nonDirective2.getChildElements()).thenReturn(ImmutableList.of());

		final IXSLTDirectiveNode node = XSLTDirectiveNode
			.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1)
			.addChildElement(directive1)
			.addChildElement(nonDirective2)
			.build();

		final ImmutableList<IXSLTDirectiveNode> directives = node.getChildDirectives();
		assertEquals(1, directives.size());
		assertEquals(directive1, directives.get(0));
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getChildDirectives()}.
	 */
	@Test
	void testGetChildDirectives_WithNestedChildDirectives() {
		final IXSLTDirectiveNode directive1b = mock();
		final IXMLNode nonDirective1 = mock();
		when(nonDirective1.getChildElements()).thenReturn(ImmutableList.of(directive1b));

		final IXSLTDirectiveNode directive2 = mock();

		final IXSLTDirectiveNode directive3b = mock();
		final IXMLNode nonDirective3 = mock();
		when(nonDirective3.getChildElements()).thenReturn(ImmutableList.of(directive3b));

		final IXSLTDirectiveNode node = XSLTDirectiveNode
			.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1)
			.addChildElement(directive2)
			.addChildElement(nonDirective3)
			.build();

		final ImmutableList<IXSLTDirectiveNode> directives = node.getChildDirectives();
		assertEquals(3, directives.size());
		assertEquals(directive1b, directives.get(0));
		assertEquals(directive2, directives.get(1));
		assertEquals(directive3b, directives.get(2));
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO #26 check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(XSLTDirectiveNode.class).verify();
	}

}
