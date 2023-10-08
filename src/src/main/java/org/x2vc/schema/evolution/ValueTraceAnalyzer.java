package org.x2vc.schema.evolution;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.items.IEvaluationTreeItem;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactory;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactoryFactory;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;

import net.sf.saxon.expr.Expression;

/**
 * Standard implementation of {@link IValueTraceAnalyzer}.
 */
public class ValueTraceAnalyzer implements IValueTraceAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private IValueTracePreprocessor valueTracePreprocessor;
	private IEvaluationTreeItemFactoryFactory evaluationTreeItemFactoryFactory;
	private IModifierCreationCoordinatorFactory modifierCreationCoordinatorFactory;

	@Inject
	protected ValueTraceAnalyzer(ISchemaManager schemaManager, IValueTracePreprocessor valueTracePreprocessor,
			IEvaluationTreeItemFactoryFactory evaluationTreeItemFactoryFactory,
			IModifierCreationCoordinatorFactory modifierCreationCoordinatorFactory) {
		super();
		this.schemaManager = schemaManager;
		this.valueTracePreprocessor = valueTracePreprocessor;
		this.evaluationTreeItemFactoryFactory = evaluationTreeItemFactoryFactory;
		this.modifierCreationCoordinatorFactory = modifierCreationCoordinatorFactory;
	}

	@Override
	public void analyzeDocument(UUID taskID, IHTMLDocumentContainer htmlContainer,
			Consumer<ISchemaModifier> modifierCollector) {
		logger.traceEntry();

		// Resolve the schema reference
		final IXMLDocumentContainer xmlContainer = htmlContainer.getSource();
		final IXMLSchema schema = this.schemaManager.getSchema(
				xmlContainer.getStylesheeURI(),
				xmlContainer.getSchemaVersion());

		// Let the preprocessor re-order the events to a more usable form
		final ImmutableMultimap<ISchemaElementProxy, Expression> schemaTraceEvents = this.valueTracePreprocessor
			.prepareEvents(htmlContainer);

		if (schemaTraceEvents.isEmpty()) {
			logger.warn("No usable tracing information was found for this document");
		} else {
			// prepare an object to consolidate and forward all the modification requests
			final IModifierCreationCoordinator modifierCreationCoordinator = this.modifierCreationCoordinatorFactory
				.createCoordinator(schema, modifierCollector);

			// prepare a factory to create new evaluation tree items
			final IEvaluationTreeItemFactory factory = this.evaluationTreeItemFactoryFactory.createFactory(schema,
					modifierCreationCoordinator);

			// process the trace events grouped by schema request
			schemaTraceEvents.keySet().forEach(
					schemaObject -> processExpressions(
							schemaObject,
							schemaTraceEvents.get(schemaObject),
							factory));
			modifierCreationCoordinator.flush();
		}
		logger.traceExit();
	}

	/**
	 * Process the expressions associated to a schema element.
	 *
	 * @param contextItem
	 * @param expressions
	 * @param factory
	 */
	private void processExpressions(ISchemaElementProxy contextItem,
			ImmutableCollection<Expression> expressions, IEvaluationTreeItemFactory factory) {
		logger.traceEntry("for context item {}", contextItem);
		for (final Expression expression : expressions) {
			logger.debug("creating evaluation tree for expression {}", expression);
			final IEvaluationTreeItem topLevelItem = factory.createItem(expression);
			factory.initializeAllCreatedItems();
			logger.debug("processing evaluation tree for expression {}", expression);
			topLevelItem.evaluate(contextItem);
		}
		logger.traceExit();
	}

