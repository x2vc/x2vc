package org.x2vc.analysis.rules;

import java.util.Optional;
import java.util.Set;
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
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLDatatype;
import org.x2vc.xml.document.DocumentValueModifier;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IModifierPayload;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

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

	static final String MISSING_INPUT_SAMPLE = "<MISSING>";
	static final String CHECK_ID_ATTRIB_STYLE = "A.CSS";
	static final String CHECK_ID_ATTRIB_ONERROR = "A.JSH";

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
				requestModification(schema, valueDescriptor, currentValue, "style", new DirectAttributeCheckPayload(
						CHECK_ID_ATTRIB_STYLE, valueDescriptor.getSchemaElementID(), elementPath, "style"), collector);

				// try to replace the entire Attribute with a Javascript event handler
				requestModification(schema, valueDescriptor, currentValue, "onerror",
						new DirectAttributeCheckPayload(CHECK_ID_ATTRIB_ONERROR, valueDescriptor.getSchemaElementID(),
								elementPath, "onerror"),
						collector);
			}
		}
		logger.traceExit();
	}

	/**
	 * Issues a modification request if the requested value is valid for the
	 * attribute in question.
	 *
	 * @param schema
	 * @param valueDescriptor
	 * @param originalValue
	 * @param replacementValue
	 * @param payload
	 * @param collector
	 */
	private void requestModification(IXMLSchema schema, IValueDescriptor valueDescriptor, String originalValue,
			String replacementValue, IModifierPayload payload, Consumer<IDocumentModifier> collector) {
		// check whether the requested value is valid (the attribute has to be a string
		// and the max length may not be exceeded) - otherwise we can just skip the
		// request
		final IXMLAttribute attribute = schema.getObjectByID(valueDescriptor.getSchemaElementID()).asAttribute();
		if (attribute.getDatatype() == XMLDatatype.STRING) {
			final Integer maxLength = attribute.getMaxLength().orElse(Integer.MAX_VALUE);
			if (replacementValue.length() <= maxLength) {
				new DocumentValueModifier.Builder(valueDescriptor).withAnalyzerRuleID(RULE_ID)
					.withOriginalValue(originalValue).withReplacementValue(replacementValue).withPayload(payload)
					.sendTo(collector);
			}
		}
	}

	@Override
	public Set<String> getElementSelectors(IXMLDocumentContainer xmlContainer) {
		logger.traceEntry();
		final IDirectAttributeCheckPayload payload = getPayloadChecked(xmlContainer,
				IDirectAttributeCheckPayload.class);
		return logger.traceExit(Set.of(payload.getElementSelector()));
	}

	@Override
	public void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IVulnerabilityCandidate> collector) {
		logger.traceEntry();
		final IDirectAttributeCheckPayload payload = getPayloadChecked(xmlContainer,
				IDirectAttributeCheckPayload.class);
		if (node instanceof final Element element) {
			final String attributeName = payload.getInjectedAttribute();
			final String actualValue = element.attr(attributeName);
			if (Strings.isNullOrEmpty(actualValue)) {
				logger.debug("attribute \"{}\" not found, follow-up check negative", attributeName);
			} else {
				logger.debug("attribute \"{}\" injected from input data, follow-up check positive", attributeName);
				// TODO Report Output: provide input sample
				collector.accept(new VulnerabilityCandidate(RULE_ID, payload.getCheckID(), taskID,
						payload.getSchemaElementID(), payload.getElementSelector(), MISSING_INPUT_SAMPLE,
						node.toString()));
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
