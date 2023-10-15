package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
		final IXMLNode nonDirective2 = mock();

		final IXSLTDirectiveNode node = XSLTDirectiveNode.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1).addChildElement(nonDirective2).build();

		assertTrue(node.getChildDirectives().isEmpty());
	}

	void testGetChildDirectives_WithChildDirectivesPresent() {
		final IXMLNode nonDirective1 = mock();
		final IXSLTDirectiveNode directive1 = mock();
		final IXMLNode nonDirective2 = mock();

		final IXSLTDirectiveNode node = XSLTDirectiveNode.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1).addChildElement(directive1).addChildElement(nonDirective2).build();

		final ImmutableList<IXSLTDirectiveNode> directives = node.getChildDirectives();
		assertFalse(directives.isEmpty());
		assertEquals(1, directives.size());
		assertEquals(directive1, directives.get(0));
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(XSLTDirectiveNode.class).verify();
	}

}
