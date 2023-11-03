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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.AttributeGetter;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.StructuredQName;

@ExtendWith(MockitoExtension.class)
class AttributeGetterItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private AttributeGetter expression;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private AttributeGetterItem treeItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.treeItem = new AttributeGetterItem(this.schema, this.coordinator, this.expression);
	}

	@Test
	void testInitialization() {
		this.treeItem.initialize(this.itemFactory);

		// no subordinate items required for this item type
		verify(this.itemFactory, never()).createItemForExpression(any());
		verify(this.itemFactory, never()).createItemForNodeTest(any());

		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final StructuredQName attributeName = new StructuredQName("pfx", "foo://bar", "baz");
		final FingerprintedQName fingerprintedAttributeName = mock();
		when(fingerprintedAttributeName.getStructuredQName()).thenReturn(attributeName);
		when(this.expression.getAttributeName()).thenReturn(fingerprintedAttributeName);

		final ISchemaElementProxy contextItem = mock();

		this.treeItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> result = this.treeItem.evaluate(contextItem);

		verify(this.coordinator).handleAttributeAccess(contextItem, attributeName);
		verify(this.coordinator, never()).handleElementAccess(any(), any());

		assertEquals(1, result.size());
		assertTrue(result.contains(contextItem));

	}

}
