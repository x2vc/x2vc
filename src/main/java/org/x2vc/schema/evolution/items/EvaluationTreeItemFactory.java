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
import net.sf.saxon.expr.instruct.*;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;
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
		// Expression subclass ..net.sf.saxon.expr.Assignation (abstract)
		if (expression instanceof final ForExpression forExpression) {
			// Expression subclass ....net.sf.saxon.expr.ForExpression
			// Expression subclass ......net.sf.saxon.expr.flwor.OuterForExpression
			newItem = new AssignationItem<ForExpression>(this.schema, this.coordinator, forExpression);
		} else if (expression instanceof final LetExpression letExpression) {
			// Expression subclass ....net.sf.saxon.expr.LetExpression
			// Expression subclass ......net.sf.saxon.expr.EagerLetExpression
			newItem = new AssignationItem<LetExpression>(this.schema, this.coordinator, letExpression);
		} else if (expression instanceof final QuantifiedExpression quantifiedExpression) {
			// Expression subclass ....net.sf.saxon.expr.QuantifiedExpression
			newItem = new AssignationItem<QuantifiedExpression>(this.schema, this.coordinator, quantifiedExpression);
		} else if (expression instanceof final AttributeGetter attributeGetter) {
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
			newItem = createItemForFilterExpression(filterExpression);
		} else if (expression instanceof final GeneralComparison generalComparison) {
			// Expression subclass ....net.sf.saxon.expr.GeneralComparison (abstract)
			// Expression subclass ......net.sf.saxon.expr.GeneralComparison20
			newItem = new IndependentBinaryExpressionItem<GeneralComparison>(this.schema, this.coordinator,
					generalComparison);
			// TODO #12 support Expression subclass ....net.sf.saxon.expr.IdentityComparison
			// TODO #12 support Expression subclass ....net.sf.saxon.expr.LookupExpression
		} else if (expression instanceof final SlashExpression slashExpression) {
			// Expression subclass ....net.sf.saxon.expr.SlashExpression
			// Expression subclass ......net.sf.saxon.expr.SimpleStepExpression
			newItem = new SlashExpressionItem(this.schema, this.coordinator, slashExpression);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.SwitchCaseComparison
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
			// Expression subclass ..net.sf.saxon.expr.ContextItemExpression
			// Expression subclass ....net.sf.saxon.expr.CurrentItemExpression
			// Although technically a value access, we can't learn anything new from a "this" (.) access...
			newItem = new NoOperationItem<ContextItemExpression>(this.schema, this.coordinator, contextItemExpression);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.DynamicFunctionCall
		else if (expression instanceof final ErrorExpression errorExpression) {
			// Expression subclass ..net.sf.saxon.expr.ErrorExpression
			newItem = new NoOperationItem<ErrorExpression>(this.schema, this.coordinator, errorExpression);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.FunctionCall (abstract)
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.StaticFunctionCall
		else if (expression instanceof final SystemFunctionCall systemFunctionCall) {
			// Expression subclass ......net.sf.saxon.expr.SystemFunctionCall
			// Expression subclass ........net.sf.saxon.expr.SystemFunctionCall.Optimized (abstract)
			newItem = new SystemFunctionCallItem(this.schema, this.coordinator, systemFunctionCall);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.UserFunctionCall
		else if (expression instanceof final IntegratedFunctionCall integratedFunctionCall) {
			// Expression subclass ....net.sf.saxon.functions.IntegratedFunctionCall
			newItem = new IntegratedFunctionCallItem(this.schema, this.coordinator, integratedFunctionCall);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.xpath.XPathFunctionCall
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.IntegerRangeTest
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.IsLastExpression
		else if (expression instanceof final Literal literal) {
			// Expression subclass ..net.sf.saxon.expr.Literal
			// Expression subclass ....net.sf.saxon.expr.StringLiteral
			// Expression subclass ....net.sf.saxon.functions.hof.FunctionLiteral
			// no value access to be extracted here
			newItem = new NoOperationItem<Literal>(this.schema, this.coordinator, literal);
		} else if (expression instanceof final NumberSequenceFormatter numberSequenceFormatter) {
			// Expression subclass ..net.sf.saxon.expr.NumberSequenceFormatter
			newItem = new NumberSequenceFormatterItem(this.schema, this.coordinator, numberSequenceFormatter);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.PseudoExpression (abstract)
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.DefaultedArgumentExpression
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.DefaultedArgumentExpression.DefaultCollationArgument
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinition
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.sort.SortKeyDefinitionList
		// TODO #12 support Expression subclass ....net.sf.saxon.pattern.Pattern (abstract)
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.AncestorQualifiedPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.AnchorPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.BasePatternWithPredicate
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.BooleanExpressionPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.GeneralNodePattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.GeneralPositionalPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.ItemTypePattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.NodeSetPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.NodeTestPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.PatternThatSetsCurrent
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.SimplePositionalPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.StreamingFunctionArgumentPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.UniversalPattern
		// TODO #12 support Expression subclass ......net.sf.saxon.pattern.VennPattern (abstract)
		// TODO #12 support Expression subclass ........net.sf.saxon.pattern.ExceptPattern
		// TODO #12 support Expression subclass ........net.sf.saxon.pattern.IntersectPattern
		// TODO #12 support Expression subclass ........net.sf.saxon.pattern.UnionPattern
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.RangeExpression
		else if (expression instanceof final RootExpression rootExpression) {
			// Expression subclass ..net.sf.saxon.expr.RootExpression
			newItem = new RootExpressionItem(this.schema, this.coordinator, rootExpression);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.SimpleExpression (abstract)
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.SuppliedParameterReference
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.TryCatch
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.UnaryExpression (abstract)
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
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.CompareToConstant (abstract)
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.CompareToIntegerConstant
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.CompareToStringConstant
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.ConsumingOperand
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.EmptyTextNodeRemover
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.HomogeneityChecker
		else if (expression instanceof final InstanceOfExpression instanceOfExpression) {
			// Expression subclass ....net.sf.saxon.expr.InstanceOfExpression
			newItem = new UnaryExpressionItem<InstanceOfExpression>(this.schema, this.coordinator,
					instanceOfExpression);
		} else if (expression instanceof final ItemChecker itemChecker) {
			// Expression subclass ....net.sf.saxon.expr.ItemChecker
			// The check itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<ItemChecker>(this.schema, this.coordinator, itemChecker);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.LookupAllExpression
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.NegateExpression
		else if (expression instanceof final SingleItemFilter singleItemFilter) {
			// Expression subclass ....net.sf.saxon.expr.SingleItemFilter (abstract)
			// Expression subclass ......net.sf.saxon.expr.FirstItemExpression
			// Expression subclass ......net.sf.saxon.expr.LastItemExpression
			// Expression subclass ......net.sf.saxon.expr.SubscriptExpression
			// The item selection itself does not constitute a value access, but check the contained expression.
			newItem = new UnaryExpressionItem<SingleItemFilter>(this.schema, this.coordinator, singleItemFilter);
		} else if (expression instanceof final SingletonAtomizer singletonAtomizer) {
			// Expression subclass ....net.sf.saxon.expr.SingletonAtomizer
			newItem = new UnaryExpressionItem<SingletonAtomizer>(this.schema, this.coordinator, singletonAtomizer);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.TailCallLoop
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.TailExpression
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.OnEmptyExpr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.OnNonEmptyExpr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.SequenceInstr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.WherePopulated
		else if (expression instanceof final DocumentSorter documentSorter) {
			// Expression subclass ....net.sf.saxon.expr.sort.DocumentSorter
			newItem = new UnaryExpressionItem<DocumentSorter>(this.schema, this.coordinator, documentSorter);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.functions.hof.FunctionSequenceCoercer
		else if (expression instanceof final VariableReference variableReference) {
			// Expression subclass ..net.sf.saxon.expr.VariableReference (abstract)
			// Expression subclass ....net.sf.saxon.expr.GlobalVariableReference
			// Expression subclass ....net.sf.saxon.expr.LocalVariableReference
			// Variable references do not constitute a context access and do not have any sub-expressions to check
			newItem = new NoOperationItem<VariableReference>(this.schema, this.coordinator, variableReference);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.flwor.FLWORExpression
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.flwor.TupleExpression
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.instruct.EvaluateInstr
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.instruct.Instruction (abstract)
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.AnalyzeString
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ApplyNextMatchingTemplate (abstract)
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.ApplyImports
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.NextMatch
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ApplyTemplates
		else if (expression instanceof final Block block) {
			// Expression subclass ....net.sf.saxon.expr.instruct.Block
			newItem = new BlockItem(this.schema, this.coordinator, block);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.BreakInstr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.CallTemplate
		else if (expression instanceof final Choose choose) {
			// Expression subclass ....net.sf.saxon.expr.instruct.Choose
			newItem = new ChooseItem(this.schema, this.coordinator, choose);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ComponentTracer
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ConditionalBlock
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.CopyOf
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.Doctype
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ForEach
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ForEachGroup
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.Fork
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.IterateInstr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.LocalParam
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.LocalParamBlock
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.MessageInstr
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.NextIteration
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ParentNodeConstructor (abstract)
		else if (expression instanceof final DocumentInstr documentInstr) {
			// Expression subclass ......net.sf.saxon.expr.instruct.DocumentInstr
			newItem = new DocumentInstrItem(this.schema, this.coordinator, documentInstr);
		}
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.ElementCreator (abstract)
		// TODO #12 support Expression subclass ........net.sf.saxon.expr.instruct.ComputedElement
		// TODO #12 support Expression subclass ........net.sf.saxon.expr.instruct.Copy
		// TODO #12 support Expression subclass ........net.sf.saxon.expr.instruct.FixedElement
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.ResultDocument
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.SimpleNodeConstructor (abstract)
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.AttributeCreator (abstract)
		// TODO #12 support Expression subclass ........net.sf.saxon.expr.instruct.ComputedAttribute
		// TODO #12 support Expression subclass ........net.sf.saxon.expr.instruct.FixedAttribute
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.Comment
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.NamespaceConstructor
		// TODO #12 support Expression subclass ......net.sf.saxon.expr.instruct.ProcessingInstruction
		else if (expression instanceof final ValueOf valueOf) {
			// Expression subclass ......net.sf.saxon.expr.instruct.ValueOf
			newItem = new ValueOfItem(this.schema, this.coordinator, valueOf);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.SourceDocument
		else if (expression instanceof final TraceExpression traceExpression) {
			// Expression subclass ....net.sf.saxon.expr.instruct.TraceExpression
			// We don't care for the trace expression, but have to examine its sub-expression
			newItem = new TraceExpressionItem(this.schema, this.coordinator, traceExpression);
		}
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.instruct.UseAttributeSet
		// TODO #12 support Expression subclass ....net.sf.saxon.expr.sort.MergeInstr
		else if (expression instanceof final NumberInstruction numberInstruction) {
			// Expression subclass ..net.sf.saxon.expr.instruct.NumberInstruction
			newItem = new NumberInstructionItem(this.schema, this.coordinator, numberInstruction);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.expr.sort.ConditionalSorter
		else if (expression instanceof final SortExpression sortExpression) {
			// Expression subclass ..net.sf.saxon.expr.sort.SortExpression
			newItem = new SortExpressionItem(this.schema, this.coordinator, sortExpression);
		}
		// TODO #12 support Expression subclass ..net.sf.saxon.functions.CurrentGroupCall
		// TODO #12 support Expression subclass ..net.sf.saxon.functions.CurrentGroupingKeyCall
		// TODO #12 support Expression subclass ..net.sf.saxon.functions.hof.PartialApply
		// TODO #12 support Expression subclass ..net.sf.saxon.functions.hof.UserFunctionReference
		// TODO #12 support Expression subclass ..net.sf.saxon.ma.arrays.SquareArrayConstructor
		// TODO #12 support Expression subclass ..net.sf.saxon.xpath.JAXPVariableReference
		else {
			newItem = new UnsupportedExpressionItem(this.schema, this.coordinator, expression);
		}
		this.uninitializedItems.add(newItem);
		return logger.traceExit(newItem);
	}

	/**
	 * @param filterExpression
	 * @return
	 */
	protected IEvaluationTreeItem createItemForFilterExpression(FilterExpression target) {
		logger.traceEntry("for FilterExpression {}", target);

		// prepare a simple default value first...
		IEvaluationTreeItem newItem;
		newItem = new FilterExpressionItem(this.schema, this.coordinator, target);

		// Unfortunately FilterExpressions require special handling...
		// There is a common expression "//foo/bar" that leads an expression that looks a bit weird at first glance.
		// It is transformed into "descendant::element(Q{}bar)[parent::element(Q{}foo)]" (which is correct: "find any
		// descendant element bar that has a parant foo"). However, for these cases, we need to reverse the order of
		// operations: evaluate the filter expression first to get the parent, then the base expression to get the
		// descendant element. We effectively need to transform this special case into
		// "descendant::element(Q{}foo)/child::element(Q{}bar)". This would be represented by a SlashExpression

		// Things get even more complicated because both the left-hand and the right-hand side element might be wrapped
		// in an exists() system function call, which has to be taken into account as well: both
		// "descendant::element(Q{}bar)[exists(parent::element(Q{}foo)]" and
		// "exists(descendant::element(Q{}bar)[exists(parent::element(Q{}foo))])" have to become
		// "descendant::element(Q{}foo)/child::element(Q{}bar)[exists(.)]".

		final Expression originalBaseExpression = target.getBase();
		final Expression originalFilterExpression = target.getFilter();

		SystemFunction wrappingTargetFunction = null;

		Expression baseExpression = originalBaseExpression;

		// check the base expression whether it needs to be unwrapped
		SystemFunctionCall baseWrappingSystemFunctionCall = null;
		if (baseExpression instanceof final SystemFunctionCall sfc) {
			final StructuredQName functionName = sfc.getFunctionName();
			if (functionName.getNamespaceUri().equals(NamespaceUri.FN)
					&& functionName.getLocalPart().equals("exists")) {
				baseWrappingSystemFunctionCall = sfc;
				baseExpression = baseWrappingSystemFunctionCall.getArg(0);
				wrappingTargetFunction = baseWrappingSystemFunctionCall.getTargetFunction();
			}
		}

		// check base expression whether it is a descendant search
		if (baseExpression instanceof final AxisExpression baseAxisExpression
				&& ((baseAxisExpression.getAxis() == AxisInfo.DESCENDANT)
						|| (baseAxisExpression.getAxis() == AxisInfo.DESCENDANT_OR_SELF))) {

			// check the filter expression whether it needs to be unwrapped
			Expression filterExpression = originalFilterExpression;
			SystemFunctionCall filterWrappingSystemFunctionCall = null;
			if (filterExpression instanceof final SystemFunctionCall sfc) {
				final StructuredQName functionName = sfc.getFunctionName();
				if (functionName.getNamespaceUri().equals(NamespaceUri.FN)
						&& functionName.getLocalPart().equals("exists")) {
					filterWrappingSystemFunctionCall = sfc;
					filterExpression = filterWrappingSystemFunctionCall.getArg(0);
					wrappingTargetFunction = filterWrappingSystemFunctionCall.getTargetFunction();
				}
			}

			// see if the filter expression ascends the parent axis
			if (filterExpression instanceof final AxisExpression filterAxisExpression
					&& (filterAxisExpression.getAxis() == AxisInfo.PARENT)) {

				// looks like it - so let's reformat this

				// create an AxisExpression for the first step
				final Expression firstStepExpression = new AxisExpression(baseAxisExpression.getAxis(),
						filterAxisExpression.getNodeTest());

				// create an AxisExpression for the second step
				Expression secondStepExpression = new AxisExpression(AxisInfo.CHILD,
						baseAxisExpression.getNodeTest());

				// wrap if required
				if (wrappingTargetFunction != null) {
					secondStepExpression = new FilterExpression(secondStepExpression,
							new SystemFunctionCall(wrappingTargetFunction,
									new Expression[] { new ContextItemExpression() }));
				}

				// create slash expression
				final SlashExpression slashExpression = new SlashExpression(firstStepExpression, secondStepExpression);

				logger.debug("replaced expression {} with {}", target, slashExpression);

				newItem = createItemForExpression(slashExpression);
			}
		}

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
		// TODO #12 support NodeTest subclass DocumentNodeTest
		// TODO #12 support NodeTest subclass ErrorType
		// TODO #12 support NodeTest subclass LocalNameTest
		else if (nodeTest instanceof final MultipleNodeKindTest multipleNodeKindTest) {
			// NodeTest subclass MultipleNodeKindTest
			newItem = new MultipleNodeKindTestItem(this.schema, this.coordinator, multipleNodeKindTest);
		}
		// TODO #12 support NodeTest subclass NamespaceTest
		else if (nodeTest instanceof final NameTest nameTest) {
			// NodeTest subclass NameTest
			newItem = new NameTestItem(this.schema, this.coordinator, nameTest);
		} else if (nodeTest instanceof final NodeKindTest nodeKindTest) {
			// NodeTest subclass NodeKindTest
			newItem = new NodeKindTestItem(this.schema, this.coordinator, nodeKindTest);
		}
		// TODO #12 support NodeTest subclass NodeSelector
		// TODO #12 support NodeTest subclass SameNameTest
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
