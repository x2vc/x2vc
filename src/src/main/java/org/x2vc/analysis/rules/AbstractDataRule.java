package org.x2vc.analysis.rules;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IRuleDataModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

/**
 * An {@link IAnalyzerRule} that performs a check on data nodes.
 */
public abstract class AbstractDataRule implements IAnalyzerRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public final void checkNode(Node node, IXMLDocumentDescriptor descriptor, Consumer<IRuleDataModifier> collector) {
		logger.traceEntry();
		if (node instanceof final DataNode dataNode && (isApplicableTo(dataNode, descriptor))) {
			performCheckOn(dataNode, descriptor, collector);
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the node at all.
	 *
	 * @param dataNode   the data node to check
	 * @param descriptor the input document descriptor
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(DataNode dataNode, IXMLDocumentDescriptor descriptor);

	/**
	 * Performs the actual check on the data node in question.
	 *
	 * @param dataNode   the data node to check
	 * @param descriptor the input document descriptor
	 * @param collector  a sink to send any resulting modification requests to
	 */
	protected abstract void performCheckOn(DataNode dataNode, IXMLDocumentDescriptor descriptor,
			Consumer<IRuleDataModifier> collector);

}
