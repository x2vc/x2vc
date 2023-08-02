package org.x2vc.analysis.rules;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IRuleDataModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

/**
 * An {@link IAnalyzerRule} that performs a check on an attribute level.
 */
public abstract class AbstractAttributeRule extends AbstractElementRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	protected final void performCheckOn(Element element, IXMLDocumentDescriptor descriptor,
			Consumer<IRuleDataModifier> collector) {
		logger.traceEntry();
		for (final Attribute attribute : element.attributes()) {
			if (isApplicableTo(element, attribute, descriptor)) {
				performCheckOn(element, attribute, descriptor, collector);
			}
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the attribute in
	 * question.
	 *
	 * @param element    the element the attribute belongs to
	 * @param attribute  the attribute to check
	 * @param descriptor the input document descriptor
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(Element element, Attribute attribute, IXMLDocumentDescriptor descriptor);

	/**
	 * Performs the actual check on the element in question.
	 *
	 * @param element    the element the attribute belongs to
	 * @param attribute  the attribute to check
	 * @param descriptor the input document descriptor
	 * @param collector  a sink to send any resulting modification requests to
	 */
	protected abstract void performCheckOn(Element element, Attribute attribute, IXMLDocumentDescriptor descriptor,
			Consumer<IRuleDataModifier> collector);

}
