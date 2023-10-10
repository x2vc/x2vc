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
import net.sf.saxon.expr.instruct.ValueOf;

@ExtendWith(MockitoExtension.class)
class ValueOfItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private ValueOf expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private ValueOfItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new ValueOfItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		final Expression selectExp = mock();
		when(this.expression.getSelect()).thenReturn(selectExp);

		this.treeItem.initialize(this.itemFactory);

		verify(this.itemFactory, times(1)).createItemForExpression(selectExp);

		// no subordinate node tests required for this item type
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression selectExp = mock();
		when(this.expression.getSelect()).thenReturn(selectExp);

		final IEvaluationTreeItem selectItem = mock();
		when(this.itemFactory.createItemForExpression(selectExp)).thenReturn(selectItem);

		final ISchemaElementProxy contextItem = mock();

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		verify(selectItem, times(1)).evaluate(contextItem);
		verify(this.coordinator, never()).handleElementAccess(any(), any());
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());

		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));

	}

}
