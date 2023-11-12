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

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.structure.IXMLSchema;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.pattern.NodeTest;

@ExtendWith(MockitoExtension.class)
class EvaluationTreeItemFactoryTest {

	@Mock
	private IXMLSchema schema;

	@Mock
	private IModifierCreationCoordinator coordinator;

	private EvaluationTreeItemFactory factory;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.factory = new EvaluationTreeItemFactory(this.schema, this.coordinator);
	}

	@Test
	void testCreateItem_Expression() {
		final Expression expression = mock();
		final IEvaluationTreeItem item = this.factory.createItemForExpression(expression);
		assertInstanceOf(UnsupportedExpressionItem.class, item);
		assertSame(this.schema, ((UnsupportedExpressionItem) item).getSchema());
	}

	@Test
	void testCreateItem_NodeTest() {
		final NodeTest nodeTest = mock();
		final IEvaluationTreeItem item = this.factory.createItemForNodeTest(nodeTest);
		assertInstanceOf(UnsupportedNodeTestItem.class, item);
		assertSame(this.schema, ((UnsupportedNodeTestItem) item).getSchema());
	}

	@Test
	void testInitializeAllCreatedItems_SingleItem() {
		final IEvaluationTreeItem item = mock();
		this.factory.injectUninitializedItem(item);
		this.factory.initializeAllCreatedItems();
		final ArgumentCaptor<IEvaluationTreeItemFactory> factoryCaptor = ArgumentCaptor
			.forClass(IEvaluationTreeItemFactory.class);
		verify(item).initialize(factoryCaptor.capture());
		assertSame(this.factory, factoryCaptor.getValue());
	}

	@Test
	void testInitializeAllCreatedItems_MultipleItems() {
		final IEvaluationTreeItem rootItem = mock();
		final IEvaluationTreeItem subItem1 = mock();
		final IEvaluationTreeItem subItem2 = mock();
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				EvaluationTreeItemFactoryTest.this.factory.injectUninitializedItem(subItem1);
				EvaluationTreeItemFactoryTest.this.factory.injectUninitializedItem(subItem2);
				return null;
			}
		}).when(rootItem).initialize(this.factory);
		this.factory.injectUninitializedItem(rootItem);
		this.factory.initializeAllCreatedItems();
		verify(rootItem).initialize(this.factory);
		verify(subItem1).initialize(this.factory);
		verify(subItem1).initialize(this.factory);
	}

}
