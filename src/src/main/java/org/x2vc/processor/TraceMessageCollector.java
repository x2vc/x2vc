package org.x2vc.processor;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.ExtendedXSLTConstants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.Message;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * Collects the message events emitted by the XSLT processor and produces the
 * trace message objects.
 */
class TraceMessageCollector implements Consumer<Message> {

	private static final Logger logger = LogManager.getLogger();
	private Deque<Message> messages = Lists.newLinkedList();
	private List<ITraceEvent> traceEvents = Lists.newLinkedList();

	/**
	 * @return a list of all the {@link ITraceEvent} that have been collected so far
	 */
	public ImmutableList<ITraceEvent> getTraceEvents() {
		if (!this.messages.isEmpty()) {
			processRemainingMessages();
		}
		return ImmutableList.copyOf(this.traceEvents);
	}

	/**
	 * Processes all the messages that have been added to the queue.
	 */
	private void processRemainingMessages() {
		logger.traceEntry();
		while (!this.messages.isEmpty()) {
			final Message message = this.messages.removeFirst();
			final Iterator<XdmNode> contentIterator = message.getContent().children().iterator();
			while (contentIterator.hasNext()) {
				final XdmNode node = contentIterator.next();
				final XdmNodeKind nodeKind = node.getNodeKind();
				if (nodeKind == XdmNodeKind.ELEMENT) {
					processMessageElement(node);
				} else {
					logger.debug("ignoring message content: {}", node);
				}
			}
		}
		logger.traceExit();
	}

	/**
	 * Processes an XML element encountered in a message.
	 *
	 * @param node
	 */
	private void processMessageElement(XdmNode node) {
		logger.traceEntry();
		final QName nodeName = node.getNodeName();
		if (nodeName.getNamespace().equals(ExtendedXSLTConstants.NAMESPACE)) {
			final String localName = nodeName.getLocalName();
			if (ExtendedXSLTConstants.Elements.TRACE.equals(localName)) {
				final int traceID = Integer
						.parseInt(node.getAttributeValue(new QName(ExtendedXSLTConstants.Attributes.TRACE_ID)));
				final String elementName = node.getAttributeValue(new QName(ExtendedXSLTConstants.Attributes.ELEMENT));
				this.traceEvents.add(new TraceEvent(traceID, elementName));
			} else {
				logger.debug("ignoring unknown trace element: {}", node);
			}
		} else {
			logger.debug("ignoring foreign XML element: {}", node);
		}
		logger.traceExit();
	}

	@Override
	public void accept(Message t) {
		logger.debug("Accepted tracing message {}", t);
		this.messages.addLast(t);
	}

}
