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
 * Rule J.1: Check every script block for possible code injection
 */
public class JavascriptBlockCheckRule extends AbstractElementRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "J.1";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	JavascriptBlockCheckRule(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public String getRuleID() {
		return RULE_ID;
	}

	@Override
	protected boolean isApplicableTo(Element element, IXMLDocumentContainer xmlContainer) {
		// rule only applies to script elements
		return element.tagName().equals("script");
	}

	@Override
	protected void performCheckOn(Element element, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}", element);
		final String scriptContents = element.html();
		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(scriptContents);
		if (valueDescriptors.isPresent()) {
			final String elementPath = getPathToNode(element);
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());
			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();
				// try to replace the entire script contents with some dummy value
				final AnalyzerRulePayload payload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaElementID())
					.withElementSelector(elementPath)
					.withInjectedValue("XSS-J.1")
					.build();
				requestModification(schema, valueDescriptor, currentValue, "';alert('XSS-J.1');var bar='", payload,
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
			checkArgument(schemaElementID.isPresent());

			final String scriptContents = element.html();
			final String injectedText = injectedValue.get();
			if (scriptContents.contains(injectedText)) {
				logger.debug("script element contains injected text from input data, follow-up check positive");
				new VulnerabilityCandidate.Builder(RULE_ID, taskID)
					.withAffectingSchemaObject(schemaElementID.get())
					.withAffectedOutputElement(getPathToNode(element))
					.withInputSample(xmlContainer.getDocument())
					.withOutputSample(node.toString())
					.build()
					.sendTo(collector);

			} else {
				logger.debug("script element does not contain injected text, follow-up check negative");
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
