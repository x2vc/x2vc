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
package org.x2vc.schema.evolution.items;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

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

import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.NodeTest;

@ExtendWith(MockitoExtension.class)
class CombinedNodeTestItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private CombinedNodeTest nodeTest;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private CombinedNodeTestItem nodeTestItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.nodeTestItem = new CombinedNodeTestItem(this.schema, this.coordinator, this.nodeTest);
	}

	@Test
	void testUnion() {
		final NodeTest componentTest1 = mock();
		final NodeTest componentTest2 = mock();
		when(this.nodeTest.getComponentNodeTests()).thenReturn(new NodeTest[] { componentTest1, componentTest2 });
		when(this.nodeTest.getOperator()).thenReturn(Token.UNION);

		// provide mocked subordinate items
		final INodeTestTreeItem componentItem1 = mock();
		final INodeTestTreeItem componentItem2 = mock();
		when(this.itemFactory.createItemForNodeTest(componentTest1)).thenReturn(componentItem1);
		when(this.itemFactory.createItemForNodeTest(componentTest2)).thenReturn(componentItem2);

		this.nodeTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nodeTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// prepare some elements to filter
		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy elem4 = mock("elem4");
		final Collection<ISchemaElementProxy> elems = Set.of(elem1, elem2, elem3, elem4);

		// for the union test, have component1 filter for element1 and component 2 for element 2
		when(componentItem1.filter(elems)).thenReturn(ImmutableSet.of(elem1));
		when(componentItem2.filter(elems)).thenReturn(ImmutableSet.of(elem2));

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nodeTestItem.filter(elems);
		assertEquals(2, resultElems.size());
		assertTrue(resultElems.contains(elem1));
		assertTrue(resultElems.contains(elem2));

		// this node test itself does not record any access events
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

	}

	@Test
	void testIntersect() {
		final NodeTest componentTest1 = mock();
		final NodeTest componentTest2 = mock();
		when(this.nodeTest.getComponentNodeTests()).thenReturn(new NodeTest[] { componentTest1, componentTest2 });
		when(this.nodeTest.getOperator()).thenReturn(Token.INTERSECT);

		// provide mocked subordinate items
		final INodeTestTreeItem componentItem1 = mock();
		final INodeTestTreeItem componentItem2 = mock();
		when(this.itemFactory.createItemForNodeTest(componentTest1)).thenReturn(componentItem1);
		when(this.itemFactory.createItemForNodeTest(componentTest2)).thenReturn(componentItem2);

		this.nodeTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nodeTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// prepare some elements to filter
		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy elem4 = mock("elem4");
		final Collection<ISchemaElementProxy> elems = Set.of(elem1, elem2, elem3, elem4);

		// for the union test, have component1 filter for element1/2/3 and component 2 for element 2/3/4
		when(componentItem1.filter(elems)).thenReturn(ImmutableSet.of(elem1, elem2, elem3));
		when(componentItem2.filter(ImmutableSet.of(elem1, elem2, elem3)))
			.thenReturn(ImmutableSet.of(elem2, elem3));

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nodeTestItem.filter(elems);
		assertEquals(2, resultElems.size());
		assertTrue(resultElems.contains(elem2));
		assertTrue(resultElems.contains(elem3));

		// this node test itself does not record any access events
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

	}

	@Test
	void testExcept() {
		final NodeTest componentTest1 = mock();
		final NodeTest componentTest2 = mock();
		when(this.nodeTest.getComponentNodeTests()).thenReturn(new NodeTest[] { componentTest1, componentTest2 });
		when(this.nodeTest.getOperator()).thenReturn(Token.EXCEPT);

		// provide mocked subordinate items
		final INodeTestTreeItem componentItem1 = mock();
		final INodeTestTreeItem componentItem2 = mock();
		when(this.itemFactory.createItemForNodeTest(componentTest1)).thenReturn(componentItem1);
		when(this.itemFactory.createItemForNodeTest(componentTest2)).thenReturn(componentItem2);

		this.nodeTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nodeTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// prepare some elements to filter
		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy elem4 = mock("elem4");
		final Collection<ISchemaElementProxy> elems = Set.of(elem1, elem2, elem3, elem4);

		// for the union test, have component1 filter for element1/2/3 and component 2 for element 2
		when(componentItem1.filter(elems)).thenReturn(ImmutableSet.of(elem1, elem2, elem3));
		when(componentItem2.filter(elems)).thenReturn(ImmutableSet.of(elem2));

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nodeTestItem.filter(elems);
		assertEquals(2, resultElems.size());
		assertTrue(resultElems.contains(elem1));
		assertTrue(resultElems.contains(elem3));

		// this node test itself does not record any access events
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

}
