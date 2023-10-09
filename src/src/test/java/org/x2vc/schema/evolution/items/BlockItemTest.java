package org.x2vc.schema.evolution.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.instruct.Block;

@ExtendWith(MockitoExtension.class)
class BlockItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private Block expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private BlockItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new BlockItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		final Expression opEx1 = mock("opEx1");
		final Expression opEx2 = mock("opEx2");
		final Expression opEx3 = mock("opEx3");
		final Operand op1 = mock("op1");
		final Operand op2 = mock("op2");
		final Operand op3 = mock("op3");
		when(op1.getChildExpression()).thenReturn(opEx1);
		when(op2.getChildExpression()).thenReturn(opEx2);
		when(op3.getChildExpression()).thenReturn(opEx3);
		final Operand[] operands = new Operand[] { op1, op2, op3 };
		when(this.expression.getOperanda()).thenReturn(operands);

		this.treeItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, times(1)).createItemForExpression(opEx1);
		verify(this.itemFactory, times(1)).createItemForExpression(opEx2);
		verify(this.itemFactory, times(1)).createItemForExpression(opEx3);
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression opEx1 = mock("opEx1");
		final Expression opEx2 = mock("opEx2");
		final Expression opEx3 = mock("opEx3");
		final Operand op1 = mock("op1");
		final Operand op2 = mock("op2");
		final Operand op3 = mock("op3");
		when(op1.getChildExpression()).thenReturn(opEx1);
		when(op2.getChildExpression()).thenReturn(opEx2);
		when(op3.getChildExpression()).thenReturn(opEx3);
		final Operand[] operands = new Operand[] { op1, op2, op3 };
		when(this.expression.getOperanda()).thenReturn(operands);

		final IEvaluationTreeItem item1 = mock("item1");
		final IEvaluationTreeItem item2 = mock("item2");
		final IEvaluationTreeItem item3 = mock("item3");
		when(this.itemFactory.createItemForExpression(opEx1)).thenReturn(item1);
		when(this.itemFactory.createItemForExpression(opEx2)).thenReturn(item2);
		when(this.itemFactory.createItemForExpression(opEx3)).thenReturn(item3);

		final ISchemaElementProxy contextItem = mock();

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		verify(item1, times(1)).evaluate(contextItem);
		verify(item2, times(1)).evaluate(contextItem);
		verify(item3, times(1)).evaluate(contextItem);

		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));

	}

}
