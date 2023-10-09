package org.x2vc.schema.evolution;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.ExecutionTraceEvent;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ValueAccessTraceEvent;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.ISchemaElementProxy.ProxyType;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IGenerationRule;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Expression;

@ExtendWith(MockitoExtension.class)
class ValueTracePreprocessorTest {

	@Mock
	private ISchemaManager schemaManager;

	@Mock
	private IXMLSchema schema;

	@Mock
	private IXMLDocumentContainer xmlContainer;

	@Mock
	private IDocumentRequest request;

	@Mock
	private IXMLDocumentDescriptor xmlDescriptor;

	@Mock
	private IHTMLDocumentContainer htmlContainer;

	@Mock
	private Map<UUID, UUID> traceIDToRuleIDMap;

	private ValueTracePreprocessor preprocessor;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// connect containers and descriptors
		lenient().when(this.htmlContainer.getSource()).thenReturn(this.xmlContainer);
		lenient().when(this.xmlContainer.getDocumentDescriptor()).thenReturn(this.xmlDescriptor);
		lenient().when(this.xmlContainer.getRequest()).thenReturn(this.request);
		lenient().when(this.xmlDescriptor.getTraceIDToRuleIDMap()).thenReturn(this.traceIDToRuleIDMap);

		// wire up schema access
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "myStylesheet");
		final int schemaVersion = 42;
		lenient().when(this.schemaManager.getSchema(stylesheetURI, schemaVersion)).thenReturn(this.schema);
		lenient().when(this.xmlContainer.getStylesheeURI()).thenReturn(stylesheetURI);
		lenient().when(this.xmlContainer.getSchemaVersion()).thenReturn(schemaVersion);
		lenient().when(this.request.getStylesheeURI()).thenReturn(stylesheetURI);
		lenient().when(this.request.getSchemaVersion()).thenReturn(schemaVersion);

		this.preprocessor = new ValueTracePreprocessor(this.schemaManager);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_NoEvents() {
		when(this.htmlContainer.getTraceEvents()).thenReturn(Optional.empty());

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor.prepareEvents(this.htmlContainer);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_EmptyEventListEvents() {
		when(this.htmlContainer.getTraceEvents()).thenReturn(Optional.of(ImmutableList.of()));

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor.prepareEvents(this.htmlContainer);

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_NoValueTraceEvents() {
		final ExecutionTraceEvent event1 = ExecutionTraceEvent
			.builder()
			.withEventType(ExecutionEventType.ENTER)
			.build();
		final ExecutionTraceEvent event2 = ExecutionTraceEvent
			.builder()
			.withEventType(ExecutionEventType.LEAVE)
			.build();
		when(this.htmlContainer.getTraceEvents()).thenReturn(Optional.of(ImmutableList.of(event1, event2)));

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor
			.prepareEvents(this.htmlContainer);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_ValueTraceEvent_Element_Simple() {
		// simple case for N = [1, 2]
		// eventN
		// --> traceIDN
		// ------> ruleIDn
		// ----------> ruleN
		// ----------> referenceN(referenceIDN)
		// --> expressionN

		final UUID referenceID1 = UUID.randomUUID();
		final UUID referenceID2 = UUID.randomUUID();
		final UUID traceID1 = UUID.randomUUID();
		final UUID traceID2 = UUID.randomUUID();
		final UUID ruleID1 = UUID.randomUUID();
		final UUID ruleID2 = UUID.randomUUID();

		when(this.traceIDToRuleIDMap.get(traceID1)).thenReturn(ruleID1);
		when(this.traceIDToRuleIDMap.get(traceID2)).thenReturn(ruleID2);
		when(this.traceIDToRuleIDMap.containsKey(traceID1)).thenReturn(true);
		when(this.traceIDToRuleIDMap.containsKey(traceID2)).thenReturn(true);

		final IElementReference reference1 = mock(IElementReference.class);
		final IElementReference reference2 = mock(IElementReference.class);
		when(reference1.getID()).thenReturn(referenceID1);
		when(reference2.getID()).thenReturn(referenceID2);
		lenient().when(this.schema.getObjectByID(referenceID1, IElementReference.class)).thenReturn(reference1);
		lenient().when(this.schema.getObjectByID(referenceID2, IElementReference.class)).thenReturn(reference2);
		lenient().when(this.schema.getObjectByID(referenceID1)).thenReturn(reference1);
		lenient().when(this.schema.getObjectByID(referenceID2)).thenReturn(reference2);

		final IGenerationRule rule1 = mock(IGenerationRule.class);
		final IGenerationRule rule2 = mock(IGenerationRule.class);
		when(this.request.getRuleByID(ruleID1)).thenReturn(rule1);
		when(this.request.getRuleByID(ruleID2)).thenReturn(rule2);
		when(rule1.getSchemaObjectID()).thenReturn(Optional.of(referenceID1));
		when(rule2.getSchemaObjectID()).thenReturn(Optional.of(referenceID2));

		final Expression expression1 = mock(Expression.class);
		final Expression expression2 = mock(Expression.class);

		final ValueAccessTraceEvent event1 = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID1)
			.withExpression(expression1)
			.build();
		final ValueAccessTraceEvent event2 = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID2)
			.withExpression(expression2)
			.build();
		when(this.htmlContainer.getTraceEvents()).thenReturn(Optional.of(ImmutableList.of(event1, event2)));

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor
			.prepareEvents(this.htmlContainer);
		assertNotNull(result);

		final ImmutableSet<ISchemaElementProxy> resultKeys = result.keySet();
		assertEquals(2, resultKeys.size());
		for (final ISchemaElementProxy resultKey : resultKeys) {
			assertEquals(ProxyType.ELEMENT, resultKey.getType());
			final UUID referenceID = resultKey.getElementReference().orElseThrow().getID();

			final Optional<IElementReference> oElementReference = resultKey.getElementReference();
			final ImmutableCollection<Expression> expressions = result.get(resultKey);

			if (referenceID.equals(referenceID1)) {
				assertTrue(oElementReference.isPresent());
				assertSame(reference1, oElementReference.get());
				assertEquals(1, expressions.size());
				assertTrue(expressions.contains(expression1));

			} else if (referenceID.equals(referenceID2)) {
				assertTrue(oElementReference.isPresent());
				assertSame(reference2, oElementReference.get());
				assertEquals(1, expressions.size());
				assertTrue(expressions.contains(expression2));

			} else {
				fail("unexpected result key encountered");
			}
		}
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_ValueTraceEvent_Element_Merge() {
		// more complex case for N = [1, 2] and X = [a, b]
		// eventNX
		// --> traceIDNX
		// ------> ruleIDNX
		// ----------> ruleNX
		// ----------> referenceN(referenceIDN) - these stay the same, expressions have to be collected!
		// --> expressionNX

		final UUID referenceID1 = UUID.randomUUID();
		final UUID referenceID2 = UUID.randomUUID();
		final UUID traceID1a = UUID.randomUUID();
		final UUID traceID1b = UUID.randomUUID();
		final UUID traceID2a = UUID.randomUUID();
		final UUID traceID2b = UUID.randomUUID();
		final UUID ruleID1a = UUID.randomUUID();
		final UUID ruleID1b = UUID.randomUUID();
		final UUID ruleID2a = UUID.randomUUID();
		final UUID ruleID2b = UUID.randomUUID();

		when(this.traceIDToRuleIDMap.get(traceID1a)).thenReturn(ruleID1a);
		when(this.traceIDToRuleIDMap.get(traceID1b)).thenReturn(ruleID1b);
		when(this.traceIDToRuleIDMap.get(traceID2a)).thenReturn(ruleID2a);
		when(this.traceIDToRuleIDMap.get(traceID2b)).thenReturn(ruleID2b);
		when(this.traceIDToRuleIDMap.containsKey(traceID1a)).thenReturn(true);
		when(this.traceIDToRuleIDMap.containsKey(traceID1b)).thenReturn(true);
		when(this.traceIDToRuleIDMap.containsKey(traceID2a)).thenReturn(true);
		when(this.traceIDToRuleIDMap.containsKey(traceID2b)).thenReturn(true);

		final IElementReference reference1 = mock(IElementReference.class);
		final IElementReference reference2 = mock(IElementReference.class);
		when(reference1.getID()).thenReturn(referenceID1);
		when(reference2.getID()).thenReturn(referenceID2);
		lenient().when(this.schema.getObjectByID(referenceID1, IElementReference.class)).thenReturn(reference1);
		lenient().when(this.schema.getObjectByID(referenceID2, IElementReference.class)).thenReturn(reference2);
		lenient().when(this.schema.getObjectByID(referenceID1)).thenReturn(reference1);
		lenient().when(this.schema.getObjectByID(referenceID2)).thenReturn(reference2);

		final IGenerationRule rule1a = mock(IGenerationRule.class);
		final IGenerationRule rule1b = mock(IGenerationRule.class);
		final IGenerationRule rule2a = mock(IGenerationRule.class);
		final IGenerationRule rule2b = mock(IGenerationRule.class);
		when(this.request.getRuleByID(ruleID1a)).thenReturn(rule1a);
		when(this.request.getRuleByID(ruleID1b)).thenReturn(rule1b);
		when(this.request.getRuleByID(ruleID2a)).thenReturn(rule2a);
		when(this.request.getRuleByID(ruleID2b)).thenReturn(rule2b);
		when(rule1a.getSchemaObjectID()).thenReturn(Optional.of(referenceID1));
		when(rule1b.getSchemaObjectID()).thenReturn(Optional.of(referenceID1));
		when(rule2a.getSchemaObjectID()).thenReturn(Optional.of(referenceID2));
		when(rule2b.getSchemaObjectID()).thenReturn(Optional.of(referenceID2));

		final Expression expression1a = mock(Expression.class);
		final Expression expression1b = mock(Expression.class);
		final Expression expression2a = mock(Expression.class);
		final Expression expression2b = mock(Expression.class);

		final ValueAccessTraceEvent event1a = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID1a)
			.withExpression(expression1a)
			.build();
		final ValueAccessTraceEvent event1b = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID1b)
			.withExpression(expression1b)
			.build();
		final ValueAccessTraceEvent event2a = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID2a)
			.withExpression(expression2a)
			.build();
		final ValueAccessTraceEvent event2b = ValueAccessTraceEvent.builder()
			.withContextElementID(traceID2b)
			.withExpression(expression2b)
			.build();
		when(this.htmlContainer.getTraceEvents())
			.thenReturn(Optional.of(ImmutableList.of(event1a, event1b, event2a, event2b)));

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor
			.prepareEvents(this.htmlContainer);
		assertNotNull(result);

		final ImmutableSet<ISchemaElementProxy> resultKeys = result.keySet();
		assertEquals(2, resultKeys.size());
		for (final ISchemaElementProxy resultKey : resultKeys) {
			assertEquals(ProxyType.ELEMENT, resultKey.getType());
			final UUID referenceID = resultKey.getElementReference().orElseThrow().getID();

			final Optional<IElementReference> oElementReference = resultKey.getElementReference();
			final ImmutableCollection<Expression> expressions = result.get(resultKey);

			if (referenceID.equals(referenceID1)) {
				assertTrue(oElementReference.isPresent());
				assertSame(reference1, oElementReference.get());
				assertEquals(2, expressions.size());
				assertTrue(expressions.contains(expression1a));
				assertTrue(expressions.contains(expression1b));

			} else if (referenceID.equals(referenceID2)) {
				assertTrue(oElementReference.isPresent());
				assertSame(reference2, oElementReference.get());
				assertEquals(2, expressions.size());
				assertTrue(expressions.contains(expression2a));
				assertTrue(expressions.contains(expression2b));

			} else {
				fail("unexpected result key encountered");
			}
		}
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTracePreprocessor#prepareEvents(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testPrepareEvents_ValueTraceEvent_DocumentReference() {
		// eventN
		// --> traceIDN == documentRootID
		// --> expressionN

		final UUID documentTraceID = UUID.randomUUID();
		when(this.htmlContainer.getDocumentTraceID()).thenReturn(documentTraceID);

		final Expression expression1 = mock(Expression.class);
		final Expression expression2 = mock(Expression.class);

		final ValueAccessTraceEvent event1 = ValueAccessTraceEvent.builder()
			.withContextElementID(documentTraceID)
			.withExpression(expression1)
			.build();
		final ValueAccessTraceEvent event2 = ValueAccessTraceEvent.builder()
			.withContextElementID(documentTraceID)
			.withExpression(expression2)
			.build();
		when(this.htmlContainer.getTraceEvents()).thenReturn(Optional.of(ImmutableList.of(event1, event2)));

		final ImmutableMultimap<ISchemaElementProxy, Expression> result = this.preprocessor
			.prepareEvents(this.htmlContainer);
		assertNotNull(result);

		final ImmutableSet<ISchemaElementProxy> resultKeys = result.keySet();
		assertEquals(1, resultKeys.size());
		for (final ISchemaElementProxy resultKey : resultKeys) {
			assertEquals(ProxyType.DOCUMENT, resultKey.getType());
			final ImmutableCollection<Expression> expressions = result.get(resultKey);
			assertEquals(2, expressions.size());
			assertTrue(expressions.contains(expression1));
			assertTrue(expressions.contains(expression2));
		}

	}

	// TODO ValueTracePreprocessor: add test: event without context ID should be ignored
	// TODO ValueTracePreprocessor: add test: resolve IElementReference
	// TODO ValueTracePreprocessor: add test: events pointing to other schema objects should be ignored
	// TODO ValueTracePreprocessor: add test: rules not relating to schema elements should be ignored
	// TODO ValueTracePreprocessor: add test: event referring to non-existent rule ID not in map should be ignored
	// TODO ValueTracePreprocessor: add test: event referring to non-existent rule not in request should be ignored

}
