package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.ExtendedXSLTConstants;
import org.x2vc.stylesheet.structure.IStructureTreeNode.NodeType;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
class XSLTDirectiveNodeTest {

	@Mock
	private IStylesheetStructure parentStructure;

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getTraceID()}.
	 */
	@Test
	void testGetTraceID_WithIDAbsent() {
		XSLTDirectiveNode node = new XSLTDirectiveNode.Builder(this.parentStructure, "someName").build();

		assertFalse(node.getTraceID().isPresent());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getTraceID()}.
	 */
	@Test
	void testGetTraceID_WithIDPresent() {
		XSLTDirectiveNode node = new XSLTDirectiveNode.Builder(this.parentStructure, "someName")
				.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "42").build();
		assertTrue(node.getTraceID().isPresent());
		assertEquals(42, node.getTraceID().get());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getTraceID()}.
	 */
	@Test
	void testGetTraceID_WithIDMalformed() {
		XSLTDirectiveNode node = new XSLTDirectiveNode.Builder(this.parentStructure, "someName")
				.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "foobar").build();
		assertThrows(NumberFormatException.class, () -> {
			node.getTraceID();
		});
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.XSLTDirectiveNode#getChildDirectives()}.
	 */
	@Test
	void testGetChildDirectives_WithChildDirectivesAbsent() {
		IStructureTreeNode nonDirective1 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective1.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective1.getType()).thenReturn(NodeType.TEXT);
		IStructureTreeNode nonDirective2 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective2.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective2.getType()).thenReturn(NodeType.XML);

		XSLTDirectiveNode node = new XSLTDirectiveNode.Builder(this.parentStructure, "someName")
				.addChildElement(nonDirective1).addChildElement(nonDirective2).build();

		assertTrue(node.getChildDirectives().isEmpty());
	}

	void testGetChildDirectives_WithChildDirectivesPresent() {
		IStructureTreeNode nonDirective1 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective1.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective1.getType()).thenReturn(NodeType.TEXT);
		IStructureTreeNode directive1 = mock(IStructureTreeNode.class);
		lenient().when(directive1.isXSLTDirective()).thenReturn(true);
		lenient().when(directive1.getType()).thenReturn(NodeType.XSLT_DIRECTIVE);
		IStructureTreeNode nonDirective2 = mock(IStructureTreeNode.class);
		lenient().when(nonDirective2.isXSLTDirective()).thenReturn(false);
		lenient().when(nonDirective2.getType()).thenReturn(NodeType.XML);

		XSLTDirectiveNode node = new XSLTDirectiveNode.Builder(this.parentStructure, "someName")
				.addChildElement(nonDirective1).addChildElement(directive1).addChildElement(nonDirective2).build();

		ImmutableList<IXSLTDirectiveNode> directives = node.getChildDirectives();
		assertFalse(directives.isEmpty());
		assertEquals(1, directives.size());
		assertEquals(directive1, directives.get(0));
	}

}
