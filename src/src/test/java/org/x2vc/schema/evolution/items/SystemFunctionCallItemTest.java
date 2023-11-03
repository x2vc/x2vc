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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.evolution.ISchemaElementProxy.ProxyType;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;

@ExtendWith(MockitoExtension.class)

class SystemFunctionCallItemTest {

	@Mock
	private IXMLSchema schema;
	@Mock
	private IModifierCreationCoordinator coordinator;
	@Mock
	private SystemFunctionCall function;
	@Mock
	private IEvaluationTreeItemFactory itemFactory;

	private SystemFunctionCallItem functionItem;

	@Mock(name = "context item")
	private ISchemaElementProxy contextItem;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.functionItem = new SystemFunctionCallItem(this.schema, this.coordinator, this.function);
	}

	@ParameterizedTest
	@CsvSource({
			"boolean,            1",
			"ceiling,            1",
			"concat,             3",
			"contains,           3",
			"count,              1",
			"element-available,  1",
			"false,              0",
			"empty,              1",
			"exists,             1",
			"floor,              1",
			"format-number,      3",
			"function-available, 1",
			"generate-id,        1",
			"lang,               2",
			"last,               1",
			"local-name,         1",
			"name,               1",
			"namespace-uri,      1",
			"normalize-space,    1",
			"not,                1",
			"number,             1",
			"position,           0",
			"round,              1",
			"starts-with,        2",
			"string,             1",
			"string-join,        2",
			"string-length,      1",
			"substring,          3",
			"substring-after,    3",
			"substring-before,   3",
			"sum,                1",
			"system-property,    1",
			"translate,          3",
			"true,               0",

	})
	void testFunctionWithoutAccess(String name, int numArguments) {
		// prepare the wrapped function
		mockSystemFunction(name);

		Expression[] args = null;
		IEvaluationTreeItem[] argItems = null;
		args = mockArguments(numArguments);
		argItems = mockArgumentItems(args);

		// initialize and evaluate
		final ImmutableCollection<ISchemaElementProxy> resultItems = initializeAndEvaluateFunctionItem();

		// context item should be unchanged
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(this.contextItem));

		// this function does not perform any access itself
		assertNoAttributeAccessRecorded();
		assertNoElementAccessRecorded();

		// the arguments all have to be evaluated
		assertArgumentsEvaluated(argItems, this.contextItem);

	}

	@Test
	void testCurrent_Attribute() {
		// prepare the wrapped function
		mockSystemFunction("current");
		mockNoArguments();

		// let the context item emulate an attribute
		lenient().when(this.contextItem.getType()).thenReturn(ProxyType.ATTRIBUTE);
		lenient().when(this.contextItem.isAttribute()).thenReturn(true);
		when(this.contextItem.getAttributeName()).thenReturn(Optional.of("attributeName"));

		// initialize and evaluate
		final ImmutableCollection<ISchemaElementProxy> resultItems = initializeAndEvaluateFunctionItem();

		// context item should be unchanged
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(this.contextItem));

		// should yield an attribute access
		verify(this.coordinator).handleAttributeAccess(this.contextItem,
				new StructuredQName("", NamespaceUri.NULL, "attributeName"));
		assertNoElementAccessRecorded();
	}

	@Test
	void testCurrent_AttributeModifier() {
		// prepare the wrapped function
		mockSystemFunction("current");
		mockNoArguments();

		// let the context item emulate an attribute modifier
		lenient().when(this.contextItem.getType()).thenReturn(ProxyType.ATTRIBUTE_MODIFIER);
		lenient().when(this.contextItem.isAttributeModifier()).thenReturn(true);
		when(this.contextItem.getAttributeName()).thenReturn(Optional.of("attributeName"));

		// initialize and evaluate
		final ImmutableCollection<ISchemaElementProxy> resultItems = initializeAndEvaluateFunctionItem();

		// context item should be unchanged
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(this.contextItem));

		// should yield an attribute access
		verify(this.coordinator).handleAttributeAccess(this.contextItem,
				new StructuredQName("", NamespaceUri.NULL, "attributeName"));
		assertNoElementAccessRecorded();
	}

	@Test
	void testCurrent_Element() {
		// prepare the wrapped function
		mockSystemFunction("current");
		mockNoArguments();

		// let the context item emulate an element
		lenient().when(this.contextItem.getType()).thenReturn(ProxyType.ELEMENT);
		lenient().when(this.contextItem.isElement()).thenReturn(true);
		when(this.contextItem.getElementName()).thenReturn(Optional.of("elementName"));

		// initialize and evaluate
		final ImmutableCollection<ISchemaElementProxy> resultItems = initializeAndEvaluateFunctionItem();

		// context item should be unchanged
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(this.contextItem));

		// should yield an attribute access
		assertNoAttributeAccessRecorded();
		verify(this.coordinator).handleElementAccess(this.contextItem,
				new StructuredQName("", NamespaceUri.NULL, "elementName"));
	}

	@Test
	void testCurrent_ElementModifier() {
		// prepare the wrapped function
		mockSystemFunction("current");
		mockNoArguments();

		// let the context item emulate an element modifier
		lenient().when(this.contextItem.getType()).thenReturn(ProxyType.ELEMENT_MODIFIER);
		lenient().when(this.contextItem.isElementModifier()).thenReturn(true);
		when(this.contextItem.getElementName()).thenReturn(Optional.of("elementName"));

		// initialize and evaluate
		final ImmutableCollection<ISchemaElementProxy> resultItems = initializeAndEvaluateFunctionItem();

		// context item should be unchanged
		assertEquals(1, resultItems.size());
		assertTrue(resultItems.contains(this.contextItem));

		// should yield an attribute access
		assertNoAttributeAccessRecorded();
		verify(this.coordinator).handleElementAccess(this.contextItem,
				new StructuredQName("", NamespaceUri.NULL, "elementName"));
	}

	@Test
	@Disabled("document function not yet supported")
	void testDocument() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("id function not yet supported")
	void testId() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("key function not yet supported")
	void testKey() {
		fail("Not yet implemented");
	}

	@Test
	@Disabled("unparsed-entity-uri function not yet supported")
	void testUnparsedEntityUri() {
		fail("Not yet implemented");
	}

	// ----- auxiliary functions --------------------------------------------------------------------------------------

	private void mockSystemFunction(String name) {
		final StructuredQName functionName = new StructuredQName("", NamespaceUri.FN, name);
		when(this.function.getFunctionName()).thenReturn(functionName);
	}

	protected void mockNoArguments() {
		when(this.function.getArguments()).thenReturn(new Expression[] { });
	}

	protected Expression mockSingleArgument() {
		final Expression arg1 = mock("arg1");
		when(this.function.getArguments()).thenReturn(new Expression[] { arg1 });
		return arg1;
	}

	protected IEvaluationTreeItem mockSingleArgumentItem(final Expression arg1) {
		final IEvaluationTreeItem arg1Item = mock("arg1 item");
		when(this.itemFactory.createItemForExpression(arg1)).thenReturn(arg1Item);
		return arg1Item;
	}

	private Expression[] mockArguments(int number) {
		final Expression[] result = new Expression[number];
		for (int i = 0; i < result.length; i++) {
			result[i] = mock(String.format("arg%d", i + 1));
		}
		when(this.function.getArguments()).thenReturn(result);
		return result;
	}

	private IEvaluationTreeItem[] mockArgumentItems(Expression[] args) {
		final IEvaluationTreeItem[] result = new IEvaluationTreeItem[args.length];
		for (int i = 0; i < args.length; i++) {
			result[i] = mock(String.format("arg%d item ", i + 1));
			when(this.itemFactory.createItemForExpression(args[i])).thenReturn(result[i]);
		}
		return result;
	}

	protected ImmutableCollection<ISchemaElementProxy> initializeAndEvaluateFunctionItem() {
		this.functionItem.initialize(this.itemFactory);
		final ImmutableCollection<ISchemaElementProxy> resultItems = this.functionItem.evaluate(this.contextItem);
		return resultItems;
	}

	protected void assertNoElementAccessRecorded() {
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	protected void assertNoAttributeAccessRecorded() {
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
	}

	protected void assertArgumentEvaluated(final IEvaluationTreeItem arg1Item, final ISchemaElementProxy evalItem) {
		verify(arg1Item, times(1)).evaluate(evalItem);
	}

	private void assertArgumentsEvaluated(IEvaluationTreeItem[] argItems, ISchemaElementProxy evalItem) {
		for (int i = 0; i < argItems.length; i++) {
			verify(argItems[i], times(1)).evaluate(evalItem);
		}
	}

}
