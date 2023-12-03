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
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.xml.ITagInfo;

import com.google.common.collect.ImmutableList;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class StylesheetStructureTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.StylesheetStructure#getTemplates()}.
	 */
	@Test
	void testGetTemplates() {
		final StylesheetStructure structure = new StylesheetStructure();
		final XSLTParameterNode param1 = XSLTParameterNode.builder(structure, mock(ITagInfo.class), "param1").build();
		final XSLTParameterNode param2 = XSLTParameterNode.builder(structure, mock(ITagInfo.class), "param2").build();
		final IXSLTDirectiveNode template1 = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template1").build();
		final IXSLTDirectiveNode template2 = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template2").build();
		final IXSLTDirectiveNode rootNode = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.STYLESHEET)
			.addChildElement(param1).addChildElement(param2).addChildElement(template1).addChildElement(template2)
			.build();
		structure.setRootNode(rootNode);

		final ImmutableList<IXSLTTemplateNode> templates = structure.getTemplates();
		assertEquals(2, templates.size());
		assertEquals(template1, templates.get(0));
		assertEquals(template2, templates.get(1));
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.StylesheetStructure#getParameters()}.
	 */
	@Test
	void testGetStylesheetParameters() {
		final StylesheetStructure structure = new StylesheetStructure();
		final XSLTParameterNode param1 = XSLTParameterNode.builder(structure, mock(ITagInfo.class), "param1").build();
		final XSLTParameterNode param2 = XSLTParameterNode.builder(structure, mock(ITagInfo.class), "param2").build();
		final IXSLTDirectiveNode template1 = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template1")
			.build();
		final IXSLTDirectiveNode template2 = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template2")
			.build();
		final IXSLTDirectiveNode rootNode = XSLTDirectiveNode
			.builder(structure, mock(ITagInfo.class), XSLTConstants.Elements.STYLESHEET)
			.addFormalParameter(param1)
			.addFormalParameter(param2)
			.addChildElement(template1)
			.addChildElement(template2)
			.build();
		structure.setRootNode(rootNode);

		final ImmutableList<IXSLTParameterNode> parameters = structure.getParameters();
		assertEquals(2, parameters.size());
		assertEquals(param1, parameters.get(0));
		assertEquals(param2, parameters.get(1));
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.StylesheetStructure#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructure#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(StylesheetStructure.class)
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

}
