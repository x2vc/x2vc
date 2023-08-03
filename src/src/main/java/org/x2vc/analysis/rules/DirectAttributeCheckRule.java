package org.x2vc.analysis.rules;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.x2vc.xmldoc.DocumentValueModifier;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IDocumentValueDescriptor;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;

/**
 * Rule A.1: Check every attribute that contains the prefix used to generate he
 * values whether it is possible to inject new attributes by modifying the input
 * data.
 */
public class DirectAttributeCheckRule extends AbstractAttributeRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public String getRuleID() {
		return "A.1";
	}

	@Override
	protected boolean isApplicableTo(Element element, IXMLDocumentDescriptor descriptor) {
		// check every element
		return true;
	}

	@Override
	protected boolean isApplicableTo(Element element, Attribute attribute, IXMLDocumentDescriptor descriptor) {
		// check every attribute of element
		return true;
	}

	@Override
	protected void performCheckOn(Element element, Attribute attribute, IXMLDocumentDescriptor descriptor,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}, attribute {}", element, attribute);
		final String path = getPathToNode(element);
		logger.debug("path: {}", path);

		final String attributeName = attribute.getKey();

		// shortcut - if entire attribute name matches a data value
		Optional<ImmutableSet<IDocumentValueDescriptor>> valueDescriptors = descriptor
				.getValueDescriptors(attributeName);
		if (valueDescriptors.isPresent()) {
			handleFullMatch(attributeName, valueDescriptors.get(), collector);
		} else {
			// check for containment
			final String valuePrefix = descriptor.getValuePrefix();
			final int valueLength = descriptor.getValueLength();
			int position = attributeName.indexOf(valuePrefix, 0);
			while (position >= 0) {
				final String candidate = attributeName.substring(position, position + valueLength);
				valueDescriptors = descriptor.getValueDescriptors(candidate);
				if (valueDescriptors.isPresent()) {
					handlePartialMatch(attributeName, candidate, valueDescriptors.get(), collector);
				}
				position = attributeName.indexOf(valuePrefix, position + valueLength - 1);
			}
		}
		logger.traceExit();
	}

	/**
	 * @param attributeName
	 * @param valueDescriptors
	 * @param collector
	 */
	private void handleFullMatch(final String attributeName, ImmutableSet<IDocumentValueDescriptor> valueDescriptors,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();

		// TODO XSS Rule A.1: add payload for later handling

		for (final IDocumentValueDescriptor valueDescriptor : valueDescriptors) {
			// try to replace the entire attribute with style attribute
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(attributeName)
					.withReplacementValue("style").sendTo(collector);
			// try to replace the entire Attribute with a Javascript event handler
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(attributeName)
					.withReplacementValue("onerror").sendTo(collector);
		}
		logger.traceExit();
	}

	/**
	 * @param attributeName
	 * @param candidate
	 * @param immutableSet
	 * @param collector
	 */
	private void handlePartialMatch(String attributeName, String candidate,
			ImmutableSet<IDocumentValueDescriptor> valueDescriptors, Consumer<IDocumentModifier> collector) {
		logger.traceEntry();

		// TODO XSS Rule A.1: add payload for later handling

		for (final IDocumentValueDescriptor valueDescriptor : valueDescriptors) {
			// try to introduce new attribute by breaking the encoding
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(candidate)
					.withReplacementValue("=\"\" style=\"test\" rest").sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(candidate)
					.withReplacementValue("=\"\" onerror=\"test\" rest").sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(candidate)
					.withReplacementValue("=&quot;&quot; style=&quot;foo&quot; rest").sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor.getSchemaElementID()).withOriginalValue(candidate)
					.withReplacementValue("=&quot;&quot; onerror=&quot;foo&quot; rest").sendTo(collector);

			// TODO XSS Rule A.1: test for additional vectors with partial matches
		}
		logger.traceExit();
	}

}
