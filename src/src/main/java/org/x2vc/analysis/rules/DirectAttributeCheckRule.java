package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.UUID;
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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Rule A.1: Check every attribute that contains the prefix used to generate the
 * values whether it is possible to inject new attributes by modifying the input
 * data.
 */
public class DirectAttributeCheckRule extends AbstractAttributeRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "A.1";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	DirectAttributeCheckRule(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public String getRuleID() {
		return RULE_ID;
	}

	@Override
	protected boolean isApplicableTo(Element element, IXMLDocumentContainer xmlContainer) {
		// check every element
		return true;
	}

	@Override
	protected boolean isApplicableTo(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer) {
		// check every attribute of element
		return true;
	}

	@Override
	protected void performCheckOn(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}, attribute {}", element, attribute);

		final String attributeName = attribute.getKey();
		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(attributeName);
		if (valueDescriptors.isPresent()) {

			final String elementPath = getPathToNode(element);
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());

			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();

				// try to replace the entire attribute with style attribute
				final AnalyzerRulePayload stylePayload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaElementID())
					.withInjectedValue("style")
					.withElementSelector(elementPath)
					.build();
				requestModification(schema, valueDescriptor, currentValue, "style", stylePayload, collector);

				// try to replace the entire Attribute with a Javascript event handler
				final AnalyzerRulePayload handlerPayload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaElementID())
					.withInjectedValue("onerror")
					.withElementSelector(elementPath)
					.build();
				requestModification(schema, valueDescriptor, currentValue, "onerror", handlerPayload, collector);
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
			checkArgument(schemaElementID.isPresent());

			final String attributeName = injectedValue.get();
			final String actualValue = element.attr(attributeName);
			if (Strings.isNullOrEmpty(actualValue)) {
				logger.debug("attribute \"{}\" not found, follow-up check negative", attributeName);
			} else {
				logger.debug("attribute \"{}\" injected from input data, follow-up check positive", attributeName);
				new VulnerabilityCandidate.Builder(RULE_ID, taskID)
					.withAffectingSchemaObject(schemaElementID.get())
					.withAffectedOutputElement(getPathToNode(node))
					.withInputSample(xmlContainer.getDocument())
					.withOutputSample(node.toString())
					.build()
					.sendTo(collector);
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
