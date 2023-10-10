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

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;

@ExtendWith(MockitoExtension.class)
class FilterExpressionItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private FilterExpression expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private FilterExpressionItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new FilterExpressionItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		final Expression baseExpression = mock();
		final Expression filterExpression = mock();
		when(this.expression.getBase()).thenReturn(baseExpression);
		when(this.expression.getFilter()).thenReturn(filterExpression);

		this.treeItem.initialize(this.itemFactory);

		// subordinate items for the two sub-expressions have to be created
		verify(this.itemFactory, times(1)).createItemForExpression(baseExpression);
		verify(this.itemFactory, times(1)).createItemForExpression(filterExpression);

		// no subordinate node tests required for this item type
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression baseExpression = mock("base expression");
		final Expression filterExpression = mock("filter expression");
		when(this.expression.getBase()).thenReturn(baseExpression);
		when(this.expression.getFilter()).thenReturn(filterExpression);

		final IEvaluationTreeItem baseItem = mock("base item");
		final IEvaluationTreeItem filterItem = mock("filter item");

		when(this.itemFactory.createItemForExpression(baseExpression)).thenReturn(baseItem);
		when(this.itemFactory.createItemForExpression(filterExpression)).thenReturn(filterItem);

		final ISchemaElementProxy contextItem = mock("context");

		// base expression returns four result items
		final ISchemaElementProxy baseResult1 = mock("base result 1");
		final ISchemaElementProxy baseResult2 = mock("base result 2");
		final ISchemaElementProxy baseResult3 = mock("base result 3");
		final ISchemaElementProxy baseResult4 = mock("base result 4");
		when(baseItem.evaluate(contextItem))
			.thenReturn(ImmutableSet.of(baseResult1, baseResult2, baseResult3, baseResult4));

		// filter expression only returns two of the items
		final ISchemaElementProxy filterResult1 = mock("filter result 1");
		final ISchemaElementProxy filterResult3 = mock("filter result 3");
		when(filterItem.evaluate(baseResult1))
			.thenReturn(ImmutableSet.of(filterResult1));
		when(filterItem.evaluate(baseResult2))
			.thenReturn(ImmutableSet.of());
		when(filterItem.evaluate(baseResult3))
			.thenReturn(ImmutableSet.of(filterResult3));
		when(filterItem.evaluate(baseResult4))
			.thenReturn(ImmutableSet.of());

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the expression itself does not record any access
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// verify the sub-expressions were called
		verify(baseItem, times(1)).evaluate(contextItem);
		verify(filterItem, times(1)).evaluate(baseResult1);
		verify(filterItem, times(1)).evaluate(baseResult2);
		verify(filterItem, times(1)).evaluate(baseResult3);
		verify(filterItem, times(1)).evaluate(baseResult4);

		// the result should contain all four remaining results
		assertEquals(2, result.size());
		assertTrue(result.contains(filterResult1));
		assertTrue(result.contains(filterResult3));
	}

}
