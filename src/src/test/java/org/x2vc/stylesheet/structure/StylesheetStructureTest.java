package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.x2vc.common.XSLTConstants;

import com.google.common.collect.ImmutableList;

class StylesheetStructureTest {

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructure#getTemplates()}.
	 */
	@Test
	void testGetTemplates() {
		StylesheetStructure structure = new StylesheetStructure();
		XSLTParameterNode param1 = new XSLTParameterNode.Builder(structure, "param1").build();
		XSLTParameterNode param2 = new XSLTParameterNode.Builder(structure, "param2").build();
		XSLTDirectiveNode template1 = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.TEMPLATE)
				.addXSLTAttribute("name", "template1").build();
		XSLTDirectiveNode template2 = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.TEMPLATE)
				.addXSLTAttribute("name", "template2").build();
		XSLTDirectiveNode rootNode = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.STYLESHEET)
				.addChildElement(param1).addChildElement(param2).addChildElement(template1).addChildElement(template2)
				.build();
		structure.setRootNode(rootNode);

		ImmutableList<IXSLTDirectiveNode> templates = structure.getTemplates();
		assertEquals(2, templates.size());
		assertEquals(template1, templates.get(0));
		assertEquals(template2, templates.get(1));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructure#getParameters()}.
	 */
	@Test
	void testGetParameters() {
		StylesheetStructure structure = new StylesheetStructure();
		XSLTParameterNode param1 = new XSLTParameterNode.Builder(structure, "param1").build();
		XSLTParameterNode param2 = new XSLTParameterNode.Builder(structure, "param2").build();
		XSLTDirectiveNode template1 = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.TEMPLATE)
				.addXSLTAttribute("name", "template1").build();
		XSLTDirectiveNode template2 = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.TEMPLATE)
				.addXSLTAttribute("name", "template2").build();
		XSLTDirectiveNode rootNode = new XSLTDirectiveNode.Builder(structure, XSLTConstants.Elements.STYLESHEET)
				.addChildElement(param1).addChildElement(param2).addChildElement(template1).addChildElement(template2)
				.build();
		structure.setRootNode(rootNode);

		ImmutableList<IXSLTParameterNode> parameters = structure.getParameters();
		assertEquals(2, parameters.size());
		assertEquals(param1, parameters.get(0));
		assertEquals(param2, parameters.get(1));
	}

}