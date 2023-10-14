package org.x2vc.processor;

import java.util.*;
import java.util.function.Consumer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;
import org.x2vc.processor.ValueAccessTraceEvent.Builder;
import org.x2vc.utilities.PolymorphLocation;
import org.x2vc.xml.document.IDocumentGenerator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.*;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Genre;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Message;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.type.Type;

/**
 * Collects the message events emitted by the XSLT processor and produces the trace message objects.
 */
class ProcessorObserver implements Consumer<Message>, ErrorListener, TraceListener {

	private static final Logger logger = LogManager.getLogger();
	private List<ITraceEvent> traceEvents = Lists.newLinkedList();
	private boolean tracingEnabled = true;
	private RebindingMap noRebindingMap = new RebindingMap();
	private UUID documentTraceID = UUID.randomUUID();

	/**
	 * @return a list of all the {@link ITraceEvent} that have been collected so far
	 */
	public ImmutableList<ITraceEvent> getTraceEvents() {
		return ImmutableList.copyOf(this.traceEvents);
	}

	/**
	 * @return the trace ID used for the document root node
	 */
	public UUID getDocumentTraceID() {
		return this.documentTraceID;
	}

	// ===== callback methods used by the transformer =======================================================

	@Override
	public void accept(Message t) {
		logger.trace("Accepted transformer tracing message {}", t);
	}

	@Override
	public void warning(TransformerException exception) throws TransformerException {
		logger.trace("Received transformer warning: {}", exception.getMessage(), exception);

	}

	@Override
	public void error(TransformerException exception) throws TransformerException {
		logger.trace("Received transformer error: {}", exception.getMessage(), exception);
	}

	@Override
	public void fatalError(TransformerException exception) throws TransformerException {
		logger.trace("Received transformer fatal error: {}", exception.getMessage(), exception);
	}

	@Override
	public void setOutputDestination(net.sf.saxon.lib.Logger stream) {
		logger.trace("Attempt to set transformer trace output destination to {} ignored", stream);
	}

	@Override
	public void open(Controller controller) {
		logger.trace("Transformer trace output opened");
	}

	@Override
	public void close() {
		logger.trace("Transformer trace output closed");
	}

	@Override
	public void enter(Traceable traceable, Map<String, Object> properties, XPathContext context) {
		logger.trace("Transformer entering processing of element {}", traceable.getClass().getSimpleName());
		if (this.tracingEnabled) {
			recordExecutionEvent(traceable, ExecutionEventType.ENTER);
			recordValueAccessEvent(traceable, context);
		}
	}

	@Override
	public void leave(Traceable traceable) {
		logger.trace("Transformer leaving processing of element {}", traceable.getClass().getSimpleName());
		if (this.tracingEnabled) {
			recordExecutionEvent(traceable, ExecutionEventType.LEAVE);
		}
	}

	@Override
	public void startCurrentItem(Item currentItem) {
		logger.trace("Transformer starting current item {}", currentItem);
	}

	@Override
	public void endCurrentItem(Item currentItem) {
		logger.trace("Transformer ending current item {}", currentItem);
	}

	@Override
	public void startRuleSearch() {
		logger.trace("Transformer starting rule search");
	}

	@Override
	public void endRuleSearch(Object rule, Mode mode, Item item) {
		logger.trace("Transformer ending rule search");
	}

	// ===== internal event processing ==========================================================

	/**
	 * Records an {@link IExecutionTraceEvent}.
	 *
	 * @param traceable
	 * @param eventType
	 */
	protected void recordExecutionEvent(Traceable traceable, final ExecutionEventType eventType) {
		logger.traceEntry();
		// ignore trace expressions
		if (traceable instanceof TraceExpression) {
			return;
		}
		String executedElement = null;
		if (traceable instanceof final Expression expression) {
			executedElement = expression.getTracingTag();
		} else if (traceable instanceof final TraceableComponent component) {
			executedElement = component.getTracingTag();
		}
		if (!Strings.isNullOrEmpty(executedElement)) {
			executedElement = executedElement.replace("xsl:", "");
		} else {
			executedElement = null;
		}
		this.traceEvents.add(ExecutionTraceEvent.builder()
			.withEventType(eventType)
			.withExecutedElement(executedElement)
			.withElementLocation(PolymorphLocation.from(traceable.getLocation()))
			.build());
		logger.traceExit();
	}

	/**
	 * Records one or multiple {@link IValueAccessTraceEvent}s.
	 *
	 * @param traceable
	 * @param context
	 */
	private void recordValueAccessEvent(Traceable traceable, XPathContext context) {
		logger.traceEntry();
		// only consider instructions for now
		if (traceable instanceof final Instruction instruction) {
			// Determine the current context element that all relative evaluations refer to.
			// Current assumption is that this will always be a node at this point.
			final Optional<NodeInfo> node = getContextNode(context);
			final Optional<UUID> elementID = getContextElementID(node);
			recordValueAccessForInstruction(instruction, elementID);
		}
		logger.traceExit();
	}

