package org.x2vc.schema.evolution.items;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

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

import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Expression;

@ExtendWith(MockitoExtension.class)
class IndependentBinaryExpressionItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private BinaryExpression expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private IndependentBinaryExpressionItem<BinaryExpression> treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new IndependentBinaryExpressionItem<BinaryExpression>(this.schema, this.coordinator,
				this.expression);
	}

	@Test
	void testInitialization() {
		final Expression lhsExpression = mock();
		final Expression rhsExpression = mock();
		when(this.expression.getLhsExpression()).thenReturn(lhsExpression);
		when(this.expression.getRhsExpression()).thenReturn(rhsExpression);

		this.treeItem.initialize(this.itemFactory);

		// check that item for base expression was requested
		verify(this.itemFactory, times(1)).createItemForExpression(lhsExpression);
		verify(this.itemFactory, times(1)).createItemForExpression(rhsExpression);

		// no subordinate node test items required for this item type
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final Expression lhsExpression = mock();
		final Expression rhsExpression = mock();
		when(this.expression.getLhsExpression()).thenReturn(lhsExpression);
		when(this.expression.getRhsExpression()).thenReturn(rhsExpression);

		final IEvaluationTreeItem lhsItem = mock();
		final IEvaluationTreeItem rhsItem = mock();
		when(this.itemFactory.createItemForExpression(lhsExpression)).thenReturn(lhsItem);
		when(this.itemFactory.createItemForExpression(rhsExpression)).thenReturn(rhsItem);

		final ISchemaElementProxy contextItem = mock();

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the expression does not record any access
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// verify the base expressions were evaluated
		verify(lhsItem, times(1)).evaluate(contextItem);
		verify(rhsItem, times(1)).evaluate(contextItem);

		// the result set should consist of the element returned by the base expression
		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));
	}

}
