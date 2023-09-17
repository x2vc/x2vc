package org.x2vc.processor;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.Message;

/**
 * Collects the message events emitted by the XSLT processor and produces the
 * trace message objects.
 */
class TraceMessageCollector implements Consumer<Message> {

	private static final Logger logger = LogManager.getLogger();
	private List<ITraceEvent> traceEvents = Lists.newLinkedList();

	/**
	 * @return a list of all the {@link ITraceEvent} that have been collected so far
	 */
	public ImmutableList<ITraceEvent> getTraceEvents() {
		return ImmutableList.copyOf(this.traceEvents);
	}

	@Override
	public void accept(Message t) {
		// TODO re-build message collector
	}

}
