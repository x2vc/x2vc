package org.x2vc.schema.evolution.items;

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

import net.sf.saxon.expr.AttributeGetter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;

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
		verify(this.itemFactory, never()).createItem(any(Expression.class));
		verify(this.itemFactory, never()).createItem(any(NodeTest.class));
		// access may not be recorded in the initialization phase
		verify(this.coordinator, never()).handleAttributeAccess(any(), any());
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

	@Test
	void testEvaluation() {
		final StructuredQName attributeName = new StructuredQName("pfx", "foo://bar", "baz");
		final FingerprintedQName fingerprintedAttributeName = mock(FingerprintedQName.class);
		when(fingerprintedAttributeName.getStructuredQName()).thenReturn(attributeName);
		when(this.expression.getAttributeName()).thenReturn(fingerprintedAttributeName);
		final ISchemaElementProxy contextItem = mock();
		this.treeItem.evaluate(contextItem);
		verify(this.coordinator).handleAttributeAccess(contextItem, attributeName);
		verify(this.coordinator, never()).handleElementAccess(any(), any());
	}

}