	/**
	 * Determines the context node from the context.
	 *
	 * @param context
	 * @return
	 */
	private Optional<NodeInfo> getContextNode(XPathContext context) {
		logger.traceEntry();
		Optional<NodeInfo> result = Optional.empty();
		final Item contextItem = context.getContextItem();
		if (contextItem.getGenre() == Genre.NODE) {
			if (contextItem instanceof final NodeInfo nodeInfo) {
				result = Optional.of(nodeInfo);
			} else {
				throw logger.throwing(new IllegalArgumentException("Item of Genre NODE does not implement NodeInfo"));
			}
		} else {
			logger.warn("Unsupported genre {} encountered when trying to determine element ID", contextItem.getGenre());
		}
		return logger.traceExit(result);
	}

	/**
	 * Determine the elementID of the context node introduced by the document generator.
	 *
	 * @param contextNode
	 * @return the elementID
	 */
	private Optional<UUID> getContextElementID(Optional<NodeInfo> contextNode) {
		logger.traceEntry();
		Optional<UUID> result = Optional.empty();
		if (contextNode.isPresent()) {
			final NodeInfo node = contextNode.get();
			if (node.getNodeKind() == Type.DOCUMENT) {
				// this is the document node that we can't assign a trace ID to in the regular fashion
				logger.debug("Context node is the document root, using ID {}", this.documentTraceID);
				result = Optional.of(this.documentTraceID);
			} else {
				final String elementIDAsString = contextNode.get().getAttributeValue(
						IDocumentGenerator.TRACE_ELEMENT_NAMESPACE,
						IDocumentGenerator.TRACE_ATTRIBUTE_ELEMENT_ID);
				if (Strings.isNullOrEmpty(elementIDAsString)) {
					logger.debug("Context node is either missing or has no trace ID assigned");
				} else {
					try {
						final UUID elementID = UUID.fromString(elementIDAsString);
						logger.trace("Identified context element ID {}", elementID);
						result = Optional.of(elementID);
					} catch (final IllegalArgumentException e) {
						logger.warn("Malformed element ID \"{}\" will be ignored", elementIDAsString);
					}
				}
			}
		} else {
			logger.debug("Context node is missing");
		}
		return logger.traceExit(result);
	}

