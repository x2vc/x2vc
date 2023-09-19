package org.x2vc.processor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;
import org.x2vc.utilities.PolymorphLocation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.TraceExpression;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Message;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.Mode;

/**
 * Collects the message events emitted by the XSLT processor and produces the trace message objects.
 */
class ProcessorObserver implements Consumer<Message>, ErrorListener, TraceListener {

	private static final Logger logger = LogManager.getLogger();
	private List<ITraceEvent> traceEvents = Lists.newLinkedList();
	private boolean tracingEnabled = true;

	/**
	 * @return a list of all the {@link ITraceEvent} that have been collected so far
	 */
	public ImmutableList<ITraceEvent> getTraceEvents() {
		return ImmutableList.copyOf(this.traceEvents);
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
	}

}
