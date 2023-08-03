package org.x2vc.analysis.rules;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

/**
 * An {@link IAnalyzerRule} that performs a check on text nodes. Note that blank
 * text nodes (see {@link TextNode#isBlank()}) will be skipped without further
 * notice.
 */
public abstract class AbstractTextRule extends AbstractRule implements IAnalyzerRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public final void checkNode(Node node, IXMLDocumentDescriptor descriptor, Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		if (node instanceof final TextNode textNode && (!textNode.isBlank())
				&& (isApplicableTo(textNode, descriptor))) {
			performCheckOn(textNode, descriptor, collector);
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the node at all.
	 *
	 * @param textNode   the text node to check
	 * @param descriptor the input document descriptor
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(TextNode textNode, IXMLDocumentDescriptor descriptor);

	/**
	 * Performs the actual check on the text node in question.
	 *
	 * @param textNode   the text node to check
	 * @param descriptor the input document descriptor
	 * @param collector  a sink to send any resulting modification requests to
	 */
	protected abstract void performCheckOn(TextNode textNode, IXMLDocumentDescriptor descriptor,
			Consumer<IDocumentModifier> collector);

}
