package org.x2vc.schema.evolution;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.items.IEvaluationTreeItem;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactory;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactoryFactory;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableMultimap;

import net.sf.saxon.expr.Expression;

@ExtendWith(MockitoExtension.class)
class ValueTraceAnalyzerTest {

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ValueTraceAnalyzer#analyzeDocument(java.util.UUID, org.x2vc.processor.IHTMLDocumentContainer, java.util.function.Consumer)}.
	 */
	@Test
	void testAnalyzeDocument() {
		// prepare schema access
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "mySchema");
		final int schemaVersion = 42;
		final ISchemaManager schemaManager = mock(ISchemaManager.class);
		final IXMLSchema schema = mock(IXMLSchema.class);
		when(schemaManager.getSchema(stylesheetURI, schemaVersion)).thenReturn(schema);

		// prepare XML container
		final IXMLDocumentContainer xmlContainer = mock(IXMLDocumentContainer.class);
		when(xmlContainer.getStylesheeURI()).thenReturn(stylesheetURI);
		when(xmlContainer.getSchemaVersion()).thenReturn(schemaVersion);

		// prepare HTML container
		final IHTMLDocumentContainer htmlContainer = mock(IHTMLDocumentContainer.class);
		when(htmlContainer.getSource()).thenReturn(xmlContainer);

		// prepare test proxies and expressions
		final ISchemaElementProxy proxy1 = mock(ISchemaElementProxy.class, "proxy 1");
		final ISchemaElementProxy proxy2 = mock(ISchemaElementProxy.class, "proxy 2");
		final Expression expression1a = mock(Expression.class, "expression 1a");
		final Expression expression1b = mock(Expression.class, "expression 1b");
		final Expression expression2a = mock(Expression.class, "expression 2a");
		final Expression expression2b = mock(Expression.class, "expression 2b");

		// use a "real" Multimap to avoid mocking a sh*tload of methods
		final ImmutableMultimap<ISchemaElementProxy, Expression> preprocessedExpressions = ImmutableMultimap.of(
				proxy1, expression1a,
				proxy1, expression1b,
				proxy2, expression2a,
				proxy2, expression2b);

		// prepare preprocessor
		final IValueTracePreprocessor valueTracePreprocessor = mock(IValueTracePreprocessor.class);
		when(valueTracePreprocessor.prepareEvents(htmlContainer)).thenReturn(preprocessedExpressions);

		// prepare modifier collector
		@SuppressWarnings("unchecked")
		final Consumer<ISchemaModifier> modifierCollector = mock(Consumer.class);

		// prepare the modifier creation coordinator structure
		final IModifierCreationCoordinator modifierCreationCoordinator = mock(IModifierCreationCoordinator.class);
		final IModifierCreationCoordinatorFactory modifierCreationCoordinatorFactory = mock(
				IModifierCreationCoordinatorFactory.class);
		when(modifierCreationCoordinatorFactory.createCoordinator(schema, modifierCollector))
			.thenReturn(modifierCreationCoordinator);

		// prepare the evaluation tree structure
		final IEvaluationTreeItem item1a = mock(IEvaluationTreeItem.class, "item 1a");
		final IEvaluationTreeItem item1b = mock(IEvaluationTreeItem.class, "item 1b");
		final IEvaluationTreeItem item2a = mock(IEvaluationTreeItem.class, "item 2a");
		final IEvaluationTreeItem item2b = mock(IEvaluationTreeItem.class, "item 2b");
		final IEvaluationTreeItemFactory evaluationTreeItemFactory = mock(IEvaluationTreeItemFactory.class);
		when(evaluationTreeItemFactory.createItemForExpression(expression1a)).thenReturn(item1a);
		when(evaluationTreeItemFactory.createItemForExpression(expression1b)).thenReturn(item1b);
		when(evaluationTreeItemFactory.createItemForExpression(expression2a)).thenReturn(item2a);
		when(evaluationTreeItemFactory.createItemForExpression(expression2b)).thenReturn(item2b);
		final IEvaluationTreeItemFactoryFactory evaluationTreeItemFactoryFactory = mock(
				IEvaluationTreeItemFactoryFactory.class);
		when(evaluationTreeItemFactoryFactory.createFactory(schema, modifierCreationCoordinator))
			.thenReturn(evaluationTreeItemFactory);

		// prepare and call call object under test
		final UUID taskID = UUID.randomUUID();
		final ValueTraceAnalyzer analyzer = new ValueTraceAnalyzer(schemaManager, valueTracePreprocessor,
				evaluationTreeItemFactoryFactory, modifierCreationCoordinatorFactory);
		analyzer.analyzeDocument(taskID, htmlContainer, modifierCollector);

		// verify that the item initialization was performed
		verify(evaluationTreeItemFactory, times(4)).initializeAllCreatedItems();

		// verify that the item methods were called with the right parameters
		verify(item1a).evaluate(proxy1);
		verify(item1b).evaluate(proxy1);
		verify(item2a).evaluate(proxy2);
		verify(item2b).evaluate(proxy2);

		// verify that the modifiers were sent to the collector
		verify(modifierCreationCoordinator, times(1)).flush();
	}

}
