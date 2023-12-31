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
 * Rule U.1: Check whether the URL attributes of various tags can be manipulated via the input data.
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
					.withSchemaElementID(valueDescriptor.getSchemaObjectID())
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
			if (actualValue.contains(checkValue)) {
				logger.debug(
						"attribute \"{}\" contains injected content \"{}\" from input data, follow-up check positive",
						attributeName, checkValue);
				VulnerabilityCandidate.builder(RULE_ID, taskID)
					.withAffectingSchemaObject(schemaElementID.get())
					.withAffectedOutputElement(getPathToNode(node))
					.withInputSample(xmlContainer.getDocument())
					.withOutputSample(node.toString())
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
