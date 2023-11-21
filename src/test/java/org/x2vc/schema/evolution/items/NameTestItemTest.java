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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Optional;
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

import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.type.Type;

@ExtendWith(MockitoExtension.class)
class NameTestItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private NameTest nameTest;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private NameTestItem nameTestItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.nameTestItem = new NameTestItem(this.schema, this.coordinator, this.nameTest);
	}

	@Test
	void testElement() {
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "fooBar");
		when(this.nameTest.getNodeKind()).thenReturn((int) Type.ELEMENT);
		when(this.nameTest.getMatchingNodeName()).thenReturn(elementName);

		this.nameTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nameTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// ensure the element access was registered
		verify(this.coordinator, times(1)).handleElementAccess(contextItem, elementName);
		verify(this.coordinator, never()).handleAttributeAccess(eq(contextItem), any());

		// prepare some elements to filter
		final ISchemaElementProxy elem1 = mock("elem1");
		final ISchemaElementProxy elem2 = mock("elem2");
		final ISchemaElementProxy elem3 = mock("elem3");
		final ISchemaElementProxy elem4 = mock("elem4");
		final Collection<ISchemaElementProxy> elems = Set.of(elem1, elem2, elem3, elem4);

		// prepare some element names for filtering
		when(elem1.getElementName()).thenReturn(Optional.of("elem1"));
		when(elem2.getElementName()).thenReturn(Optional.of("elem2"));
		when(elem3.getElementName()).thenReturn(Optional.of("fooBar"));
		when(elem4.getElementName()).thenReturn(Optional.of("elem4"));

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nameTestItem.filter(elems);
		assertEquals(1, resultElems.size());
		assertTrue(resultElems.contains(elem3));

	}

	@Test
	void testAttribute() {
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "fooBar");
		when(this.nameTest.getNodeKind()).thenReturn((int) Type.ATTRIBUTE);
		when(this.nameTest.getMatchingNodeName()).thenReturn(attributeName);

		this.nameTestItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		// evaluate the node test itself (should not change the context)
		final ISchemaElementProxy contextItem = mock();
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.nameTestItem.evaluate(contextItem);
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(contextItem));

		// ensure the element access was registered
		verify(this.coordinator, times(1)).handleAttributeAccess(contextItem, attributeName);
		verify(this.coordinator, never()).handleElementAccess(eq(contextItem), any());

		// prepare some elements to filter
		final ISchemaElementProxy attrib1 = mock("attrib1");
		final ISchemaElementProxy attrib2 = mock("attrib2");
		final ISchemaElementProxy attrib3 = mock("attrib3");
		final ISchemaElementProxy attrib4 = mock("attrib4");
		final Collection<ISchemaElementProxy> elems = Set.of(attrib1, attrib2, attrib3, attrib4);

		// prepare some element names for filtering
		when(attrib1.getAttributeName()).thenReturn(Optional.of("attrib1"));
		when(attrib2.getAttributeName()).thenReturn(Optional.of("attrib2"));
		when(attrib3.getAttributeName()).thenReturn(Optional.of("fooBar"));
		when(attrib4.getAttributeName()).thenReturn(Optional.of("attrib4"));

		final ImmutableCollection<ISchemaElementProxy> resultElems = this.nameTestItem.filter(elems);
		assertEquals(1, resultElems.size());
		assertTrue(resultElems.contains(attrib3));

	}

}
