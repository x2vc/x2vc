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
import net.sf.saxon.expr.SlashExpression;

@ExtendWith(MockitoExtension.class)
class SlashExpressionItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private SlashExpression expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private SlashExpressionItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new SlashExpressionItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		final Expression firstStepExpression = mock();
		final Expression remainingStepsExpression = mock();
		when(this.expression.getFirstStep()).thenReturn(firstStepExpression);
		when(this.expression.getRemainingSteps()).thenReturn(remainingStepsExpression);

		this.treeItem.initialize(this.itemFactory);

		// subordinate items for the two sub-expressions have to be created
		verify(this.itemFactory, times(1)).createItemForExpression(firstStepExpression);
		verify(this.itemFactory, times(1)).createItemForExpression(remainingStepsExpression);

		// no subordinate node tests required for this item type
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression firstStepExpression = mock("first step expression");
		final Expression remainingStepsExpression = mock("remaining steps expression");
		when(this.expression.getFirstStep()).thenReturn(firstStepExpression);
		when(this.expression.getRemainingSteps()).thenReturn(remainingStepsExpression);

		final IEvaluationTreeItem firstStepItem = mock("first steps item");
		final IEvaluationTreeItem remainingStepsItem = mock("remaining steps item");

		when(this.itemFactory.createItemForExpression(firstStepExpression)).thenReturn(firstStepItem);
		when(this.itemFactory.createItemForExpression(remainingStepsExpression)).thenReturn(remainingStepsItem);

		final ISchemaElementProxy contextItem = mock("context");

		// first expression returns two result items
		final ISchemaElementProxy firstResult1 = mock("first result 1");
		final ISchemaElementProxy firstResult2 = mock("first result 2");
		when(firstStepItem.evaluate(contextItem)).thenReturn(ImmutableSet.of(firstResult1, firstResult2));

		// remaining expression returns two result items each
		final ISchemaElementProxy remainingResult1a = mock("remaining result 1a");
		final ISchemaElementProxy remainingResult1b = mock("remaining result 1b");
		final ISchemaElementProxy remainingResult2a = mock("remaining result 2a");
		final ISchemaElementProxy remainingResult2b = mock("remaining result 2b");
		when(remainingStepsItem.evaluate(firstResult1))
			.thenReturn(ImmutableSet.of(remainingResult1a, remainingResult1b));
		when(remainingStepsItem.evaluate(firstResult2))
			.thenReturn(ImmutableSet.of(remainingResult2a, remainingResult2b));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the expression itself does not record any access
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// verify the sub-expressions were called
		verify(firstStepItem, times(1)).evaluate(contextItem);
		verify(remainingStepsItem, times(1)).evaluate(firstResult1);
		verify(remainingStepsItem, times(1)).evaluate(firstResult2);

		// the result should contain all four remaining results
		assertEquals(4, result.size());
		assertTrue(result.contains(remainingResult1a));
		assertTrue(result.contains(remainingResult1b));
		assertTrue(result.contains(remainingResult2a));
		assertTrue(result.contains(remainingResult2b));
	}

}
