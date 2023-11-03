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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.pattern.NodeTest;

@ExtendWith(MockitoExtension.class)
class AxisExpressionItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private AxisExpression expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private AxisExpressionItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new AxisExpressionItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization_WithoutNodeTest() {
		when(this.expression.getNodeTest()).thenReturn(null);

		this.treeItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testInitialization_WithNodeTest() {
		final NodeTest nodeTest = mock();
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		this.treeItem.initialize(this.itemFactory);

		// no subordinate expressions required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());

		// a node test should have been required
		verify(this.itemFactory, times(1)).createItemForNodeTest(nodeTest);

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation_AxisAttribute_WithoutNodeTest() {
		when(this.expression.getAxis()).thenReturn(AxisInfo.ATTRIBUTE);
		when(this.expression.getNodeTest()).thenReturn(null);

		final ISchemaElementProxy att1 = mock("att1");
		final ISchemaElementProxy att2 = mock("att2");
		final ISchemaElementProxy att3 = mock("att3");
		final ISchemaElementProxy contextItem = mock();
		when(contextItem.getSubAttributes()).thenReturn(ImmutableSet.of(att1, att2, att3));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain all three attributes
		assertEquals(3, result.size());
		assertTrue(result.contains(att1));
		assertTrue(result.contains(att2));
		assertTrue(result.contains(att3));
	}

	@Test
	void testEvaluation_AxisAttribute_WithNodeTest() {
		final NodeTest nodeTest = mock();
		final INodeTestTreeItem nodeTestItem = mock();
		when(this.itemFactory.createItemForNodeTest(nodeTest)).thenReturn(nodeTestItem);

		when(this.expression.getAxis()).thenReturn(AxisInfo.ATTRIBUTE);
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		final ISchemaElementProxy att1 = mock("att1");
		final ISchemaElementProxy att2 = mock("att2");
		final ISchemaElementProxy att3 = mock("att3");
		final ISchemaElementProxy contextItem = mock();
		when(contextItem.getSubAttributes()).thenReturn(ImmutableSet.of(att1, att2, att3));

		// setup the node test to only let one of the attributes pass
		when(nodeTestItem.filter(Set.of(att1, att2, att3))).thenReturn(ImmutableList.of(att2));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// ensure that the node test was called
		verify(nodeTestItem, times(1)).evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should only contain all one attribute
		assertEquals(1, result.size());
		assertFalse(result.contains(att1));
		assertTrue(result.contains(att2));
		assertFalse(result.contains(att3));
	}

	@Test
	void testEvaluation_AxisChild_WithoutNodeTest() {
		when(this.expression.getAxis()).thenReturn(AxisInfo.CHILD);
		when(this.expression.getNodeTest()).thenReturn(null);

		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy contextItem = mock();
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(elem1, elem2, elem3));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain all three attributes
		assertEquals(3, result.size());
		assertTrue(result.contains(elem1));
		assertTrue(result.contains(elem2));
		assertTrue(result.contains(elem3));
	}

	@Test
	void testEvaluation_AxisChild_WithNodeTest() {
		final NodeTest nodeTest = mock();
		final INodeTestTreeItem nodeTestItem = mock();
		when(this.itemFactory.createItemForNodeTest(nodeTest)).thenReturn(nodeTestItem);

		when(this.expression.getAxis()).thenReturn(AxisInfo.CHILD);
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy contextItem = mock();
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(elem1, elem2, elem3));

