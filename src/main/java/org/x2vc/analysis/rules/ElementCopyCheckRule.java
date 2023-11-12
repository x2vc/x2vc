package org.x2vc.analysis.rules;

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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.VulnerabilityCandidate;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.ISchemaObject;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Rule E.2: Check the text content of every element that contains the prefix used to generate the values whether it is
 * possible to inject arbitrary code. This checks for xsl:copy and xsl:copy-of vulnerabilities.
 */
public class ElementCopyCheckRule extends AbstractTextRule {

	/**
	 * @see #getRuleID()
	 */
	public static final String RULE_ID = "E.2";

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	ElementCopyCheckRule(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public String getRuleID() {
		return RULE_ID;
	}

	@Override
	protected boolean isApplicableTo(TextNode textNode, IXMLDocumentContainer xmlContainer) {
		logger.traceEntry();
		// this rule is applicable for text nodes that contain the prefix only
		final String prefix = xmlContainer.getDocumentDescriptor().getValuePrefix();
		final boolean result = textNode.text().contains(prefix);
		return logger.traceExit(result);
	}

	@Override
	protected void performCheckOn(TextNode textNode, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();

		final String textContent = textNode.text();
		final Optional<ImmutableSet<IValueDescriptor>> valueDescriptors = xmlContainer.getDocumentDescriptor()
			.getValueDescriptors(textContent);
		if (valueDescriptors.isPresent()) {
			// we can't address the text node directly - we need to emit the path to the
			// parent node
			final String parentElementPath = getPathToNode(textNode.parentNode());
			logger.debug("checking text of element {}", parentElementPath);
			final IXMLSchema schema = this.schemaManager.getSchema(xmlContainer.getStylesheeURI());
			for (final IValueDescriptor valueDescriptor : valueDescriptors.get()) {
				final String currentValue = valueDescriptor.getValue();
				final UUID schemaElementID = valueDescriptor.getSchemaObjectID();
				// The xsl:copy/copy-of vulnerability only applies to mixed output elements
				final ISchemaObject schemaObject = schema.getObjectByID(schemaElementID);
				if ((schemaObject instanceof final IElementType schemaElement) && schemaElement.hasMixedContent()) {
					// try to replace the entire element with script element
					logger.debug("attempt to replace \"{}\" with \"<script></script>\" for schema element {}",
							currentValue,
							schemaElementID);
					final AnalyzerRulePayload payload = AnalyzerRulePayload.builder()
						.withSchemaElementID(schemaElementID)
						.withElementSelector(parentElementPath)
						.withElementName("script")
						.withInjectedValue("XSS-E.2")
						.build();
					requestModification(schema, valueDescriptor, currentValue, "<script>alert('XSS-E.2!')</script>",
							payload,
							collector);
				}
			}
		}
		logger.traceExit();
	}

	@Override
	protected void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Optional<String> injectedValue, Optional<UUID> schemaElementID,
			Consumer<IVulnerabilityCandidate> collector) {
		logger.traceEntry();
		checkArgument(injectedValue.isPresent());
		final String injectedContent = injectedValue.get();

		checkArgument(schemaElementID.isPresent());

		final Optional<IAnalyzerRulePayload> oPayload = getPayload(xmlContainer);
		checkArgument(oPayload.isPresent());
		final Optional<String> oElementName = oPayload.get().getElementName();
		checkArgument(oElementName.isPresent());
		final String elementName = oElementName.get();

		// As per the "can't address the text node directly" comment above, the node to
		// be examined here will actually be an Element node. We have to examine its
		// contents - which is fine, because if we succeeded to inject a new Element, it
		// wouldn't have turned up as part of the text node anyway.
		if (node instanceof final Element element) {
			final String parentPath = getPathToNode(element);
			logger.debug("follow-up check on {} to check for injection of \"{}\" tag", parentPath, elementName);
			final Elements possiblyInjectedElements = element.getElementsByTag(elementName);
			possiblyInjectedElements.forEach(injectedElement -> {
				final String actualContent = injectedElement.toString();
				if (actualContent.contains(injectedContent)) {
					logger.debug(
							"tag \"{}\" injected from input data contains search string \"{}\", follow-up check positive",
							injectedElement.tagName(), injectedContent);
					VulnerabilityCandidate.builder(RULE_ID, taskID)
						.withAffectingSchemaObject(schemaElementID.get())
						.withAffectedOutputElement(getPathToNode(injectedElement.parentNode()))
						.withInputSample(xmlContainer.getDocument())
						.withOutputSample(element.toString())
						.build()
						.sendTo(collector);
				}
			});
		} else {
			logger.warn("follow-up check called for non-element node");
		}
		logger.traceExit();
	}

}
