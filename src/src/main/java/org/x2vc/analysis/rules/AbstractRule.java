package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.report.*;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IModifierPayload;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.*;

/**
 * Base class for all {@link IAnalyzerRule} implementations that provides some
 * common implementations.
 */
public abstract class AbstractRule implements IAnalyzerRule {

	private static final Logger logger = LogManager.getLogger();

	protected static final String DEFAULT_SECTION = "Default"; //$NON-NLS-1$

	/**
	 * Determines an XPath selector string to make it easier to relocate an element
	 * inside the HTML document later.
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

	/**
	 * Retrieves the {@link IModifierPayload} of an {@link IDocumentModifier} used
	 * to generate a document, checking its type and casting it in the process.
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
			throw logger.throwing(new IllegalArgumentException(
					String.format("payload of document modifier has the wrong type %s, expected %s", //$NON-NLS-1$
							oPayload.get().getClass().getName(), expectedType.getName())));
		}

	}

	@Override
	public List<IVulnerabilityReportSection> consolidateResults(Set<IVulnerabilityCandidate> candidates) {
		logger.traceEntry();

		// TODO Report Output: add missing unit tests for this

		// sort the candidates by section ID
		final Multimap<String, IVulnerabilityCandidate> candidatesBySectionID = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		candidates.forEach(c -> candidatesBySectionID.put(getReportSectionID(c), c));

		final List<IVulnerabilityReportSection> sections = Lists.newArrayList();
		for (final String sectionID : getReportSectionIDs()) {
			final Collection<IVulnerabilityCandidate> sectionCandidates = candidatesBySectionID.get(sectionID);
			final VulnerabilityReportSection.Builder sectionBuilder = VulnerabilityReportSection.builder()
				.withHeading(getSReportectionHeading(sectionID));
			if (sectionCandidates.isEmpty()) {
				// create empty placeholder section
				sectionBuilder.withIntroduction(getReportPlaceholderIntroduction(sectionID));
			} else {
				final List<IVulnerabilityReportIssue> issues = createReportIssues(sectionCandidates);
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
	 * Provides the list of section IDs that this rule is able to provide for the
	 * final report. The base class provides a default section; subclasses may
	 * override to provide more sections.
	 *
	 * @return the list of section IDs that this rule is able to provide for the
	 *         final report
	 */
	protected List<String> getReportSectionIDs() {
		return List.of(DEFAULT_SECTION);
	}

	/**
	 * Provides the section ID that a candidate belongs to. The base class assigns
	 * all candidates to the default section; subclasses may override to fill
	 * different sections.
	 *
	 * @param candidate
	 * @return the section ID that a candidate belongs to
	 */
	protected String getReportSectionID(IVulnerabilityCandidate candidate) {
		return DEFAULT_SECTION;
	}

	/**
	 * Provides the section title. The base class provides a default implementation
	 * to read from externalized strings using the rule ID and the section ID as a
	 * key.
	 *
	 * @param sectionID
	 * @return the section title
	 */
	protected String getSReportectionHeading(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Heading", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides a placeholder introduction for an empty section (that is, a section
	 * without issue reports). The base class provides a default implementation to
	 * read from externalized strings using the rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportPlaceholderIntroduction(String sectionID) {
		return Messages.getString(String.format("AnalyzerRule.%s.%s.PlaceholderIntroduction", //$NON-NLS-1$
				getRuleID().replace(".", "_"), sectionID));
	}

	/**
	 * Provides an introduction for a section. The base class provides a default
	 * implementation to read from externalized strings using the rule ID and the
	 * section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportIntroduction(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Introduction", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides an description of the vulnerability for a section. The base class
	 * provides a default implementation to read from externalized strings using the
	 * rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportDescription(String sectionID) {
		return Messages
			.getString(String.format("AnalyzerRule.%s.%s.Description", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Provides an description of countermeasures for a section. The base class
	 * provides a default implementation to read from externalized strings using the
	 * rule ID and the section ID as a key.
	 *
	 * @param sectionID
	 * @return
	 */
	private String getReportCountermeasures(String sectionID) {
		return Messages.getString(
				String.format("AnalyzerRule.%s.%s.Countermeasures", getRuleID().replace(".", "_"), sectionID)); //$NON-NLS-1$
	}

	/**
	 * Produces the issues contained in a section of the vulnerability report. The
	 * default implementation in the base class will combine all candidates that
	 * affect the same output element and select one example for each input element.
	 *
	 * @param sectionCandidates
	 * @return the issues to be reported from the candidates
	 */
	protected List<IVulnerabilityReportIssue> createReportIssues(
			Collection<IVulnerabilityCandidate> sectionCandidates) {

		final Table<String, UUID, IVulnerabilityCandidate> selectedCandidates = HashBasedTable.create();
		sectionCandidates
			.forEach(c -> selectedCandidates.put(c.getAffectedOutputElement(), c.getAffectingSchemaObject(), c));
		final List<IVulnerabilityReportIssue> result = Lists.newArrayList();
		selectedCandidates.cellSet().forEach(cell -> result.add(VulnerabilityReportIssue
			.builder()
			.withAffectedOutputElement(cell.getRowKey())
			// TODO Report Output: map schema object to human-readable text
			.addAffectingInputElement(cell.getColumnKey().toString())
			.addExample(cell.getValue().getInputSample(), cell.getValue().getOutputSample())
			.build()));
		return result;
	}

}
