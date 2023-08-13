package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IVulnerabilityReport;
import org.x2vc.analysis.VulnerabilityReport;
import org.x2vc.xml.document.*;

import com.google.common.collect.ImmutableSet;

/**
 * Rule A.1: Check every attribute that contains the prefix used to generate he
 * values whether it is possible to inject new attributes by modifying the input
 * data.
 */
public class DirectAttributeCheckRule extends AbstractAttributeRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "A.1";

	private static final Logger logger = LogManager.getLogger();

	@Override
	public String getRuleID() {
		return RULE_ID;
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
		final String elementPath = getPathToNode(element);
		final String attributeName = attribute.getKey();

		// shortcut - if entire attribute name matches a data value
		Optional<ImmutableSet<IDocumentValueDescriptor>> valueDescriptors = descriptor
			.getValueDescriptors(attributeName);
		if (valueDescriptors.isPresent()) {
			handleFullMatch(elementPath, attributeName, valueDescriptors.get(), collector);
		} else {
			// check for containment
			final String valuePrefix = descriptor.getValuePrefix();
			final int valueLength = descriptor.getValueLength();
			int position = attributeName.indexOf(valuePrefix, 0);
			while (position >= 0) {
				final String candidate = attributeName.substring(position, position + valueLength);
				valueDescriptors = descriptor.getValueDescriptors(candidate);
				if (valueDescriptors.isPresent()) {
					handlePartialMatch(elementPath, candidate, valueDescriptors.get(), collector);
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
	private void handleFullMatch(final String elementSelector, final String attributeName,
			ImmutableSet<IDocumentValueDescriptor> valueDescriptors, Consumer<IDocumentModifier> collector) {
		logger.traceEntry();

		// TODO XSS Rule A.1: add payload for later handling

		for (final IDocumentValueDescriptor valueDescriptor : valueDescriptors) {
			// try to replace the entire attribute with style attribute
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID)
				.withOriginalValue(attributeName).withReplacementValue("style")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "style")).sendTo(collector);
			// try to replace the entire Attribute with a Javascript event handler
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID)
				.withOriginalValue(attributeName).withReplacementValue("onerror")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "onerror")).sendTo(collector);
		}
		logger.traceExit();
	}

	/**
	 * @param candidate
	 * @param immutableSet
	 * @param collector
	 */
	private void handlePartialMatch(final String elementSelector, String candidate,
			ImmutableSet<IDocumentValueDescriptor> valueDescriptors, Consumer<IDocumentModifier> collector) {
		logger.traceEntry();

		for (final IDocumentValueDescriptor valueDescriptor : valueDescriptors) {
			// try to introduce new attribute by breaking the encoding
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID).withOriginalValue(candidate)
				.withReplacementValue("=\"\" style=\"test\" rest")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "style", "test")).sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID).withOriginalValue(candidate)
				.withReplacementValue("=\"\" onerror=\"test\" rest")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "onerror", "test")).sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID).withOriginalValue(candidate)
				.withReplacementValue("=&quot;&quot; style=&quot;foo&quot; rest")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "style", "foo")).sendTo(collector);
			new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID).withOriginalValue(candidate)
				.withReplacementValue("=&quot;&quot; onerror=&quot;foo&quot; rest")
				.withPayload(new DirectAttributeCheckPayload(elementSelector, "onerror", "foo")).sendTo(collector);

			// TODO XSS Rule A.1: test for additional vectors with partial matches
		}
		logger.traceExit();
	}

	@Override
	public Set<String> getElementSelectors(IXMLDocumentDescriptor descriptor) {
		logger.traceEntry();
		final DirectAttributeCheckPayload payload = getPayloadChecked(descriptor, DirectAttributeCheckPayload.class);
		return logger.traceExit(Set.of(payload.getElementSelector()));
	}

	@Override
	public void verifyNode(Node node, IXMLDocumentDescriptor descriptor, Consumer<IVulnerabilityReport> collector) {
		logger.traceEntry();
		final DirectAttributeCheckPayload payload = getPayloadChecked(descriptor, DirectAttributeCheckPayload.class);
		if (node instanceof final Element element) {
			final String attributeName = payload.getInjectedAttribute();
			final String injectedValue = payload.getInjectedValue();
			final String actualValue = element.attr(attributeName);
			if (actualValue == null) {
				logger.debug("attribute \"{}\" not found, follow-up check negative", attributeName);
			} else {
				if (actualValue.equalsIgnoreCase(injectedValue)) {
					logger.debug("attribute \"{}\" contains injected value \"{}\", follow-up check positive",
							attributeName, injectedValue);
					collector.accept(new VulnerabilityReport());
					// TODO XSS Vulnerability: produce a proper report object
				} else {
					logger.debug(
							"attribute \"{}\" with value \"{}\" does not contain injected value \"{}\", follow-up check negative",
							attributeName, actualValue, injectedValue);
				}
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

	@SuppressWarnings("unchecked")
	protected <T extends IModifierPayload> T getPayloadChecked(IXMLDocumentDescriptor descriptor,
			Class<T> expectedType) {
		final Optional<IDocumentModifier> oModifier = descriptor.getModifier();
		checkArgument(oModifier.isPresent());
		final Optional<IModifierPayload> oPayload = oModifier.get().getPayload();
		checkArgument(oPayload.isPresent());
		if (expectedType.isInstance(oPayload.get())) {
			return (T) oPayload.get();
		} else {
			throw logger.throwing(new IllegalArgumentException(
					String.format("payload of document modifier has the wrong type %s, expected %s",
							oPayload.get().getClass().getName(), expectedType.getName())));
		}

	}

	private class DirectAttributeCheckPayload implements IModifierPayload {

		private static final long serialVersionUID = -176115350310411970L;

		String elementSelector;
		String injectedAttribute;
		String injectedValue;

		/**
		 * @param elementSelector
		 * @param injectedAttribute
		 * @param injectedValue
		 */
		public DirectAttributeCheckPayload(String elementSelector, String injectedAttribute, String injectedValue) {
			super();
			this.elementSelector = elementSelector;
			this.injectedAttribute = injectedAttribute;
			this.injectedValue = injectedValue;
		}

		/**
		 * @param elementSelector
		 * @param injectedAttribute
		 */
		public DirectAttributeCheckPayload(String elementSelector, String injectedAttribute) {
			super();
			this.elementSelector = elementSelector;
			this.injectedAttribute = injectedAttribute;
		}

		/**
		 * @return the elementSelector
		 */
		public String getElementSelector() {
			return this.elementSelector;
		}

		/**
		 * @return the injectedAttribute
		 */
		public String getInjectedAttribute() {
			return this.injectedAttribute;
		}

		/**
		 * @return the injectedValue
		 */
		public String getInjectedValue() {
			return this.injectedValue;
		}
	}

}
