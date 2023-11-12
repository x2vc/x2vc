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

import java.util.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.report.*;
import org.x2vc.schema.structure.*;
import org.x2vc.xml.document.DocumentValueModifier;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IModifierPayload;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.*;

/**
 * Base class for all {@link IAnalyzerRule} implementations that provides some common implementations.
 */
public abstract class AbstractRule implements IAnalyzerRule {

	private static final Logger logger = LogManager.getLogger();

	protected static final String DEFAULT_SECTION = "Default"; //$NON-NLS-1$

	/**
	 * Determines an XPath selector string to make it easier to relocate an element inside the HTML document later.
	 *
	 * @param node the node in question
	 * @return the selection path
	 */
	String getPathToNode(Node node) {
		final Deque<String> pathElements = Lists.newLinkedList();
		Node nextNode = node;
		while (nextNode != null) {
			if (nextNode instanceof Document) {
				// ignore document node
			} else if (nextNode instanceof final Element element) {
				pathElements.addFirst(element.nodeName());
			} else {
				if (!pathElements.isEmpty()) {
					logger.warn("non-element node as intermediate node encountered - check situation!"); //$NON-NLS-1$
				}
			}
			nextNode = nextNode.parent();
		}
		final StringBuilder result = new StringBuilder();
		pathElements.forEach(e -> result.append("/" + e)); //$NON-NLS-1$
		return result.toString();
	}

	@Override
	public Set<String> getElementSelectors(IXMLDocumentContainer xmlContainer) {
		logger.traceEntry();
		// default implementation will check for an IAnalyzerRulePayload and return the
		// value thereof
		final IAnalyzerRulePayload payload = getPayloadChecked(xmlContainer);
		Set<String> result = Collections.emptySet();
		final Optional<String> oSelector = payload.getElementSelector();
		if (oSelector.isPresent()) {
			result = Set.of(oSelector.get());
		}
		return logger.traceExit(result);
	}

