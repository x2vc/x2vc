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
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.Expression;

@ExtendWith(MockitoExtension.class)
class UnaryExpressionItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private Atomizer expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private UnaryExpressionItem<Atomizer> treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new UnaryExpressionItem<Atomizer>(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		final Expression baseExpression = mock();
		when(this.expression.getBaseExpression()).thenReturn(baseExpression);

		this.treeItem.initialize(this.itemFactory);

		// check that item for base expression was requested
		verify(this.itemFactory, times(1)).createItemForExpression(baseExpression);

		// no subordinate node test items required for this item type
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression baseExpression = mock();
		when(this.expression.getBaseExpression()).thenReturn(baseExpression);

		final IEvaluationTreeItem baseItem = mock();
		when(this.itemFactory.createItemForExpression(baseExpression)).thenReturn(baseItem);

		final ISchemaElementProxy contextItem = mock();
		final ISchemaElementProxy resultItem = mock();
		when(baseItem.evaluate(contextItem)).thenReturn(ImmutableSet.of(resultItem));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the expression does not record any access
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// verify the base expression was evaluated
		verify(baseItem, times(1)).evaluate(contextItem);

		// the result set should consist of the element returned by the base expression
		assertEquals(1, result.size());
		assertTrue(result.contains(resultItem));

	}

}
