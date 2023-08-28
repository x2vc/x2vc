package org.x2vc.analysis.rules;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * An {@link IAnalyzerRule} that performs a check on data nodes.
 */
public abstract class AbstractDataRule extends AbstractRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public final void checkNode(Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		if (node instanceof final DataNode dataNode && (isApplicableTo(dataNode, xmlContainer))) {
			performCheckOn(dataNode, xmlContainer, collector);
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the node at all.
	 *
	 * @param dataNode             the data node to check
	 * @param xmlContainer the input document container
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(DataNode dataNode, IXMLDocumentContainer xmlContainer);

	/**
	 * Performs the actual check on the data node in question.
	 *
	 * @param dataNode             the data node to check
	 * @param xmlContainer the input document container
	 * @param collector            a sink to send any resulting modification
	 *                             requests to
	 */
	protected abstract void performCheckOn(DataNode dataNode, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector);

}