		// setup the node test to only let one of the elements pass
		when(nodeTestItem.filter(Set.of(elem1, elem2, elem3))).thenReturn(ImmutableList.of(elem2));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// ensure that the node test was called
		verify(nodeTestItem, times(1)).evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should only contain all one attribute
		assertEquals(1, result.size());
		assertFalse(result.contains(elem1));
		assertTrue(result.contains(elem2));
		assertFalse(result.contains(elem3));
	}

	@Test
	void testEvaluation_AxisSelf_WithoutNodeTest() {
		when(this.expression.getAxis()).thenReturn(AxisInfo.SELF);
		when(this.expression.getNodeTest()).thenReturn(null);

		final ISchemaElementProxy contextItem = mock();

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain the node itself
		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));
	}

	@Test
	void testEvaluation_AxisSelf_WithNodeTest() {
		final NodeTest nodeTest = mock();
		final INodeTestTreeItem nodeTestItem = mock();
		when(this.itemFactory.createItemForNodeTest(nodeTest)).thenReturn(nodeTestItem);

		when(this.expression.getAxis()).thenReturn(AxisInfo.SELF);
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		final ISchemaElementProxy contextItem = mock();

		// setup the node test to let the node pass
		when(nodeTestItem.filter(Set.of(contextItem))).thenReturn(ImmutableList.of(contextItem));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// ensure that the node test was called
		verify(nodeTestItem, times(1)).evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should only contain all one attribute
		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));
	}

	@Test
	void testEvaluation_AxisDescendant_WithoutNodeTest() {
		when(this.expression.getAxis()).thenReturn(AxisInfo.DESCENDANT);
		when(this.expression.getNodeTest()).thenReturn(null);

		final ISchemaElementProxy subItem1 = mock("sub 1");
		final ISchemaElementProxy subItem1a = mock("sub 1a");
		final ISchemaElementProxy subItem1b = mock("sub 1b");
		final ISchemaElementProxy subItem2 = mock("sub 2");
		final ISchemaElementProxy subItem2a = mock("sub 2a");
		final ISchemaElementProxy subItem2b = mock("sub 2b");
		final ISchemaElementProxy contextItem = mock("context");
		when(subItem1a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1.getSubElements()).thenReturn(ImmutableList.of(subItem1a, subItem1b));
		when(subItem2.getSubElements()).thenReturn(ImmutableList.of(subItem2a, subItem2b));
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(subItem1, subItem2));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain all of the sub nodes
		assertEquals(6, result.size());
		assertTrue(result.contains(subItem1));
		assertTrue(result.contains(subItem1a));
		assertTrue(result.contains(subItem1b));
		assertTrue(result.contains(subItem2));
		assertTrue(result.contains(subItem2a));
		assertTrue(result.contains(subItem2b));
	}

	@Test
	void testEvaluation_AxisDescendant_WithNodeTest() {
		final NodeTest nodeTest = mock();
		final INodeTestTreeItem nodeTestItem = mock();
		when(this.itemFactory.createItemForNodeTest(nodeTest)).thenReturn(nodeTestItem);

		when(this.expression.getAxis()).thenReturn(AxisInfo.DESCENDANT);
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		final ISchemaElementProxy subItem1 = mock("sub 1");
		final ISchemaElementProxy subItem1a = mock("sub 1a");
		final ISchemaElementProxy subItem1b = mock("sub 1b");
		final ISchemaElementProxy subItem2 = mock("sub 2");
		final ISchemaElementProxy subItem2a = mock("sub 2a");
		final ISchemaElementProxy subItem2b = mock("sub 2b");
		final ISchemaElementProxy contextItem = mock("context");
		when(subItem1a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1.getSubElements()).thenReturn(ImmutableList.of(subItem1a, subItem1b));
		when(subItem2.getSubElements()).thenReturn(ImmutableList.of(subItem2a, subItem2b));
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(subItem1, subItem2));

		// setup the node test to some of the nodes pass
		when(nodeTestItem.filter(Set.of(subItem1, subItem2, subItem1a, subItem1b, subItem2a, subItem2b)))
			.thenReturn(ImmutableList.of(subItem1, subItem1a, subItem2b));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain the selected nodes
		assertEquals(3, result.size());
		assertTrue(result.contains(subItem1));
		assertTrue(result.contains(subItem1a));
		assertTrue(result.contains(subItem2b));
	}

	@Test
	void testEvaluation_AxisDescendantOrSelf_WithoutNodeTest() {
		when(this.expression.getAxis()).thenReturn(AxisInfo.DESCENDANT_OR_SELF);
		when(this.expression.getNodeTest()).thenReturn(null);

		final ISchemaElementProxy subItem1 = mock("sub 1");
		final ISchemaElementProxy subItem1a = mock("sub 1a");
		final ISchemaElementProxy subItem1b = mock("sub 1b");
		final ISchemaElementProxy subItem2 = mock("sub 2");
		final ISchemaElementProxy subItem2a = mock("sub 2a");
		final ISchemaElementProxy subItem2b = mock("sub 2b");
		final ISchemaElementProxy contextItem = mock("context");
		when(subItem1a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1.getSubElements()).thenReturn(ImmutableList.of(subItem1a, subItem1b));
		when(subItem2.getSubElements()).thenReturn(ImmutableList.of(subItem2a, subItem2b));
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(subItem1, subItem2));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain all of the sub nodes
		assertEquals(7, result.size());
		assertTrue(result.contains(subItem1));
		assertTrue(result.contains(subItem1a));
		assertTrue(result.contains(subItem1b));
		assertTrue(result.contains(subItem2));
		assertTrue(result.contains(subItem2a));
		assertTrue(result.contains(subItem2b));
		assertTrue(result.contains(contextItem));
	}

	@Test
	void testEvaluation_AxisDescendantOrSelf_WithNodeTest() {
		final NodeTest nodeTest = mock();
		final INodeTestTreeItem nodeTestItem = mock();
		when(this.itemFactory.createItemForNodeTest(nodeTest)).thenReturn(nodeTestItem);

		when(this.expression.getAxis()).thenReturn(AxisInfo.DESCENDANT_OR_SELF);
		when(this.expression.getNodeTest()).thenReturn(nodeTest);

		final ISchemaElementProxy subItem1 = mock("sub 1");
		final ISchemaElementProxy subItem1a = mock("sub 1a");
		final ISchemaElementProxy subItem1b = mock("sub 1b");
		final ISchemaElementProxy subItem2 = mock("sub 2");
		final ISchemaElementProxy subItem2a = mock("sub 2a");
		final ISchemaElementProxy subItem2b = mock("sub 2b");
		final ISchemaElementProxy contextItem = mock("context");
		when(subItem1a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2a.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem2b.getSubElements()).thenReturn(ImmutableList.of());
		when(subItem1.getSubElements()).thenReturn(ImmutableList.of(subItem1a, subItem1b));
		when(subItem2.getSubElements()).thenReturn(ImmutableList.of(subItem2a, subItem2b));
		when(contextItem.getSubElements()).thenReturn(ImmutableList.of(subItem1, subItem2));

		// setup the node test to some of the nodes pass
		when(nodeTestItem.filter(Set.of(contextItem, subItem1, subItem2, subItem1a, subItem1b, subItem2a, subItem2b)))
			.thenReturn(ImmutableList.of(contextItem, subItem1, subItem1a, subItem2b));

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		// the axis expression itself does not record any access, this is done by the node test
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// the result set should contain the selected nodes
		assertEquals(4, result.size());
		assertTrue(result.contains(contextItem));
		assertTrue(result.contains(subItem1));
		assertTrue(result.contains(subItem1a));
		assertTrue(result.contains(subItem2b));
	}

}
