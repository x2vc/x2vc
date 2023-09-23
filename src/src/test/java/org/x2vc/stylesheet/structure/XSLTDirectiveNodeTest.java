package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
class XSLTDirectiveNodeTest {

	@Mock
	private IStylesheetStructure parentStructure;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getChildDirectives()}.
	 */
	@Test
	void testGetChildDirectives_WithChildDirectivesAbsent() {
		final IStructureTreeNode nonDirective1 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective1.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective1.isText()).thenReturn(true);
		final IStructureTreeNode nonDirective2 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective2.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective2.isXML()).thenReturn(true);

		final IXSLTDirectiveNode node = XSLTDirectiveNode.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1).addChildElement(nonDirective2).build();

		assertTrue(node.getChildDirectives().isEmpty());
	}

	void testGetChildDirectives_WithChildDirectivesPresent() {
		final IStructureTreeNode nonDirective1 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective1.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective1.isText()).thenReturn(true);
		final IStructureTreeNode directive1 = mock(IStructureTreeNode.class);
		lenient().when(directive1.isXSLTDirective()).thenReturn(true);
		final IStructureTreeNode nonDirective2 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective2.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective2.isXML()).thenReturn(true);

		final IXSLTDirectiveNode node = XSLTDirectiveNode.builder(this.parentStructure, "someName")
			.addChildElement(nonDirective1).addChildElement(directive1).addChildElement(nonDirective2).build();

		final ImmutableList<IXSLTDirectiveNode> directives = node.getChildDirectives();
		assertFalse(directives.isEmpty());
		assertEquals(1, directives.size());
		assertEquals(directive1, directives.get(0));
	}

}