	/**
	 * @param expression
	 * @param elementID
	 */
	@SuppressWarnings("java:S3776") // desperately waiting for https://openjdk.org/jeps/433 to simplify this
	private void recordValueAccessForInstruction(Instruction instruction, Optional<UUID> elementID) {
		logger.traceEntry("with instruction {}", instruction);

		// TODO XSLT 3.0: check Expression --> Instruction --> AnalyzeString
		if (instruction instanceof ApplyNextMatchingTemplate) {
			// Expression --> Instruction --> ApplyNextMatchingTemplate (abstract)
			// Expression --> Instruction --> ApplyNextMatchingTemplate --> ApplyImports
			// Expression --> Instruction --> ApplyNextMatchingTemplate --> NextMatch
			// no value access to analyze here
		} else if (instruction instanceof final ApplyTemplates applyTemplates) {
			// Expression --> Instruction --> ApplyTemplates
			recordValueAccessForExpression(instruction, applyTemplates.getSelectExpression(), elementID);
			// TODO consider xsl:sort below apply-templates
		} else if (instruction instanceof Block) {
			// Expression --> Instruction --> Block
			// no value access to analyze here
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> BreakInstr
		else if (instruction instanceof CallTemplate) {
			// Expression --> Instruction --> CallTemplate
			// nothing to do here since all values are known statically
		} else if (instruction instanceof final Choose choose) {
			// Expression --> Instruction --> Choose
			choose.conditions()
				.forEach(c -> recordValueAccessForExpression(instruction, c.getChildExpression(), elementID));
		} else if (instruction instanceof ComponentTracer) {
			// Expression --> Instruction --> ComponentTracer
			// ignore trace expressions for now
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> ConditionalBlock
		else if (instruction instanceof final CopyOf copyOf) {
			// Expression --> Instruction --> CopyOf
			recordValueAccessForExpression(instruction, copyOf.getSelect(), elementID);
		}
		// TODO Saxon proprietary: check Expression --> Instruction --> Doctype
		else if (instruction instanceof final ForEach forEach) {
			// Expression --> Instruction --> ForEach
			recordValueAccessForExpression(instruction, forEach.getSelect(), elementID);
			// TODO consider xsl:sort below for-each
		}
		// TODO XSLT 2.0: check Expression --> Instruction --> ForEachGroup
		// TODO XSLT 3.0: check Expression --> Instruction --> Fork
		// TODO XSLT 3.0: check Expression --> Instruction --> IterateInstr
		else if (instruction instanceof final LocalParam localParam) {
			// Expression --> Instruction --> LocalParam
			recordValueAccessForExpression(instruction, localParam.getSelectExpression(), elementID);
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> LocalParamBlock
		else if (instruction instanceof final MessageInstr messageInstr) {
			// Expression --> Instruction --> MessageInstr
			recordValueAccessForExpression(instruction, messageInstr.getErrorCode(), elementID);
			recordValueAccessForExpression(instruction, messageInstr.getSelect(), elementID);
			recordValueAccessForExpression(instruction, messageInstr.getTerminate(), elementID);
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> NextIteration
		else if (instruction instanceof DocumentInstr) {
			// Expression --> Instruction --> ParentNodeConstructor --> DocumentInstr
			// nothing to do here since all values are known statically
		} else if (instruction instanceof final ComputedElement computedElement) {
			// Expression --> Instruction --> ParentNodeConstructor --> ElementCreator --> ComputedElement
			recordValueAccessForExpression(instruction, computedElement.getNameExp(), elementID);
			recordValueAccessForExpression(instruction, computedElement.getNamespaceExp(), elementID);
		} else if (instruction instanceof Copy) {
			// Expression --> Instruction --> ParentNodeConstructor --> ElementCreator --> Copy
			// nothing to do here since all values are known statically
		} else if (instruction instanceof FixedElement) {
			// Expression --> Instruction --> ParentNodeConstructor --> ElementCreator --> FixedElement
			// nothing to do here since all values are known statically
		}
		// TODO XSLT 2.0: check Expression --> Instruction --> ResultDocument
		else if (instruction instanceof final ComputedAttribute computedAttribute) {
			// Expression --> Instruction --> SimpleNodeConstructor --> AttributeCreator --> ComputedAttribute
			recordValueAccessForExpression(instruction, computedAttribute.getNameExp(), elementID);
			recordValueAccessForExpression(instruction, computedAttribute.getNamespaceExp(), elementID);
			recordValueAccessForExpression(instruction, computedAttribute.getSelect(), elementID);
		} else if (instruction instanceof final FixedAttribute fixedAttribute) {
			// Expression --> Instruction --> SimpleNodeConstructor --> AttributeCreator --> FixedAttribute
			recordValueAccessForExpression(instruction, fixedAttribute.getSelect(), elementID);
		} else if (instruction instanceof Comment) {
			// Expression --> Instruction --> SimpleNodeConstructor --> Comment
			// nothing to do here since all values are known statically
		}
		// TODO XSLT 2.0: check Expression --> Instruction --> SimpleNodeConstructor --> NamespaceConstructor
		else if (instruction instanceof final ProcessingInstruction processingInstruction) {
			// Expression --> Instruction --> SimpleNodeConstructor --> ProcessingInstruction
			recordValueAccessForExpression(instruction, processingInstruction.getNameExp(), elementID);
		} else if (instruction instanceof final ValueOf valueOf) {
			// Expression --> Instruction --> SimpleNodeConstructor --> ValueOf
			recordValueAccessForExpression(instruction, valueOf.getSelect(), elementID);
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> SourceDocument
		else if (instruction instanceof TraceExpression) {
			// Expression --> Instruction --> TraceExpression
			// ignore trace expressions for now
		} else if (instruction instanceof UseAttributeSet) {
			// Expression --> Instruction --> UseAttributeSet
			// nothing to do here since all values are known statically
		}
		// TODO XSLT 3.0: check Expression --> Instruction --> MergeInstr
		else {
			logger.warn("Instruction type {} not yet covered by value access trace",
					instruction.getClass().getSimpleName());
		}
	}

	/**
	 * @param instruction
	 * @param expression
	 * @param elementID
	 */
	private void recordValueAccessForExpression(Instruction instruction, Expression expression,
			Optional<UUID> elementID) {
		// performing the null check here makes the calling method much more readable
		if (expression == null) {
			return;
		}
		logger.traceEntry("with expression {} out of instruction {}", expression, instruction);
		// make a deep copy of the expression because it might be altered during the processing
		Expression expCopy;
		try {
			expCopy = expression.copy(this.noRebindingMap);
		} catch (final ConcurrentModificationException e) {
			// TODO Check what to do with CMEs during Expression.copy() - report bug?
			// see also https://saxonica.plan.io/issues/4363
			logger.warn(
					"Encountered what is (probably) a bug in Saxon. Will use original expression instead of copy - beware of side effects");
			expCopy = expression;
		}

		final Builder builder = ValueAccessTraceEvent.builder()
			.withExpression(expCopy)
			.withLocation(instruction.getSourceLocator());
		if (elementID.isPresent()) {
			builder.withContextElementID(elementID.get());
		}
		this.traceEvents.add(builder.build());
	}

}
