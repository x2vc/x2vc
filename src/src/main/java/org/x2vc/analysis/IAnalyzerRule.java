package org.x2vc.analysis;

import java.util.function.Consumer;

import org.jsoup.nodes.Node;
import org.x2vc.analysis.rules.AbstractAttributeRule;
import org.x2vc.analysis.rules.AbstractElementRule;
import org.x2vc.analysis.rules.AbstractTextRule;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

/**
 * A rule that checks for a certain condition that might be exploited in order
 * to introduce an XSS vulnerability. This interface should not be implemented
 * directly, but rather by subclassing {@link AbstractElementRule},
 * {@link AbstractAttributeRule}, {@AbstractDataRule} or
 * {@link AbstractTextRule}.
 */
public interface IAnalyzerRule {

	/**
	 * @return a short technical designator to identify the rule, e.g. in log
	 *         entries.
	 */
	String getRuleID();

	/**
	 * This method is called once for each DOM node and may emit any number of data
	 * modification requests to perform follow-up checks.
	 *
	 * @param node       the DOM node to check
	 * @param descriptor the input document descriptor
	 * @param collector  a sink to send any resulting modification requests to
	 */
	void checkNode(Node node, IXMLDocumentDescriptor descriptor, Consumer<IDocumentModifier> collector);

}
