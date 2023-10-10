package org.x2vc.schema.evolution.items;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Deque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import net.sf.saxon.expr.*;
import net.sf.saxon.expr.compat.GeneralComparison10;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.pattern.*;

/**
 * Standard implementation of {@link IEvaluationTreeItemFactory}.
 */
public class EvaluationTreeItemFactory implements IEvaluationTreeItemFactory {

	private static final Logger logger = LogManager.getLogger();
	private final IXMLSchema schema;
	private final IModifierCreationCoordinator coordinator;
	private final Deque<IEvaluationTreeItem> uninitializedItems;

	@Inject
	protected EvaluationTreeItemFactory(@Assisted IXMLSchema schema,
			@Assisted IModifierCreationCoordinator coordinator) {
		super();
		checkNotNull(schema);
		this.schema = schema;
		this.coordinator = coordinator;
		this.uninitializedItems = Lists.newLinkedList();
	}

	@Override
	public IEvaluationTreeItem createItemForExpression(Expression expression) {
		checkNotNull(expression);
		logger.traceEntry("for {} {}", expression.getClass().getSimpleName(), expression);
		IEvaluationTreeItem newItem = null;
		// ===== Expression subclass net.sf.saxon.expr.Expression (abstract) =====
//		// TODO support Expression subclass ..net.sf.saxon.expr.Assignation (abstract)
//		// TODO support Expression subclass ....net.sf.saxon.expr.ForExpression
//		// TODO support Expression subclass ......net.sf.saxon.expr.flwor.OuterForExpression
//		// TODO support Expression subclass ....net.sf.saxon.expr.LetExpression
//		// TODO support Expression subclass ......net.sf.saxon.expr.EagerLetExpression
//		// TODO support Expression subclass ....net.sf.saxon.expr.QuantifiedExpression
		if (expression instanceof final AttributeGetter attributeGetter) {
			// Expression subclass ..net.sf.saxon.expr.AttributeGetter
			newItem = new AttributeGetterItem(this.schema, this.coordinator, attributeGetter);
		} else if (expression instanceof final AxisExpression axisExpression) {
			// Expression subclass ..net.sf.saxon.expr.AxisExpression
			newItem = new AxisExpressionItem(this.schema, this.coordinator, axisExpression);
		}
		// ----- Expression subclass ..net.sf.saxon.expr.BinaryExpression (abstract) -----
		else if (expression instanceof final ArithmeticExpression arithmeticExpression) {
			// Expression subclass ....net.sf.saxon.expr.ArithmeticExpression
			// Expression subclass ......net.sf.saxon.expr.compat.ArithmeticExpression10
			newItem = new IndependentBinaryExpressionItem<ArithmeticExpression>(this.schema, this.coordinator,
					arithmeticExpression);
		} else if (expression instanceof final BooleanExpression booleanExpression) {
			// Expression subclass ....net.sf.saxon.expr.BooleanExpression (abstract)
			// Expression subclass ......net.sf.saxon.expr.AndExpression
			// Expression subclass ......net.sf.saxon.expr.OrExpression
			newItem = new IndependentBinaryExpressionItem<BooleanExpression>(this.schema, this.coordinator,
					booleanExpression);
		} else if (expression instanceof final FilterExpression filterExpression) {
			// Expression subclass ....net.sf.saxon.expr.FilterExpression
			newItem = new FilterExpressionItem(this.schema, this.coordinator, filterExpression);
		} else if (expression instanceof final GeneralComparison generalComparison) {
			// Expression subclass ....net.sf.saxon.expr.GeneralComparison (abstract)
			// Expression subclass ......net.sf.saxon.expr.GeneralComparison20
			newItem = new IndependentBinaryExpressionItem<GeneralComparison>(this.schema, this.coordinator,
					generalComparison);
//		// TODO support Expression subclass ....net.sf.saxon.expr.IdentityComparison
//		// TODO support Expression subclass ....net.sf.saxon.expr.LookupExpression
		} else if (expression instanceof final SlashExpression slashExpression) {
			// Expression subclass ....net.sf.saxon.expr.SlashExpression
			// Expression subclass ......net.sf.saxon.expr.SimpleStepExpression
			newItem = new SlashExpressionItem(this.schema, this.coordinator, slashExpression);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.SwitchCaseComparison
		else if (expression instanceof final ValueComparison valueComparison) {
			// Expression subclass ....net.sf.saxon.expr.ValueComparison
			newItem = new IndependentBinaryExpressionItem<ValueComparison>(this.schema, this.coordinator,
					valueComparison);
		} else if (expression instanceof final VennExpression vennExpression) {
			// Expression subclass ....net.sf.saxon.expr.VennExpression
			// Expression subclass ......net.sf.saxon.expr.SingletonIntersectExpression
			newItem = new IndependentBinaryExpressionItem<VennExpression>(this.schema, this.coordinator,
					vennExpression);
		} else if (expression instanceof final GeneralComparison10 generalComparison10) {
			// Expression subclass ....net.sf.saxon.expr.compat.GeneralComparison10
			newItem = new IndependentBinaryExpressionItem<GeneralComparison10>(this.schema, this.coordinator,
					generalComparison10);
		} else if (expression instanceof final ContextItemExpression contextItemExpression) {
//			// Expression subclass ..net.sf.saxon.expr.ContextItemExpression
//			// Expression subclass ....net.sf.saxon.expr.CurrentItemExpression
//			// Although technically a value access, we can't learn anything new from a "this" (.) access...
			newItem = new NoOperationItem<ContextItemExpression>(this.schema, this.coordinator, contextItemExpression);
		}
//		// TODO support Expression subclass ..net.sf.saxon.expr.DynamicFunctionCall
//		// TODO support Expression subclass ..net.sf.saxon.expr.ErrorExpression
//		// TODO support Expression subclass ..net.sf.saxon.expr.FunctionCall (abstract)
//		// TODO support Expression subclass ....net.sf.saxon.expr.StaticFunctionCall
		else if (expression instanceof final SystemFunctionCall systemFunctionCall) {
			// Expression subclass ......net.sf.saxon.expr.SystemFunctionCall
			// Expression subclass ........net.sf.saxon.expr.SystemFunctionCall.Optimized (abstract)
			newItem = new SystemFunctionCallItem(this.schema, this.coordinator, systemFunctionCall);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.UserFunctionCall
//		// TODO support Expression subclass ....net.sf.saxon.functions.IntegratedFunctionCall
//		// TODO support Expression subclass ....net.sf.saxon.xpath.XPathFunctionCall
//		// TODO support Expression subclass ..net.sf.saxon.expr.IntegerRangeTest
//		// TODO support Expression subclass ..net.sf.saxon.expr.IsLastExpression
		else if (expression instanceof final Literal literal) {
			// Expression subclass ..net.sf.saxon.expr.Literal
			// Expression subclass ....net.sf.saxon.expr.StringLiteral
			// Expression subclass ....net.sf.saxon.functions.hof.FunctionLiteral
			// no value access to be extracted here
			newItem = new NoOperationItem<Literal>(this.schema, this.coordinator, literal);
		}
//		// TODO support Expression subclass ..net.sf.saxon.expr.NumberSequenceFormatter
//		// TODO support Expression subclass ..net.sf.saxon.expr.PseudoExpression (abstract)
//		// TODO support Expression subclass ....net.sf.saxon.expr.DefaultedArgumentExpression
//		// TODO support Expression subclass
//		// ......net.sf.saxon.expr.DefaultedArgumentExpression.DefaultCollationArgument
//		// TODO support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinition
//		// TODO support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinitionList
//		// TODO support Expression subclass ....net.sf.saxon.pattern.Pattern (abstract)
//		// TODO support Expression subclass ......net.sf.saxon.pattern.AncestorQualifiedPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.AnchorPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.BasePatternWithPredicate
//		// TODO support Expression subclass ......net.sf.saxon.pattern.BooleanExpressionPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.GeneralNodePattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.GeneralPositionalPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.ItemTypePattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.NodeSetPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.NodeTestPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.PatternThatSetsCurrent
//		// TODO support Expression subclass ......net.sf.saxon.pattern.SimplePositionalPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.StreamingFunctionArgumentPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.UniversalPattern
//		// TODO support Expression subclass ......net.sf.saxon.pattern.VennPattern (abstract)
//		// TODO support Expression subclass ........net.sf.saxon.pattern.ExceptPattern
//		// TODO support Expression subclass ........net.sf.saxon.pattern.IntersectPattern
//		// TODO support Expression subclass ........net.sf.saxon.pattern.UnionPattern
//		// TODO support Expression subclass ..net.sf.saxon.expr.RangeExpression
//		// TODO support Expression subclass ..net.sf.saxon.expr.RootExpression
//		// TODO support Expression subclass ..net.sf.saxon.expr.SimpleExpression (abstract)
//		// TODO support Expression subclass ..net.sf.saxon.expr.SuppliedParameterReference
//		// TODO support Expression subclass ..net.sf.saxon.expr.TryCatch
//		// TODO support Expression subclass ..net.sf.saxon.expr.UnaryExpression (abstract)
		else if (expression instanceof final AdjacentTextNodeMerger adjacentTextNodeMerger) {
			// Expression subclass ....net.sf.saxon.expr.AdjacentTextNodeMerger
			// check all sub-expressions
			newItem = new UnaryExpressionItem<AdjacentTextNodeMerger>(this.schema, this.coordinator,
					adjacentTextNodeMerger);
		} else if (expression instanceof final AtomicSequenceConverter atomicSequenceConverter) {
			// Expression subclass ....net.sf.saxon.expr.AtomicSequenceConverter
			// The conversion itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<AtomicSequenceConverter>(this.schema, this.coordinator,
					atomicSequenceConverter);
		} else if (expression instanceof final UntypedSequenceConverter untypedSequenceConverter) {
			// Expression subclass ......net.sf.saxon.expr.UntypedSequenceConverter
			// The conversion itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<AtomicSequenceConverter>(this.schema, this.coordinator,
					untypedSequenceConverter);
		} else if (expression instanceof final Atomizer atomizer) {
			// Expression subclass ....net.sf.saxon.expr.Atomizer
			// The atomizer itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<Atomizer>(this.schema, this.coordinator, atomizer);
		} else if (expression instanceof final CardinalityChecker cardinalityChecker) {
			// Expression subclass ....net.sf.saxon.expr.CardinalityChecker
			// The check itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<CardinalityChecker>(this.schema, this.coordinator, cardinalityChecker);
		} else if (expression instanceof final CastingExpression castingExpression) {
			// Expression subclass ....net.sf.saxon.expr.CastingExpression (abstract)
			// Expression subclass ......net.sf.saxon.expr.CastExpression
			// Expression subclass ......net.sf.saxon.expr.CastableExpression
			// The cast itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<CastingExpression>(this.schema, this.coordinator, castingExpression);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.CompareToConstant (abstract)
//		// TODO support Expression subclass ......net.sf.saxon.expr.CompareToIntegerConstant
//		// TODO support Expression subclass ......net.sf.saxon.expr.CompareToStringConstant
//		// TODO support Expression subclass ....net.sf.saxon.expr.ConsumingOperand
//		// TODO support Expression subclass ....net.sf.saxon.expr.EmptyTextNodeRemover
//		// TODO support Expression subclass ....net.sf.saxon.expr.HomogeneityChecker
//		// TODO support Expression subclass ....net.sf.saxon.expr.InstanceOfExpression
		else if (expression instanceof final ItemChecker itemChecker) {
			// Expression subclass ....net.sf.saxon.expr.ItemChecker
			// The check itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<ItemChecker>(this.schema, this.coordinator, itemChecker);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.LookupAllExpression
//		// TODO support Expression subclass ....net.sf.saxon.expr.NegateExpression
		else if (expression instanceof final SingleItemFilter singleItemFilter) {
			// Expression subclass ....net.sf.saxon.expr.SingleItemFilter (abstract)
			// Expression subclass ......net.sf.saxon.expr.FirstItemExpression
			// Expression subclass ......net.sf.saxon.expr.LastItemExpression
			// Expression subclass ......net.sf.saxon.expr.SubscriptExpression
			// The item selection itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<SingleItemFilter>(this.schema, this.coordinator, singleItemFilter);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.SingletonAtomizer
//		// TODO support Expression subclass ....net.sf.saxon.expr.TailCallLoop
//		// TODO support Expression subclass ....net.sf.saxon.expr.TailExpression
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.OnEmptyExpr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.OnNonEmptyExpr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SequenceInstr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.WherePopulated
//		// TODO support Expression subclass ....net.sf.saxon.expr.sort.DocumentSorter
//		// TODO support Expression subclass ....net.sf.saxon.functions.hof.FunctionSequenceCoercer
		else if (expression instanceof final VariableReference variableReference) {
			// Expression subclass ..net.sf.saxon.expr.VariableReference (abstract)
			// Expression subclass ....net.sf.saxon.expr.GlobalVariableReference
			// Expression subclass ....net.sf.saxon.expr.LocalVariableReference
			// Variable references do not constitute a context access and do not have any sub-expressions to check
			newItem = new NoOperationItem<VariableReference>(this.schema, this.coordinator, variableReference);
		}
//		// TODO support Expression subclass ..net.sf.saxon.expr.flwor.FLWORExpression
//		// TODO support Expression subclass ..net.sf.saxon.expr.flwor.TupleExpression
//		// TODO support Expression subclass ..net.sf.saxon.expr.instruct.EvaluateInstr
//		// TODO support Expression subclass ..net.sf.saxon.expr.instruct.Instruction (abstract)
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.AnalyzeString
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ApplyNextMatchingTemplate (abstract)
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ApplyImports
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.NextMatch
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ApplyTemplates
		else if (expression instanceof final Block block) {
			// Expression subclass ....net.sf.saxon.expr.instruct.Block
			newItem = new BlockItem(this.schema, this.coordinator, block);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.BreakInstr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.CallTemplate
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Choose
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ComponentTracer
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ConditionalBlock
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.CopyOf
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Doctype
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ForEach
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ForEachGroup
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.Fork
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.IterateInstr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.LocalParam
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.LocalParamBlock
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.MessageInstr
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.NextIteration
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ParentNodeConstructor (abstract)
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.DocumentInstr
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ElementCreator (abstract)
//		// TODO support Expression subclass ........net.sf.saxon.expr.instruct.ComputedElement
//		// TODO support Expression subclass ........net.sf.saxon.expr.instruct.Copy
//		// TODO support Expression subclass ........net.sf.saxon.expr.instruct.FixedElement
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.ResultDocument
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SimpleNodeConstructor (abstract)
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.AttributeCreator (abstract)
//		// TODO support Expression subclass ........net.sf.saxon.expr.instruct.ComputedAttribute
//		// TODO support Expression subclass ........net.sf.saxon.expr.instruct.FixedAttribute
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.Comment
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.NamespaceConstructor
//		// TODO support Expression subclass ......net.sf.saxon.expr.instruct.ProcessingInstruction
		else if (expression instanceof final ValueOf valueOf) {
			// Expression subclass ......net.sf.saxon.expr.instruct.ValueOf
			newItem = new ValueOfItem(this.schema, this.coordinator, valueOf);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.SourceDocument
		else if (expression instanceof final TraceExpression traceExpression) {
			// Expression subclass ....net.sf.saxon.expr.instruct.TraceExpression
			// We don't care for the trace expression, but have to examine its sub-expression
			newItem = new TraceExpressionItem(this.schema, this.coordinator, traceExpression);
		}
//		// TODO support Expression subclass ....net.sf.saxon.expr.instruct.UseAttributeSet
//		// TODO support Expression subclass ....net.sf.saxon.expr.sort.MergeInstr
//		// TODO support Expression subclass ..net.sf.saxon.expr.instruct.NumberInstruction
//		// TODO support Expression subclass ..net.sf.saxon.expr.sort.ConditionalSorter
//		// TODO support Expression subclass ..net.sf.saxon.expr.sort.SortExpression
//		// TODO support Expression subclass ..net.sf.saxon.functions.CurrentGroupCall
//		// TODO support Expression subclass ..net.sf.saxon.functions.CurrentGroupingKeyCall
//		// TODO support Expression subclass ..net.sf.saxon.functions.hof.PartialApply
//		// TODO support Expression subclass ..net.sf.saxon.functions.hof.UserFunctionReference
//		// TODO support Expression subclass ..net.sf.saxon.ma.arrays.SquareArrayConstructor
//		// TODO support Expression subclass ..net.sf.saxon.xpath.JAXPVariableReference
		else {
			newItem = new UnsupportedExpressionItem(this.schema, this.coordinator, expression);
		}
		this.uninitializedItems.add(newItem);
		return logger.traceExit(newItem);
	}

	@Override
	public INodeTestTreeItem createItemForNodeTest(NodeTest nodeTest) {
		checkNotNull(nodeTest);
		logger.traceEntry("for {} {}", nodeTest.getClass().getSimpleName(), nodeTest);
		INodeTestTreeItem newItem = null;
		if (nodeTest instanceof final AnyNodeTest anyNodeTest) {
			// NodeTest subclass AnyNodeTest
			newItem = new AnyNodeTestItem(this.schema, this.coordinator, anyNodeTest);
		} else if (nodeTest instanceof final CombinedNodeTest combinedNodeTest) {
			// NodeTest subclass CombinedNodeTest
			newItem = new CombinedNodeTestItem(this.schema, this.coordinator, combinedNodeTest);
		}
		// TODO support NodeTest subclass DocumentNodeTest
		// TODO support NodeTest subclass ErrorType
		// TODO support NodeTest subclass LocalNameTest
		// TODO support NodeTest subclass MultipleNodeKindTest
		// TODO support NodeTest subclass NamespaceTest
		else if (nodeTest instanceof final NameTest nameTest) {
			// NodeTest subclass NameTest
			newItem = new NameTestItem(this.schema, this.coordinator, nameTest);
		} else if (nodeTest instanceof final NodeKindTest nodeKindTest) {
			// NodeTest subclass NodeKindTest
			newItem = new NodeKindTestItem(this.schema, this.coordinator, nodeKindTest);
		}
		// TODO support NodeTest subclass NodeSelector
		// TODO support NodeTest subclass SameNameTest
		else {
			newItem = new UnsupportedNodeTestItem(this.schema, this.coordinator, nodeTest);
		}
		logger.trace("created item of type {}", newItem.getClass().getSimpleName());
		this.uninitializedItems.add(newItem);
		return logger.traceExit(newItem);
	}

	@Override
	public void initializeAllCreatedItems() {
		logger.traceEntry();
		logger.debug("initializing all created items, initial queue size is {}", this.uninitializedItems.size());
		int totalItemsInitialized = 0;
		while (!this.uninitializedItems.isEmpty()) {
			final IEvaluationTreeItem nextItem = this.uninitializedItems.removeFirst();
			nextItem.initialize(this);
			totalItemsInitialized++;
		}
		logger.debug("completed initialization of {} items in total", totalItemsInitialized);
		logger.traceExit();
	}

	/**
	 * Required to test the item initialization - <b>DO NOT USE</b> for anything else than unit testing.
	 *
	 * @param item
	 */
	void injectUninitializedItem(IEvaluationTreeItem item) {
		this.uninitializedItems.add(item);
	}

}
