package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.VulnerabilityCandidate;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Rule E.1: Check every element that contains the prefix used to generate the
 * values whether it is possible to inject new elements by modifying the input
 * data.
 */
public class DirectElementCheckRule extends AbstractElementRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "E.1";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	DirectElementCheckRule(ISchemaManager schemaManager) {
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
	protected void performCheckOn(Element element, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}", element);

		final String elementName = element.tagName();
		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(elementName);
		if (valueDescriptors.isPresent()) {
			final String parentElementPath = getPathToNode(element.parentNode());
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());
			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();
				// try to replace the entire element with script element
				final AnalyzerRulePayload payload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaElementID())
					.withElementSelector(parentElementPath + "/script")
					.withInjectedValue("script")
					.build();
				requestModification(schema, valueDescriptor, currentValue, "script", payload, collector);
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

			final String elementName = element.tagName();
			final String injectedName = injectedValue.get();
			final String elementPath = getPathToNode(element);
			if (elementName.equals(injectedName)) {
				logger.debug("element \"{}\" injected from input data, follow-up check positive", elementName);
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
				logger.debug("element \"{}\" not found, follow-up check negative", injectedName);
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
