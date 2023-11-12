package org.x2vc.analysis;

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

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.VulnerabilityReport;
import org.x2vc.report.VulnerabilityReport.Builder;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.coverage.ICoverageStatistics;
import org.x2vc.stylesheet.coverage.ILineCoverage;
import org.x2vc.utilities.ReportCollectorAppender;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IDocumentAnalyzer}.
 */
@Singleton
public class DocumentAnalyzer implements IDocumentAnalyzer {

	private static final Logger logger = LogManager.getLogger();
	private Set<IAnalyzerRule> rules;
	private ISchemaManager schemaManager;

	/**
	 * Creates a new analyzer instance using the rules provided.
	 *
	 * @param rules
	 * @param schemaManager
	 */
	@Inject
	public DocumentAnalyzer(Set<IAnalyzerRule> rules, ISchemaManager schemaManager) {
		this.rules = rules;
		this.schemaManager = schemaManager;
		logger.info("Analyzer initialized using a set of {} rules", this.rules.size());
	}

	@Override
	public ImmutableSet<String> getRuleIDs() {
		return ImmutableSet.copyOf(this.rules.stream().map(IAnalyzerRule::getRuleID).toList());
	}

	@Override
	public void analyzeDocument(UUID taskID, IHTMLDocumentContainer document,
			Consumer<IDocumentModifier> modifierCollector, Consumer<IVulnerabilityCandidate> vulnerabilityCollector) {
		checkArgument(!document.isFailed());
		logger.traceEntry();
		logger.debug("analyzing document using a set of {} rules", this.rules.size());
		final IXMLDocumentContainer xmlContainer = document.getSource();
		final Optional<IDocumentModifier> modifier = xmlContainer.getDocumentDescriptor().getModifier();
		// if the document was not modified by an analyzer rule, perform a first pass
		if (modifier.isEmpty() || modifier.get().getAnalyzerRuleID().isEmpty()) {
			performFirstPass(document, xmlContainer, modifierCollector);
		} else {
			performFollowUpPass(taskID, document, xmlContainer, modifier.get(), vulnerabilityCollector);
		}
		logger.traceExit();
	}

	/**
	 * @param document
	 * @param descriptor
	 */
	private void performFirstPass(IHTMLDocumentContainer container, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> modifierCollector) {
		logger.traceEntry();
		final Optional<String> doc = container.getDocument();
		checkArgument(doc.isPresent());
		logger.debug("parsing HTML document");
		final Document parsedDocument = Jsoup.parse(doc.get());
		parsedDocument.traverse((node, depth) -> {
			for (final IAnalyzerRule rule : DocumentAnalyzer.this.rules) {
				rule.checkNode(node, xmlContainer, modifier -> {
					logger.debug("rule {} produced modifier {} at depth {}", rule.getRuleID(), modifier, depth);
					modifierCollector.accept(modifier);
				});
			}
		});
		logger.traceExit();
	}

	/**
	 * @param container
	 * @param descriptor
	 * @param iDocumentModifier
	 * @param vulnerabilityCollector
	 */
	private void performFollowUpPass(UUID taskID, IHTMLDocumentContainer container, IXMLDocumentContainer xmlContainer,
			IDocumentModifier modifier, Consumer<IVulnerabilityCandidate> vulnerabilityCollector) {
		logger.traceEntry();
		final IAnalyzerRule rule = filterCorrespondingRule(modifier);

		final Optional<String> doc = container.getDocument();
		checkArgument(doc.isPresent());
		logger.debug("parsing HTML document");
		final Document parsedDocument = Jsoup.parse(doc.get());

		final Set<String> selectors = rule.getElementSelectors(xmlContainer);
		if (selectors.isEmpty()) {
			logger.debug("rule requires checking of entire DOM tree");
			parsedDocument.traverse((node, depth) -> rule.verifyNode(taskID, node, xmlContainer, report -> {
				logger.debug("rule {} produced report {}", rule.getRuleID(), report);
				vulnerabilityCollector.accept(report);
			}));
		} else {
			logger.debug("rule provided set of {} XPath expressions to filter DOM nodes", selectors.size());
			final List<Element> filteredNodes = selectors.stream().<Element>mapMulti((selector, consumer) -> {
				final Elements elements = parsedDocument.selectXpath(selector);
				logger.debug("selector \"{}\" identified {} nodes", selector, elements.size());
				elements.forEach(consumer);
			}).distinct().toList();
			logger.debug("identified {} nodes to check for follow-up pass", filteredNodes.size());
			filteredNodes.forEach(node -> rule.verifyNode(taskID, node, xmlContainer, report -> {
				logger.debug("rule {} produced report {}", rule.getRuleID(), report);
				vulnerabilityCollector.accept(report);
			}));
		}
		logger.traceExit();
	}

	/**
	 * @param modifier
	 * @return
	 * @throws IllegalArgumentException
	 */
	private IAnalyzerRule filterCorrespondingRule(IDocumentModifier modifier) throws IllegalArgumentException {
		logger.traceEntry();
		final Optional<String> analyzerRuleID = modifier.getAnalyzerRuleID();
		checkArgument(analyzerRuleID.isPresent());
		final String ruleID = analyzerRuleID.get();
		return logger.traceExit(filterRuleByID(ruleID));
	}

	/**
	 * @param ruleID
	 * @return
	 * @throws IllegalArgumentException
	 */
	private IAnalyzerRule filterRuleByID(String ruleID) throws IllegalArgumentException {
		logger.traceEntry("for rule ID {}", ruleID);
		// TODO Document Analyzer: move to map of rules
		final List<IAnalyzerRule> filteredRules = this.rules.stream().filter(r -> r.getRuleID().equals(ruleID))
			.toList();
		if (filteredRules.isEmpty()) {
			throw logger.throwing(new IllegalArgumentException(
					String.format("Unable to identify rule %s for follow-up pass", ruleID)));
		} else if (filteredRules.size() > 1) {
			throw logger
				.throwing(new IllegalArgumentException(String.format("Multiple rules match rule ID %s", ruleID)));
		}
		return logger.traceExit(filteredRules.get(0));
	}

	@Override
	public IVulnerabilityReport consolidateResults(URI stylesheetURI, Set<IVulnerabilityCandidate> candidates,
			ICoverageStatistics coverageStatistics, ImmutableList<ILineCoverage> codeCoverage) {
		logger.traceEntry();

		// obtain the schema reference
		final IXMLSchema schema = this.schemaManager.getSchema(stylesheetURI);

		// sort the candidates by rule ID
		final Multimap<String, IVulnerabilityCandidate> candidatesByRuleID = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		candidates.forEach(c -> candidatesByRuleID.put(c.getAnalyzerRuleID(), c));
		logger.debug("consolidating {} candidates for {} rules", candidates.size(), candidatesByRuleID.keySet().size());

		// start building report
		final Builder builder = VulnerabilityReport.builder(stylesheetURI)
			.withCoverageStatistics(coverageStatistics)
			.withCodeCoverage(codeCoverage)
			.addMessages(ReportCollectorAppender
				.removeCollectedMessage(Paths.get(stylesheetURI).getFileName().toString()));

		// process all rules, whether we have vulnerability candidates or not
		for (final String ruleID : getRuleIDs()) {
			try {
				final IAnalyzerRule rule = filterRuleByID(ruleID);
				builder.addSections(rule.consolidateResults(schema, Set.copyOf(candidatesByRuleID.get(ruleID))));
			} catch (Exception e) {
				logger.error("error occurred consolidating results for rule {}, report may be incomplete", ruleID);
				logger.trace("error details", e);
			}
		}

		return logger.traceExit(builder.build());
	}

}
