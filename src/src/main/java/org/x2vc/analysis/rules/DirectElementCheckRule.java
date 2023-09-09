package org.x2vc.analysis.rules;

import java.util.Optional;
import java.util.Set;
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
				requestModification(schema, valueDescriptor, currentValue, "script", new DirectElementCheckPayload(
						valueDescriptor.getSchemaElementID(), parentElementPath + "/script",
						"script"), collector);
			}
		}
		logger.traceExit();
	}

	@Override
	public Set<String> getElementSelectors(IXMLDocumentContainer xmlContainer) {
		logger.traceEntry();
		final IDirectElementCheckPayload payload = getPayloadChecked(xmlContainer,
				IDirectElementCheckPayload.class);
		return logger.traceExit(Set.of(payload.getElementSelector()));
	}

	@Override
	public void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IVulnerabilityCandidate> collector) {
		logger.traceEntry();
		final IDirectElementCheckPayload payload = getPayloadChecked(xmlContainer,
				IDirectElementCheckPayload.class);
		if (node instanceof final Element element) {
			final String elementName = element.tagName();
			final String injectedName = payload.getInjectedElement();
			if (elementName.equals(injectedName)) {
				logger.debug("element \"{}\" injected from input data, follow-up check positive", elementName);
				// TODO Report Output: provide better input sample (formatting, highlighting?)
				final String inputSample = xmlContainer.getDocument();

				// the output sample can be derived from the node
				final String outputSample = node.toString();

				collector.accept(new VulnerabilityCandidate(RULE_ID, taskID,
						payload.getSchemaElementID(), payload.getElementSelector(), inputSample,
						outputSample));
			} else {
				logger.debug("element \"{}\" not found, follow-up check negative", injectedName);
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
