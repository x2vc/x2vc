package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.VulnerabilityCandidate;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.value.IValueDescriptor;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Rule U.1: Check whether the URL attributes of various tags can be manipulated
 * via the input data.
 */
public class GeneralURLCheckRule extends AbstractAttributeRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "U.1";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private Set<String> urlAttributes;

	/**
	 * @param schemaManager
	 */
	@Inject
	GeneralURLCheckRule(ISchemaManager schemaManager,
			@TypesafeConfig("x2vc.analysis.attributes.url") List<String> urlAttributes) {
		super();
		this.schemaManager = schemaManager;
		this.urlAttributes = new HashSet<>(urlAttributes);
	}

	@Override
	public String getRuleID() {
		return RULE_ID;
	}

	@Override
	protected boolean isApplicableTo(Element element, IXMLDocumentContainer xmlContainer) {
		// check every element except for
		// - script --> covered by JavascriptURLCheckRule
		// - link rel="stylesheet" --> covered by CSSURLCheckRule
		final var tagName = element.tagName();
		if (tagName.equalsIgnoreCase("script")) {
			return false;
		}
		if (tagName.equalsIgnoreCase("link")) {
			return !element.attr("rel").equalsIgnoreCase("stylesheet");
		}
		return true;
	}

	@Override
	protected boolean isApplicableTo(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer) {
		// check the configured attributes
		return this.urlAttributes.contains(attribute.getKey());
	}

	@Override
	protected void performCheckOn(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}, attribute {}", element, attribute);

		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(attribute.getValue());
		if (valueDescriptors.isPresent()) {

			final String elementPath = getPathToNode(element);
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());

			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();

				// try to replace the entire attribute with an attempted code injection
				final AnalyzerRulePayload stylePayload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaElementID())
					.withAttributeName(attribute.getKey())
					.withInjectedValue("XSS-U.1")
					.withElementSelector(elementPath)
					.build();
				requestModification(schema, valueDescriptor, currentValue, "http://evil.attacker.com/XSS-U.1",
						stylePayload,
						collector);
			}
		}
		logger.traceExit();
	}

	@Override
	protected void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Optional<String> injectedValue, Optional<UUID> schemaElementID,
			Consumer<IVulnerabilityCandidate> collector) {
		logger.traceEntry();

		if (node instanceof final Element element) {
			checkArgument(injectedValue.isPresent());
			final String checkValue = injectedValue.get();

			checkArgument(schemaElementID.isPresent());
			final Optional<IAnalyzerRulePayload> oPayload = getPayload(xmlContainer);
			checkArgument(oPayload.isPresent());
			final Optional<String> oAttributeName = oPayload.get().getAttributeName();
			checkArgument(oAttributeName.isPresent());
			final String attributeName = oAttributeName.get();

			final String actualValue = element.attr(attributeName);
			final String elementPath = getPathToNode(node);
			if (actualValue.contains(checkValue)) {
				logger.debug(
						"attribute \"{}\" contains injected content \"{}\" from input data, follow-up check positive",
						attributeName, checkValue);

				// TODO Report Output: provide better input sample (formatting, highlighting?)
				final String inputSample = xmlContainer.getDocument();

				// the output sample can be derived from the node
				final String outputSample = node.toString();

				new VulnerabilityCandidate.Builder(RULE_ID, taskID)
					.withAffectingSchemaObject(schemaElementID.get())
					.withAffectedOutputElement(elementPath)
					.withInputSample(inputSample)
					.withOutputSample(outputSample)
					.build()
					.sendTo(collector);
			} else {
				logger.debug(
						"attribute \"{}\" does not contain injected content \"{}\" from input data, follow-up check negative",
						attributeName, checkValue);
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