	@Override
	public void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IVulnerabilityCandidate> collector) {
		logger.traceEntry();
		final IAnalyzerRulePayload payload = getPayloadChecked(xmlContainer);
		final Optional<String> injectedValue = payload.getInjectedValue();
		final Optional<UUID> schemaElementID = payload.getSchemaElementID();
		verifyNode(taskID, node, xmlContainer, injectedValue, schemaElementID, collector);
		logger.traceExit();
	}

	/**
	 * @param taskID
	 * @param node
	 * @param xmlContainer
	 * @param injectedValue
	 * @param schemaElementID
	 * @param collector
	 */
	protected abstract void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Optional<String> injectedValue, Optional<UUID> schemaElementID,
			Consumer<IVulnerabilityCandidate> collector);

	/**
	 * Retrieves the {@link IModifierPayload} of an {@link IDocumentModifier} used to generate a document, checking its
	 * type and casting it in the process.
	 *
	 * @param <T>
	 * @param xmlContainer
	 * @param expectedType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends IModifierPayload> T getPayloadChecked(IXMLDocumentContainer xmlContainer,
			Class<T> expectedType) {
		final Optional<IDocumentModifier> oModifier = xmlContainer.getDocumentDescriptor().getModifier();
		checkArgument(oModifier.isPresent());
		final Optional<IModifierPayload> oPayload = oModifier.get().getPayload();
		checkArgument(oPayload.isPresent());
		if (expectedType.isInstance(oPayload.get())) {
			return (T) oPayload.get();
		} else {
			final String actualTypeName = oPayload.get().getClass().getName();
			final String expectedTypeName = expectedType.getName();
			throw logger.throwing(new IllegalArgumentException(
					String.format("payload of document modifier has the wrong type %s, expected %s", //$NON-NLS-1$
							actualTypeName, expectedTypeName)));
		}
	}

	/**
	 * Retrieves the {@link IAnalyzerRulePayload} of an {@link IDocumentModifier} used to generate a document, checking
	 * its type and casting it in the process.
	 *
	 * @param xmlContainer
	 * @return
	 */
	protected IAnalyzerRulePayload getPayloadChecked(IXMLDocumentContainer xmlContainer) {
		final Optional<IDocumentModifier> oModifier = xmlContainer.getDocumentDescriptor().getModifier();
		checkArgument(oModifier.isPresent());
		final Optional<IModifierPayload> oPayload = oModifier.get().getPayload();
		checkArgument(oPayload.isPresent());
		final IModifierPayload payload = oPayload.get();
		if (payload instanceof final IAnalyzerRulePayload arPayload) {
			return arPayload;
		} else {
			final String actualTypeName = oPayload.get().getClass().getName();
			throw logger.throwing(new IllegalArgumentException(
					String.format("payload of document modifier has the wrong type %s, expected IAnalyzerRulePayload", //$NON-NLS-1$
							actualTypeName)));
		}
	}

	/**
	 * Retrieves the {@link IAnalyzerRulePayload} of an {@link IDocumentModifier} used to generate a document, checking
	 * its type and casting it in the process. This will return an empty object if no payload is present or the wrong
	 * type was used.
	 *
	 * @param xmlContainer
	 * @return
	 */
	protected Optional<IAnalyzerRulePayload> getPayload(IXMLDocumentContainer xmlContainer) {
		final Optional<IDocumentModifier> oModifier = xmlContainer.getDocumentDescriptor().getModifier();
		checkArgument(oModifier.isPresent());
		final Optional<IModifierPayload> oPayload = oModifier.get().getPayload();
		checkArgument(oPayload.isPresent());
		final IModifierPayload payload = oPayload.get();
		if (payload instanceof final IAnalyzerRulePayload arPayload) {
			return Optional.of(arPayload);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public List<IVulnerabilityReportSection> consolidateResults(IXMLSchema schema,
			Set<IVulnerabilityCandidate> candidates) {
		logger.traceEntry();

		// TODO #35 Report Output: add missing unit tests for this

		// sort the candidates by section ID
		final Multimap<String, IVulnerabilityCandidate> candidatesBySectionID = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		candidates.forEach(c -> candidatesBySectionID.put(getReportSectionID(c), c));
		logger.debug("sorted {} candidates into {} sections", candidates.size(), candidatesBySectionID.keySet().size());

		final List<IVulnerabilityReportSection> sections = Lists.newArrayList();
		for (final String sectionID : getReportSectionIDs()) {
			logger.debug("adding section ID {} to report", sectionID);
			final Collection<IVulnerabilityCandidate> sectionCandidates = candidatesBySectionID.get(sectionID);
			final VulnerabilityReportSection.Builder sectionBuilder = VulnerabilityReportSection.builder()
				.withRuleID(getRuleID())
				.withHeading(getReportSectionHeading(sectionID))
				.withShortHeading(getReportSectionShortHeading(sectionID));
			if (sectionCandidates.isEmpty()) {
				// create empty placeholder section
				logger.debug("section ID {} is empty", sectionID);
				sectionBuilder.withIntroduction(getReportPlaceholderIntroduction(sectionID));
			} else {
				logger.debug("section ID {} contains {} candidates", sectionID, sectionCandidates.size());
				final List<IVulnerabilityReportIssue> issues = createReportIssues(schema, sectionCandidates);
				sectionBuilder
					.withIntroduction(getReportIntroduction(sectionID))
					.withDescription(getReportDescription(sectionID))
					.withCountermeasures(getReportCountermeasures(sectionID))
					.addIssues(issues);
			}
			sections.add(sectionBuilder.build());
		}
		return logger.traceExit(sections);
	}

	/**
	 * Provides the list of section IDs that this rule is able to provide for the final report. The base class provides
	 * a default section; subclasses may override to provide more sections.
	 *
	 * @return the list of section IDs that this rule is able to provide for the final report
	 */
	protected List<String> getReportSectionIDs() {
		return List.of(DEFAULT_SECTION);
	}

	/**
	 * Provides the section ID that a candidate belongs to. The base class assigns all candidates to the default
	 * section; subclasses may override to fill different sections.
	 *
	 * @param candidate
	 * @return the section ID that a candidate belongs to
	 */
	@SuppressWarnings("java:S1172") // parameter is provisional for overrides
	protected String getReportSectionID(IVulnerabilityCandidate candidate) {
		return DEFAULT_SECTION;
	}

	/**
	 * Provides the section title. The base class provides a default implementation to read from externalized strings
	 * using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return the section title
	 */
	protected String getReportSectionHeading(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Heading", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides the shortened section title. The base class provides a default implementation to read from externalized
	 * strings using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return the section title
	 */
	protected String getReportSectionShortHeading(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.ShortHeading", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides a placeholder introduction for an empty section (that is, a section without issue reports). The base
	 * class provides a default implementation to read from externalized strings using the rule ID and the section ID as
	 * a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportPlaceholderIntroduction(String sectionID) {
		return Messages.getString(String.format("AnalyzerRule.%s.%s.PlaceholderIntroduction", //$NON-NLS-1$
				getRuleID().replace(".", "_"), sectionID));
	}

	/**
	 * Provides an introduction for a section. The base class provides a default implementation to read from
	 * externalized strings using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportIntroduction(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Introduction", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides an description of the vulnerability for a section. The base class provides a default implementation to
	 * read from externalized strings using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportDescription(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Description", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides an description of countermeasures for a section. The base class provides a default implementation to
	 * read from externalized strings using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportCountermeasures(String sectionID) {
		return Messages.getString(
				String.format("AnalyzerRule.%s.%s.Countermeasures", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Produces the issues contained in a section of the vulnerability report. The default implementation in the base
	 * class will combine all candidates that affect the same output element and select one example for each input
	 * element.
	 *
	 * @param sectionCandidates
	 * @return the issues to be reported from the candidates
	 */
	protected List<IVulnerabilityReportIssue> createReportIssues(IXMLSchema schema,
			Collection<IVulnerabilityCandidate> sectionCandidates) {

		final Table<String, UUID, IVulnerabilityCandidate> selectedCandidates = HashBasedTable.create();
		sectionCandidates
			.forEach(c -> selectedCandidates.put(c.getAffectedOutputElement(), c.getAffectingSchemaObject(), c));
		final List<IVulnerabilityReportIssue> result = Lists.newArrayList();
		selectedCandidates.cellSet().forEach(cell -> result.add(VulnerabilityReportIssue.builder()
			.withAffectedOutputElement(cell.getRowKey())
			.addAffectingInputElements(schema.getObjectPaths(cell.getColumnKey()))
			.addExample(cell.getValue().getInputSample(), cell.getValue().getOutputSample())
			.build()));
		return result;
	}

	/**
	 * Issues a modification request if the requested value is valid for the attribute in question.
	 *
	 * @param schema
	 * @param valueDescriptor
	 * @param originalValue
	 * @param replacementValue
	 * @param payload
	 * @param collector
	 */
	protected void requestModification(IXMLSchema schema, IValueDescriptor valueDescriptor, String originalValue,
			String replacementValue, IModifierPayload payload,
			Consumer<IDocumentModifier> collector) {
		// check whether the requested value is valid and the input field is
		logger.traceEntry();
		final ISchemaObject schemaObject = schema.getObjectByID(valueDescriptor.getSchemaObjectID());
		if (schemaObject instanceof final IAttribute attribute) {
			requestAttributeModification(attribute, valueDescriptor, originalValue, replacementValue,
					payload, collector);
		} else if (schemaObject instanceof final IElementType element) {
			requestElementModification(element, valueDescriptor, originalValue, replacementValue,
					payload, collector);
		} else if (schemaObject instanceof IExtensionFunction) {
			DocumentValueModifier.builder(valueDescriptor)
				.withAnalyzerRuleID(getRuleID())
				.withOriginalValue(originalValue)
				.withReplacementValue(replacementValue)
				.withPayload(payload)
				.build()
				.sendTo(collector);
		} else if (schemaObject instanceof IStylesheetParameter) {
			DocumentValueModifier.builder(valueDescriptor)
				.withAnalyzerRuleID(getRuleID())
				.withOriginalValue(originalValue)
				.withReplacementValue(replacementValue)
				.withPayload(payload)
				.build()
				.sendTo(collector);
		} else {
			throw logger
				.throwing(new IllegalArgumentException(
						"Modification requests are not possible for this schema object type"));
		}
		logger.traceExit();
	}

	/**
	 * @param attribute
	 * @param valueDescriptor
	 * @param originalValue
	 * @param replacementValue
	 * @param payload
	 * @param collector
	 */
	protected void requestAttributeModification(final IAttribute attribute, IValueDescriptor valueDescriptor,
			String originalValue, String replacementValue, IModifierPayload payload,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		if (attribute.isUserModifiable()) {
			if (attribute.getDataType() == XMLDataType.STRING) {
				final Integer maxLength = attribute.getMaxLength().orElse(Integer.MAX_VALUE);
				if (replacementValue.length() <= maxLength) {
					DocumentValueModifier.builder(valueDescriptor)
						.withAnalyzerRuleID(getRuleID())
						.withOriginalValue(originalValue)
						.withReplacementValue(replacementValue)
						.withPayload(payload)
						.build()
						.sendTo(collector);
				} else {
					logger.debug("requested value \"{}\" exceeds maximum length and will be disregarded",
							replacementValue);
				}
			} else {
				logger.warn("modification of non-string attributes is not yet implemented");
			}
		} else {
			logger.debug("attribute {} is not user-modifiable, request will be disregarded",
					attribute.getID());
		}
		logger.traceExit();
	}

	/**
	 * @param element
	 * @param valueDescriptor
	 * @param originalValue
	 * @param replacementValue
	 * @param payload
	 * @param collector
	 */
	protected void requestElementModification(final IElementType element, IValueDescriptor valueDescriptor,
			String originalValue, String replacementValue, IModifierPayload payload,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		if (element.isUserModifiable().orElse(true)) {
			switch (element.getContentType()) {
			case DATA:
				requestDataElementModification(element, valueDescriptor, originalValue, replacementValue, payload,
						collector);
				break;
			case MIXED:
				DocumentValueModifier.builder(valueDescriptor)
					.withAnalyzerRuleID(getRuleID())
					.withOriginalValue(originalValue)
					.withReplacementValue(replacementValue)
					.withPayload(payload)
					.build()
					.sendTo(collector);
				break;
			default:
				final String message = String.format("Modification requests are not implemented for element type %s",
						element.getContentType());
				throw logger.throwing(new IllegalArgumentException(message));
			}
		} else {
			logger.debug("element {} is not user-modifiable, request will be disregarded",
					element.getID());
		}
		logger.traceExit();
	}

	/**
	 * @param element
	 * @param valueDescriptor
	 * @param originalValue
	 * @param replacementValue
	 * @param payload
	 * @param collector
	 */
	protected void requestDataElementModification(final IElementType element, IValueDescriptor valueDescriptor,
			String originalValue, String replacementValue, IModifierPayload payload,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		switch (element.getDataType()) {
		case BOOLEAN:
			if (replacementValue.equals("true") || replacementValue.equals("false")) {
				DocumentValueModifier.builder(valueDescriptor)
					.withAnalyzerRuleID(getRuleID())
					.withOriginalValue(originalValue)
					.withReplacementValue(replacementValue)
					.withPayload(payload)
					.build()
					.sendTo(collector);
			}
			break;
		case INTEGER:
			try {
				final int intValue = Integer.parseInt(replacementValue);
				final Integer minValue = element.getMinValue().orElse(Integer.MIN_VALUE);
				final Integer maxValue = element.getMaxValue().orElse(Integer.MAX_VALUE);
				if ((intValue >= minValue) && (intValue <= maxValue)) {
					DocumentValueModifier.builder(valueDescriptor)
						.withAnalyzerRuleID(getRuleID())
						.withOriginalValue(originalValue)
						.withReplacementValue(replacementValue)
						.withPayload(payload)
						.build()
						.sendTo(collector);
				}
			} catch (final NumberFormatException e) {
				logger.debug("requested value {} is not a valid integer and will be disregarded", replacementValue);
			}
			break;
		case STRING:
			final Integer maxLength = element.getMaxLength().orElse(Integer.MAX_VALUE);
			if (replacementValue.length() <= maxLength) {
				DocumentValueModifier.builder(valueDescriptor)
					.withAnalyzerRuleID(getRuleID())
					.withOriginalValue(originalValue)
					.withReplacementValue(replacementValue)
					.withPayload(payload)
					.build()
					.sendTo(collector);
			} else {
				logger.debug("requested value \"{}\" is too long ({} > {}) and will be disregarded", replacementValue,
						replacementValue.length(), maxLength);
			}
			break;
		default:
			final String message = String.format("Modification requests are not implemented for data type %s",
					element.getDataType());
			throw logger.throwing(new IllegalArgumentException(message));
		}
		logger.traceExit();

	}

}