//
//		/**
//		 * @param schemaElement
//		 * @param axisExpression
//		 */
//		private ISchemaElementProxy processAxisExpression(ISchemaElementProxy schemaElement,
//				AxisExpression axisExpression) {
//			logger.traceEntry("with axis expression {}", axisExpression);
//			ISchemaElementProxy newSchemaElement = schemaElement;
//			switch (axisExpression.getAxis()) {
//			// TODO support axis ANCESTOR
//			// TODO support axis ANCESTOR_OR_SELF
//			// TODO support axis ATTRIBUTE
//			case AxisInfo.ATTRIBUTE:
//				newSchemaElement = processNodeTest(schemaElement, axisExpression.getNodeTest(), true);
//				break;
//			case AxisInfo.CHILD:
//				newSchemaElement = processNodeTest(schemaElement, axisExpression.getNodeTest(), false);
//				break;
//			// TODO support axis DESCENDANT
//			// TODO support axis DESCENDANT_OR_SELF
//			// TODO support axis FOLLOWING
//			// TODO support axis FOLLOWING_SIBLING
//			// TODO support axis NAMESPACE
//			// TODO support axis PARENT
//			// TODO support axis PRECEDING
//			// TODO support axis PRECEDING_SIBLING
//			// TODO support axis SELF
//			// TODO support axis PRECEDING_OR_ANCESTOR
//			default:
//				logger.warn("Unsupported axis {}: {}", AxisInfo.axisName[axisExpression.getAxis()], axisExpression);
//			}
//			return logger.traceExit(newSchemaElement);
//		}
//
//		/**
//		 * @param schemaElement
//		 * @param systemFunctionCall
//		 * @return
//		 */
//		private ISchemaElementProxy processSystemFunctionCall(ISchemaElementProxy schemaElement,
//				SystemFunctionCall systemFunctionCall) {
//			logger.traceEntry("with system function call {}", systemFunctionCall);
//			final ISchemaElementProxy newSchemaElement = schemaElement;
//			final StructuredQName functionName = systemFunctionCall.getFunctionName();
//			if (functionName.getNamespaceUri().equals(NamespaceUri.FN)) {
//				// system function - distinguish by name
//				switch (functionName.getLocalPart()) {
//				// TODO support system function boolean
//				// TODO support system function ceiling
//				case "concat":
//					// check arguments of the function
//					Arrays.stream(systemFunctionCall.getArguments())
//						.forEach(argument -> processExpression(schemaElement, argument));
//					break;
//				// TODO support system function contains
//				// TODO support system function count
//				// TODO support system function current
//				// TODO support system function document
//				// TODO support system function element-available
//				case "false":
//					// does not access any value and has no arguments - ignore
//					break;
//				case "exists":
//					// check arguments of the function
//					Arrays.stream(systemFunctionCall.getArguments())
//						.forEach(argument -> processExpression(schemaElement, argument));
//					break;
//				// TODO support system function floor
//				// TODO support system function format-number
//				// TODO support system function function-available
//				// TODO support system function generate-id
//				// TODO support system function id
//				// TODO support system function key
//				// TODO support system function lang
//				// TODO support system function last
//				case "last":
//					// does not access any value and has no arguments - ignore
//					break;
//				// TODO support system function local-name
//				// TODO support system function name
//				// TODO support system function namespace-uri
//				// TODO support system function normalize-space
//				// TODO support system function not
//				// TODO support system function number
//				case "position":
//					// does not access any value and has no arguments - ignore
//					break;
//				// TODO support system function round
//				// TODO support system function starts-with
//				// TODO support system function string
//				case "string-join":
//					// check arguments of the function
//					Arrays.stream(systemFunctionCall.getArguments())
//						.forEach(argument -> processExpression(schemaElement, argument));
//					break;
//
//				// TODO support system function string-length
//				// TODO support system function substring
//				// TODO support system function substring-after
//				// TODO support system function substring-before
//				// TODO support system function sum
//				// TODO support system function system-property
//				// TODO support system function translate
//				case "true":
//					// does not access any value and has no arguments - ignore
//					break;
//				// TODO support system function unparsed-entity-uri
//
//				default:
//					logger.warn("Unsupported system function {}: {}", functionName.getLocalPart(),
//							systemFunctionCall);
//				}
//			} else {
//				logger.warn("Unsupported system function namespace {}: {}", functionName.getNamespaceUri(),
//						systemFunctionCall);
//			}
//			return logger.traceExit(newSchemaElement);
//		}

}
