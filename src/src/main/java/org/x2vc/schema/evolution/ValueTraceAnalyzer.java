package org.x2vc.schema.evolution;

import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ITraceEvent;
import org.x2vc.processor.IValueAccessTraceEvent;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.IXMLSchemaObject;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IGenerationRule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.inject.Inject;

import net.sf.saxon.expr.*;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;

/**
 * Standard implementation of {@link IValueTraceAnalyzer}.
 */
public class ValueTraceAnalyzer implements IValueTraceAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	@Inject
	protected ValueTraceAnalyzer(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public void analyzeDocument(UUID taskID, IHTMLDocumentContainer htmlContainer,
			Consumer<ISchemaModifier> modifierCollector) {
		logger.traceEntry();

		// Filter the trace events down to the value access events we need.
		final List<IValueAccessTraceEvent> valueTraceEvents = getValueTraceEvents(htmlContainer);

		// The raw trace events point to the document elements - resolve these to the
		// schema element references the elements are based on.
		final IDocumentRequest request = htmlContainer.getSource().getRequest();
		final IXMLSchema schema = this.schemaManager.getSchema(request.getSchemaURI(), request.getSchemaVersion());
		final Multimap<IXMLSchemaObject, Expression> schemaTraceEvents = mapEventsToSchema(valueTraceEvents,
				htmlContainer.getSource(), schema);

		// process events grouped by schema element
		final ExpressionProcessor expressionProcessor = new ExpressionProcessor(request.getSchemaURI(),
				request.getSchemaVersion());
		schemaTraceEvents.keys().forEach(
				schemaObject -> expressionProcessor.processExpressions(
						schemaObject,
						schemaTraceEvents.get(schemaObject)));

		// transfer modifiers to collector
		expressionProcessor.attributeModifiers.values().forEach(modifierCollector::accept);
		expressionProcessor.elementModifiers.values().forEach(modifierCollector::accept);

		logger.traceExit();
	}

	/**
	 * Extract the trace events relevant to the schema analyzer.
	 *
	 * @param container
	 * @return
	 */
	protected List<IValueAccessTraceEvent> getValueTraceEvents(IHTMLDocumentContainer container) {
		logger.traceEntry();
		List<IValueAccessTraceEvent> valueTraceEvents;
		final Optional<ImmutableList<ITraceEvent>> oTraceEvents = container.getTraceEvents();
		if (oTraceEvents.isPresent()) {
			valueTraceEvents = oTraceEvents.get().stream()
				.filter(IValueAccessTraceEvent.class::isInstance)
				.map(IValueAccessTraceEvent.class::cast)
				.toList();
		} else {
			logger.warn("No trace events were recorded for the generated document");
			valueTraceEvents = Lists.newArrayList();
		}
		return logger.traceExit(valueTraceEvents);
	}

	/**
	 * Resolve the schema references and group the trace events by schema ID.
	 *
	 * @param events
	 * @param source
	 * @return
	 */
	private Multimap<IXMLSchemaObject, Expression> mapEventsToSchema(List<IValueAccessTraceEvent> events,
			IXMLDocumentContainer source, IXMLSchema schema) {
		logger.traceEntry();
		final Multimap<IXMLSchemaObject, Expression> result = MultimapBuilder.hashKeys().hashSetValues().build();

		final Map<UUID, UUID> traceIDToRuleIDMap = source.getDocumentDescriptor().getTraceIDToRuleIDMap();
		final IDocumentRequest request = source.getRequest();
		int discardedElements = 0;
		int eventIndex = -1;
		for (final IValueAccessTraceEvent event : events) {
			eventIndex++;
			final Optional<UUID> oElementID = event.getContextElementID();
			if (oElementID.isPresent()) {
				final UUID elementID = oElementID.get();
				if (traceIDToRuleIDMap.containsKey(elementID)) {
					final UUID ruleID = traceIDToRuleIDMap.get(elementID);
					try {
						final IGenerationRule rule = request.getRuleByID(ruleID);
						final Optional<UUID> oSchemaObjectID = rule.getSchemaObjectID();
						if (oSchemaObjectID.isPresent()) {
							final UUID schemaObjectID = oSchemaObjectID.get();
							final IXMLSchemaObject schemaObject = schema.getObjectByID(schemaObjectID);
							result.put(schemaObject, event.getExpression());
						} else {
							logger.debug("rule {} identified by event {} does not relate to a schema object", ruleID,
									eventIndex);
							discardedElements++;
						}
					} catch (final IllegalArgumentException e) {
						logger.debug("rule {} identified by event {} cannot found in document request", ruleID,
								eventIndex);
						discardedElements++;
					}
				} else {
					logger.debug("element ID {} of event {} cannot be resolved to a rule ID", elementID, eventIndex);
					discardedElements++;
				}
			} else {
				logger.debug("event {} does not have a context ID", eventIndex);
				discardedElements++;
			}
		}
		if (discardedElements > 0) {
			logger.debug("A total of {} incomplete trace events were ignored", discardedElements);
		}
		return logger.traceExit(result);
	}

	private record AttributeKey(UUID parentElement, String attributeName) {
	}

	private record ElementKey(UUID parentElement, String elementName) {
	}

	private class ExpressionProcessor {

		private URI schemaURI;
		private int schemaVersion;

		private Map<AttributeKey, IAddAttributeModifier> attributeModifiers = new HashMap<>();
		private Map<ElementKey, IAddElementModifier> elementModifiers = new HashMap<>();

		protected ExpressionProcessor(URI schemaURI, int schemaVersion) {
			super();
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
		}

		/**
		 * Process the expressions relating to s schema object.
		 *
		 * @param schemaObject
		 * @param expressions
		 */
		public void processExpressions(IXMLSchemaObject schemaObject, Collection<Expression> expressions) {
			logger.traceEntry();

			// The schema object has to resolve to an element type because that's the only thing we can extend
			// by adding new sub-elements or adding attributes.
			IXMLElementType elementType = null;
			if (schemaObject instanceof final IXMLElementType et) {
				elementType = et;
			} else if (schemaObject instanceof final IXMLElementReference elementReference) {
				elementType = elementReference.getElement();
			} else {
				logger.warn("Unable to process trace events relating to schema object {}", schemaObject);
				return;
			}

			for (final Expression expression : expressions) {
				processExpression(new SchemaElementProxy(elementType), expression);
			}

			logger.traceExit();
		}

		/**
		 * Processes a single traced expression relating to a schema object.
		 *
		 * @param schemaElement
		 * @param expression
		 */
		private ISchemaElementProxy processExpression(ISchemaElementProxy schemaElement, Expression expression) {
			logger.traceEntry("with expression {}", expression);
			ISchemaElementProxy newSchemaElement = schemaElement;

			// TODO support Expression subclass net.sf.saxon.expr.Expression (abstract)
			// TODO support Expression subclass ..net.sf.saxon.expr.Assignation (abstract)
			// TODO support Expression subclass ....net.sf.saxon.expr.ForExpression
			// TODO support Expression subclass ......net.sf.saxon.expr.flwor.OuterForExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.LetExpression
			// TODO support Expression subclass ......net.sf.saxon.expr.EagerLetExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.QuantifiedExpression
			if (expression instanceof final AttributeGetter attributeGetter) {
				// Expression subclass ..net.sf.saxon.expr.AttributeGetter
				processAttributeAccess(schemaElement, attributeGetter.getAttributeName().getStructuredQName());
			} else if (expression instanceof final AxisExpression axisExpression) {
				// Expression subclass ..net.sf.saxon.expr.AxisExpression
				newSchemaElement = processAxisExpression(schemaElement, axisExpression);

			}
			// TODO support Expression subclass ..net.sf.saxon.expr.BinaryExpression (abstract)
			// TODO support Expression subclass ....net.sf.saxon.expr.ArithmeticExpression
			// TODO support Expression subclass ......net.sf.saxon.expr.compat.ArithmeticExpression10
			// TODO support Expression subclass ....net.sf.saxon.expr.BooleanExpression (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.AndExpression
			// TODO support Expression subclass ......net.sf.saxon.expr.OrExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.FilterExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.GeneralComparison (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.GeneralComparison20
			// TODO support Expression subclass ....net.sf.saxon.expr.IdentityComparison
			// TODO support Expression subclass ....net.sf.saxon.expr.LookupExpression
			else if (expression instanceof final SlashExpression slashExpression) {
				// Expression subclass ....net.sf.saxon.expr.SlashExpression
				// Expression subclass ......net.sf.saxon.expr.SimpleStepExpression
				newSchemaElement = processExpression(schemaElement, slashExpression.getFirstStep());
				newSchemaElement = processExpression(newSchemaElement, slashExpression.getRemainingSteps());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.SwitchCaseComparison
			else if (expression instanceof final ValueComparison valueComparison) {
				// Expression subclass ....net.sf.saxon.expr.ValueComparison
				// The comparison itself does not constitute a value access, but check the contained expressions.
				newSchemaElement = processExpression(schemaElement, valueComparison.getLhsExpression());
				newSchemaElement = processExpression(newSchemaElement, valueComparison.getRhsExpression());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.VennExpression
			// TODO support Expression subclass ......net.sf.saxon.expr.SingletonIntersectExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.compat.GeneralComparison10
			else if (expression instanceof ContextItemExpression) {
				// Expression subclass ..net.sf.saxon.expr.ContextItemExpression
				// Expression subclass ....net.sf.saxon.expr.CurrentItemExpression
				// Although technically a value access, we can't learn anything new from a "this" (.) access...
			}
			// TODO support Expression subclass ..net.sf.saxon.expr.DynamicFunctionCall
			// TODO support Expression subclass ..net.sf.saxon.expr.ErrorExpression
			// TODO support Expression subclass ..net.sf.saxon.expr.FunctionCall (abstract)
			// TODO support Expression subclass ....net.sf.saxon.expr.StaticFunctionCall
			else if (expression instanceof final SystemFunctionCall systemFunctionCall) {
				newSchemaElement = processSystemFunctionCall(schemaElement, systemFunctionCall);
				// Expression subclass ......net.sf.saxon.expr.SystemFunctionCall
				// Expression subclass ........net.sf.saxon.expr.SystemFunctionCall.Optimized (abstract)
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.UserFunctionCall
			// TODO support Expression subclass ....net.sf.saxon.functions.IntegratedFunctionCall
			// TODO support Expression subclass ....net.sf.saxon.xpath.XPathFunctionCall
			// TODO support Expression subclass ..net.sf.saxon.expr.IntegerRangeTest
			// TODO support Expression subclass ..net.sf.saxon.expr.IsLastExpression
			else if (expression instanceof Literal) {
				// Expression subclass ..net.sf.saxon.expr.Literal
				// Expression subclass ....net.sf.saxon.expr.StringLiteral
				// Expression subclass ....net.sf.saxon.functions.hof.FunctionLiteral
				// no value access to be extracted here
			}
			// TODO support Expression subclass ..net.sf.saxon.expr.NumberSequenceFormatter
			// TODO support Expression subclass ..net.sf.saxon.expr.PseudoExpression (abstract)
			// TODO support Expression subclass ....net.sf.saxon.expr.DefaultedArgumentExpression
			// TODO support Expression subclass
			// ......net.sf.saxon.expr.DefaultedArgumentExpression.DefaultCollationArgument
			// TODO support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinition
			// TODO support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinitionList
			// TODO support Expression subclass ....net.sf.saxon.pattern.Pattern (abstract)
			// TODO support Expression subclass ......net.sf.saxon.pattern.AncestorQualifiedPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.AnchorPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.BasePatternWithPredicate
			// TODO support Expression subclass ......net.sf.saxon.pattern.BooleanExpressionPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.GeneralNodePattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.GeneralPositionalPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.ItemTypePattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.NodeSetPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.NodeTestPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.PatternThatSetsCurrent
			// TODO support Expression subclass ......net.sf.saxon.pattern.SimplePositionalPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.StreamingFunctionArgumentPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.UniversalPattern
			// TODO support Expression subclass ......net.sf.saxon.pattern.VennPattern (abstract)
			// TODO support Expression subclass ........net.sf.saxon.pattern.ExceptPattern
			// TODO support Expression subclass ........net.sf.saxon.pattern.IntersectPattern
			// TODO support Expression subclass ........net.sf.saxon.pattern.UnionPattern
			// TODO support Expression subclass ..net.sf.saxon.expr.RangeExpression
			// TODO support Expression subclass ..net.sf.saxon.expr.RootExpression
			// TODO support Expression subclass ..net.sf.saxon.expr.SimpleExpression (abstract)
			// TODO support Expression subclass ..net.sf.saxon.expr.SuppliedParameterReference
			// TODO support Expression subclass ..net.sf.saxon.expr.TryCatch
			// TODO support Expression subclass ..net.sf.saxon.expr.UnaryExpression (abstract)
			else if (expression instanceof final AdjacentTextNodeMerger adjacentTextNodeMerger) {
				// Expression subclass ....net.sf.saxon.expr.AdjacentTextNodeMerger
				// check all sub-expressions
				newSchemaElement = processExpression(schemaElement, adjacentTextNodeMerger.getBaseExpression());
			} else if (expression instanceof final AtomicSequenceConverter atomicSequenceConverter) {
				// Expression subclass ....net.sf.saxon.expr.AtomicSequenceConverter
				// The conversion itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, atomicSequenceConverter.getBaseExpression());
			}
			// TODO support Expression subclass ......net.sf.saxon.expr.UntypedSequenceConverter
			else if (expression instanceof final Atomizer atomizer) {
				// Expression subclass ....net.sf.saxon.expr.Atomizer
				// The atomizer itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, atomizer.getBaseExpression());
			} else if (expression instanceof final CardinalityChecker cardinalityChecker) {
				// Expression subclass ....net.sf.saxon.expr.CardinalityChecker
				// The check itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, cardinalityChecker.getBaseExpression());
			} else if (expression instanceof final CastingExpression castingExpression) {
				// Expression subclass ....net.sf.saxon.expr.CastingExpression (abstract)
				// Expression subclass ......net.sf.saxon.expr.CastExpression
				// Expression subclass ......net.sf.saxon.expr.CastableExpression
				// The cast itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, castingExpression.getBaseExpression());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.CompareToConstant (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.CompareToIntegerConstant
			// TODO support Expression subclass ......net.sf.saxon.expr.CompareToStringConstant
			// TODO support Expression subclass ....net.sf.saxon.expr.ConsumingOperand
			// TODO support Expression subclass ....net.sf.saxon.expr.EmptyTextNodeRemover
			// TODO support Expression subclass ....net.sf.saxon.expr.HomogeneityChecker
			// TODO support Expression subclass ....net.sf.saxon.expr.InstanceOfExpression
			else if (expression instanceof final ItemChecker itemChecker) {
				// Expression subclass ....net.sf.saxon.expr.ItemChecker
				// The check itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, itemChecker.getBaseExpression());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.LookupAllExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.NegateExpression
			else if (expression instanceof final SingleItemFilter singleItemFilter) {
				// Expression subclass ....net.sf.saxon.expr.SingleItemFilter (abstract)
				// Expression subclass ......net.sf.saxon.expr.FirstItemExpression
				// Expression subclass ......net.sf.saxon.expr.LastItemExpression
				// Expression subclass ......net.sf.saxon.expr.SubscriptExpression
				// The item selection itself does not constitute a value access, but check the contained expression.
				newSchemaElement = processExpression(schemaElement, singleItemFilter.getBaseExpression());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.SingletonAtomizer
			// TODO support Expression subclass ....net.sf.saxon.expr.TailCallLoop
			// TODO support Expression subclass ....net.sf.saxon.expr.TailExpression
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.OnEmptyExpr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.OnNonEmptyExpr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SequenceInstr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.WherePopulated
			// TODO support Expression subclass ....net.sf.saxon.expr.sort.DocumentSorter
			// TODO support Expression subclass ....net.sf.saxon.functions.hof.FunctionSequenceCoercer
			else if (expression instanceof VariableReference) {
				// Expression subclass ..net.sf.saxon.expr.VariableReference (abstract)
				// Expression subclass ....net.sf.saxon.expr.GlobalVariableReference
				// Expression subclass ....net.sf.saxon.expr.LocalVariableReference
				// Variable references do not constitute a context access and do not have any sub-expressions to check
			}
			// TODO support Expression subclass ..net.sf.saxon.expr.flwor.FLWORExpression
			// TODO support Expression subclass ..net.sf.saxon.expr.flwor.TupleExpression
			// TODO support Expression subclass ..net.sf.saxon.expr.instruct.EvaluateInstr
			// TODO support Expression subclass ..net.sf.saxon.expr.instruct.Instruction (abstract)
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.AnalyzeString
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ApplyNextMatchingTemplate (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ApplyImports
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.NextMatch
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ApplyTemplates
			else if (expression instanceof final Block block) {
				// Expression subclass ....net.sf.saxon.expr.instruct.Block
				// check the sub-expressions of the block
				Arrays.stream(block.getOperanda())
					.forEach(op -> processExpression(schemaElement, op.getChildExpression()));
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.BreakInstr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.CallTemplate
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Choose
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ComponentTracer
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ConditionalBlock
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.CopyOf
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Doctype
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ForEach
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ForEachGroup
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Fork
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.IterateInstr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.LocalParam
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.LocalParamBlock
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.MessageInstr
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.NextIteration
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ParentNodeConstructor (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.DocumentInstr
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ElementCreator (abstract)
			// TODO support Expression subclass ........net.sf.saxon.expr.instruct.ComputedElement
			// TODO support Expression subclass ........net.sf.saxon.expr.instruct.Copy
			// TODO support Expression subclass ........net.sf.saxon.expr.instruct.FixedElement
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ResultDocument
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SimpleNodeConstructor (abstract)
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.AttributeCreator (abstract)
			// TODO support Expression subclass ........net.sf.saxon.expr.instruct.ComputedAttribute
			// TODO support Expression subclass ........net.sf.saxon.expr.instruct.FixedAttribute
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.Comment
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.NamespaceConstructor
			// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ProcessingInstruction
			else if (expression instanceof final ValueOf valueOf) {
				// Expression subclass ......net.sf.saxon.expr.instruct.ValueOf
				// Check the select expression
				newSchemaElement = processExpression(schemaElement, valueOf.getSelect());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SourceDocument
			else if (expression instanceof final TraceExpression traceExpression) {
				// Expression subclass ....net.sf.saxon.expr.instruct.TraceExpression
				// We don't care for the trace expression, but have to examine its sub-expression
				newSchemaElement = processExpression(schemaElement, traceExpression.getBody());
			}
			// TODO support Expression subclass ....net.sf.saxon.expr.instruct.UseAttributeSet
			// TODO support Expression subclass ....net.sf.saxon.expr.sort.MergeInstr
			// TODO support Expression subclass ..net.sf.saxon.expr.instruct.NumberInstruction
			// TODO support Expression subclass ..net.sf.saxon.expr.sort.ConditionalSorter
			// TODO support Expression subclass ..net.sf.saxon.expr.sort.SortExpression
			// TODO support Expression subclass ..net.sf.saxon.functions.CurrentGroupCall
			// TODO support Expression subclass ..net.sf.saxon.functions.CurrentGroupingKeyCall
			// TODO support Expression subclass ..net.sf.saxon.functions.hof.PartialApply
			// TODO support Expression subclass ..net.sf.saxon.functions.hof.UserFunctionReference
			// TODO support Expression subclass ..net.sf.saxon.ma.arrays.SquareArrayConstructor
			// TODO support Expression subclass ..net.sf.saxon.xpath.JAXPVariableReference
			else {
				logger.warn("Unsupported expression type {}: {}", expression.getClass().getSimpleName(), expression);
			}

			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param axisExpression
		 */
		private ISchemaElementProxy processAxisExpression(ISchemaElementProxy schemaElement,
				AxisExpression axisExpression) {
			logger.traceEntry("with axis expression {}", axisExpression);
			ISchemaElementProxy newSchemaElement = schemaElement;
			switch (axisExpression.getAxis()) {
			// TODO support axis ANCESTOR
			// TODO support axis ANCESTOR_OR_SELF
			// TODO support axis ATTRIBUTE
			case AxisInfo.ATTRIBUTE:
				newSchemaElement = processNodeTest(schemaElement, axisExpression.getNodeTest(), true);
				break;
			case AxisInfo.CHILD:
				newSchemaElement = processNodeTest(schemaElement, axisExpression.getNodeTest(), false);
				break;
			// TODO support axis DESCENDANT
			// TODO support axis DESCENDANT_OR_SELF
			// TODO support axis FOLLOWING
			// TODO support axis FOLLOWING_SIBLING
			// TODO support axis NAMESPACE
			// TODO support axis PARENT
			// TODO support axis PRECEDING
			// TODO support axis PRECEDING_SIBLING
			// TODO support axis SELF
			// TODO support axis PRECEDING_OR_ANCESTOR
			default:
				logger.warn("Unsupported axis {}: {}", AxisInfo.axisName[axisExpression.getAxis()], axisExpression);
			}
			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param systemFunctionCall
		 * @return
		 */
		private ISchemaElementProxy processSystemFunctionCall(ISchemaElementProxy schemaElement,
				SystemFunctionCall systemFunctionCall) {
			logger.traceEntry("with system function call {}", systemFunctionCall);
			final ISchemaElementProxy newSchemaElement = schemaElement;
			final StructuredQName functionName = systemFunctionCall.getFunctionName();
			if (functionName.getNamespaceUri().equals(NamespaceUri.FN)) {
				// system function - distinguish by name
				switch (functionName.getLocalPart()) {
				// TODO support system function boolean
				// TODO support system function ceiling
				case "concat":
					// check arguments of the function
					Arrays.stream(systemFunctionCall.getArguments())
						.forEach(argument -> processExpression(schemaElement, argument));
					break;
				// TODO support system function contains
				// TODO support system function count
				// TODO support system function current
				// TODO support system function document
				// TODO support system function element-available
				case "false":
					// does not access any value and has no arguments - ignore
					break;
				case "exists":
					// check arguments of the function
					Arrays.stream(systemFunctionCall.getArguments())
						.forEach(argument -> processExpression(schemaElement, argument));
					break;
				// TODO support system function floor
				// TODO support system function format-number
				// TODO support system function function-available
				// TODO support system function generate-id
				// TODO support system function id
				// TODO support system function key
				// TODO support system function lang
				// TODO support system function last
				case "last":
					// does not access any value and has no arguments - ignore
					break;
				// TODO support system function local-name
				// TODO support system function name
				// TODO support system function namespace-uri
				// TODO support system function normalize-space
				// TODO support system function not
				// TODO support system function number
				case "position":
					// does not access any value and has no arguments - ignore
					break;
				// TODO support system function round
				// TODO support system function starts-with
				// TODO support system function string
				case "string-join":
					// check arguments of the function
					Arrays.stream(systemFunctionCall.getArguments())
						.forEach(argument -> processExpression(schemaElement, argument));
					break;

				// TODO support system function string-length
				// TODO support system function substring
				// TODO support system function substring-after
				// TODO support system function substring-before
				// TODO support system function sum
				// TODO support system function system-property
				// TODO support system function translate
				case "true":
					// does not access any value and has no arguments - ignore
					break;
				// TODO support system function unparsed-entity-uri

				default:
					logger.warn("Unsupported system function {}: {}", functionName.getLocalPart(),
							systemFunctionCall);
				}
			} else {
				logger.warn("Unsupported system function namespace {}: {}", functionName.getNamespaceUri(),
						systemFunctionCall);
			}
			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param nodeTest
		 */
		private ISchemaElementProxy processNodeTest(ISchemaElementProxy schemaElement, NodeTest nodeTest,
				boolean isAttribute) {
			logger.traceEntry("with node test {}", nodeTest);
			ISchemaElementProxy newSchemaElement = schemaElement;
			// TODO support NodeTest subclass AnyNodeTest
			if (nodeTest instanceof final CombinedNodeTest combinedNodeTest) {
				// NodeTest subclass CombinedNodeTest
				Arrays.stream(combinedNodeTest.getComponentNodeTests())
					.forEach(subTest -> processNodeTest(schemaElement, subTest, isAttribute));
			}
			// TODO support NodeTest subclass DocumentNodeTest
			// TODO support NodeTest subclass ErrorType
			// TODO support NodeTest subclass LocalNameTest
			// TODO support NodeTest subclass MultipleNodeKindTest
			// TODO support NodeTest subclass NamespaceTest
			else if (nodeTest instanceof final NameTest nameTest) {
				// NodeTest subclass NameTest
				if (isAttribute) {
					newSchemaElement = processAttributeAccess(schemaElement, nameTest.getMatchingNodeName());
				} else {
					newSchemaElement = processElementAccess(schemaElement, nameTest.getMatchingNodeName());
				}
			} else if (nodeTest instanceof NodeKindTest) {
				// NodeTest subclass NodeKindTest
				// ignore this test for now
			}
			// TODO support NodeTest subclass NodeSelector
			// TODO support NodeTest subclass SameNameTest
			else {
				logger.warn("Unsupported node test {}: {}", nodeTest.getClass().getSimpleName(), nodeTest);
			}
			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param subElementName
		 */
		private ISchemaElementProxy processElementAccess(ISchemaElementProxy schemaElement,
				StructuredQName subElementName) {
			logger.traceEntry();
			ISchemaElementProxy newSchemaElement = schemaElement;

			final NamespaceUri namespaceURI = subElementName.getNamespaceUri();
			final String localName = subElementName.getLocalPart();
			if (namespaceURI.equals(NamespaceUri.NULL)) {
				// check whether a sub-element with that name already exists
				final Optional<ISchemaElementProxy> oSubElement = schemaElement.getSubElement(localName);
				if (oSubElement.isPresent()) {
					// element already exists, nothing else to do
					newSchemaElement = oSubElement.get();
				} else {
					final Optional<IAddElementModifier> oParentElementModifier = schemaElement.getModifier();
					if (oParentElementModifier.isPresent()) {
						// parent schema element is newly created - add the sub-element below it
						logger.debug("First attempt to access non-existing sub-element {} of element type {}",
								localName,
								schemaElement.getID());
						final IAddElementModifier newModifier = createElementModifier(schemaElement, localName);
						oParentElementModifier.get().addSubElement(newModifier);
						newSchemaElement = new SchemaElementProxy(newModifier);
					} else {
						// parent element already exists - we need to check the global map in this case
						final ElementKey elementKey = new ElementKey(schemaElement.getID(), localName);
						if (!this.elementModifiers.containsKey(elementKey)) {
							logger.debug("First attempt to access non-existing sub-element {} of element type {}",
									localName,
									schemaElement.getID());
							final IAddElementModifier newModifier = createElementModifier(schemaElement, localName);
							this.elementModifiers.put(elementKey, newModifier);
							newSchemaElement = new SchemaElementProxy(newModifier);
						}
					}
				}
			} else {
				logger.warn("Unable to process references with namespace: {}", subElementName);
			}
			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param elementName
		 * @return
		 */
		protected IAddElementModifier createElementModifier(ISchemaElementProxy schemaElement,
				final String elementName) {
			return AddElementModifier
				.builder(this.schemaURI, this.schemaVersion)
				.withElementID(schemaElement.getID())
				.withName(elementName)
				.withDependency(schemaElement.getModifier())
				.build();
		}

		/**
		 * @param schemaElement
		 * @param attributeName
		 */
		private ISchemaElementProxy processAttributeAccess(ISchemaElementProxy schemaElement,
				StructuredQName attributeName) {
			logger.traceEntry();
			final ISchemaElementProxy newSchemaElement = schemaElement;

			final NamespaceUri namespaceURI = attributeName.getNamespaceUri();
			final String localName = attributeName.getLocalPart();
			if (namespaceURI.equals(NamespaceUri.NULL)) {
				// check whether an attribute with that name already exists
				if (!schemaElement.hasAttribute(localName)) {
					final Optional<IAddElementModifier> oElementModifier = schemaElement.getModifier();
					if (oElementModifier.isPresent()) {
						// parent element is newly created - add the attribute modifier below it
						logger.debug("First attempt to access non-existing attribute {} of element type {}", localName,
								schemaElement.getID());
						final IAddAttributeModifier modifier = createAttributeModifier(schemaElement, localName);
						oElementModifier.get().addAttribute(modifier);
					} else {
						// parent element already exists - we need to check the global map in this case
						final AttributeKey attributeKey = new AttributeKey(schemaElement.getID(), localName);
						if (!this.attributeModifiers.containsKey(attributeKey)) {
							logger.debug("First attempt to access non-existing attribute {} of element type {}",
									localName,
									schemaElement.getID());
							final IAddAttributeModifier modifier = createAttributeModifier(schemaElement, localName);
							this.attributeModifiers.put(attributeKey, modifier);
						}
					}
				}
			} else {
				logger.warn("Unable to process attributes with namespace: {}", attributeName);
			}
			return logger.traceExit(newSchemaElement);
		}

		/**
		 * @param schemaElement
		 * @param attributeName
		 * @return
		 */
		protected IAddAttributeModifier createAttributeModifier(ISchemaElementProxy schemaElement,
				final String attributeName) {
			return AddAttributeModifier
				.builder(this.schemaURI, this.schemaVersion)
				.withElementID(schemaElement.getID())
				.withName(attributeName)
				.withDependency(schemaElement.getModifier())
				.build();
		}
	}

}
