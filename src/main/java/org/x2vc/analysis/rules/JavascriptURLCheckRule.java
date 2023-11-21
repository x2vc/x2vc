/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Rule J.2: Check whether the src attribute of a script tag can be manipulated via the input data.
 */
public class JavascriptURLCheckRule extends AbstractAttributeRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "J.2";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	JavascriptURLCheckRule(ISchemaManager schemaManager) {
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
		return element.tagName().equalsIgnoreCase("script");
	}

	@Override
	protected boolean isApplicableTo(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer) {
		// rule only applies to src attributes
		return attribute.getKey().equalsIgnoreCase("src");
	}

	@Override
	protected void performCheckOn(Element element, Attribute attribute, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry("element {}, attribute {}", element, attribute);

		final String attributeValue = attribute.getValue();
		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(attributeValue);
		if (valueDescriptors.isPresent()) {

			final String elementPath = getPathToNode(element);
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());

			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();

				// try to inject a source path into the attribute
				final AnalyzerRulePayload stylePayload = AnalyzerRulePayload.builder()
					.withSchemaElementID(valueDescriptor.getSchemaObjectID())
					.withInjectedValue("XSS-J.2")
					.withElementSelector(elementPath)
					.build();
				requestModification(schema, valueDescriptor, currentValue, "http://evil.attacker.com/XSS-J.2.js",
						stylePayload, collector);
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

			final String injectedData = injectedValue.get();
			final String actualValue = element.attr("src");
			if (actualValue.contains(injectedData)) {
				logger.debug(
						"attribute \"src\" contains injected data \"{}\" from input data, follow-up check positive",
						injectedData);
				VulnerabilityCandidate.builder(RULE_ID, taskID)
					.withAffectingSchemaObject(schemaElementID.get())
					.withAffectedOutputElement(getPathToNode(node))
					.withInputSample(xmlContainer.getDocument())
					.withOutputSample(node.toString())
					.build()
					.sendTo(collector);
			} else {
				logger.debug("attribute \"src\" does not contain injected data, follow-up check negative");
			}
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
