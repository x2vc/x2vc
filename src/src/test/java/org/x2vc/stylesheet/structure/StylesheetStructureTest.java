package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.x2vc.stylesheet.XSLTConstants;

import com.google.common.collect.ImmutableList;

import nl.jqno.equalsverifier.EqualsVerifier;

class StylesheetStructureTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.StylesheetStructure#getTemplates()}.
	 */
	@Test
	void testGetTemplates() {
		final StylesheetStructure structure = new StylesheetStructure();
		final XSLTParameterNode param1 = XSLTParameterNode.builder(structure, "param1").build();
		final XSLTParameterNode param2 = XSLTParameterNode.builder(structure, "param2").build();
		final IXSLTDirectiveNode template1 = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template1").build();
		final IXSLTDirectiveNode template2 = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template2").build();
		final IXSLTDirectiveNode rootNode = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.STYLESHEET)
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
		final XSLTParameterNode param1 = XSLTParameterNode.builder(structure, "param1").build();
		final XSLTParameterNode param2 = XSLTParameterNode.builder(structure, "param2").build();
		final IXSLTDirectiveNode template1 = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template1")
			.build();
		final IXSLTDirectiveNode template2 = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "template2")
			.build();
		final IXSLTDirectiveNode rootNode = XSLTDirectiveNode
			.builder(structure, XSLTConstants.Elements.STYLESHEET)
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
	 * Test method for {@link org.x2vc.stylesheet.structure.StylesheetStructure#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(StylesheetStructure.class).verify();
	}

}
