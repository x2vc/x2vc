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
import static org.mockito.Mockito.*;

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

import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.Type;

@ExtendWith(MockitoExtension.class)
class NodeKindTestItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private NodeKindTest nodeKindTest;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private NodeKindTestItem nodeKindTestItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.nodeKindTestItem = new NodeKindTestItem(this.schema, this.coordinator, this.nodeKindTest);
	}

	@Test
	void testElement() {
		when(this.nodeKindTest.getNodeKind()).thenReturn((int) Type.ELEMENT);

		this.nodeKindTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nodeKindTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// ensure that no access was registered
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// prepare some elements to filter
		final ISchemaElementProxy proxy1 = mock("proxy1 element");
		final ISchemaElementProxy proxy2 = mock("proxy2 element modifier");
		final ISchemaElementProxy proxy3 = mock("proxy3 attribute");
		final ISchemaElementProxy proxy4 = mock("proxy4 attribute modifier");
		final ISchemaElementProxy proxy5 = mock("proxy5 document");
		lenient().when(proxy1.isElement()).thenReturn(true);
		lenient().when(proxy2.isElementModifier()).thenReturn(true);
		lenient().when(proxy3.isAttribute()).thenReturn(true);
		lenient().when(proxy4.isAttributeModifier()).thenReturn(true);
		lenient().when(proxy5.isDocument()).thenReturn(true);
		final Collection<ISchemaElementProxy> elems = Set.of(proxy1, proxy2, proxy3, proxy4, proxy5);

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nodeKindTestItem.filter(elems);
		assertEquals(2, resultElems.size());
		assertTrue(resultElems.contains(proxy1));
		assertTrue(resultElems.contains(proxy2));
	}

	@Test
	void testAttribute() {
		when(this.nodeKindTest.getNodeKind()).thenReturn((int) Type.ATTRIBUTE);

		this.nodeKindTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nodeKindTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// ensure that no access was registered
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// prepare some elements to filter
		final ISchemaElementProxy proxy1 = mock("proxy1 element");
		final ISchemaElementProxy proxy2 = mock("proxy2 element modifier");
		final ISchemaElementProxy proxy3 = mock("proxy3 attribute");
		final ISchemaElementProxy proxy4 = mock("proxy4 attribute modifier");
		final ISchemaElementProxy proxy5 = mock("proxy5 document");
		lenient().when(proxy1.isElement()).thenReturn(true);
		lenient().when(proxy2.isElementModifier()).thenReturn(true);
		lenient().when(proxy3.isAttribute()).thenReturn(true);
		lenient().when(proxy4.isAttributeModifier()).thenReturn(true);
		lenient().when(proxy5.isDocument()).thenReturn(true);
		final Collection<ISchemaElementProxy> elems = Set.of(proxy1, proxy2, proxy3, proxy4, proxy5);

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nodeKindTestItem.filter(elems);
		assertEquals(2, resultElems.size());
		assertTrue(resultElems.contains(proxy3));
		assertTrue(resultElems.contains(proxy4));
		}

}
