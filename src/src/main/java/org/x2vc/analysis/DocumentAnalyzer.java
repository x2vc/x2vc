package org.x2vc.analysis;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IDocumentAnalyzer}.
 */
@Singleton
public class DocumentAnalyzer implements IDocumentAnalyzer {

	private static final Logger logger = LogManager.getLogger();
	private Set<IAnalyzerRule> rules;

	/**
	 * Creates a new analyzer instance using the rules provided.
	 *
	 * @param rules
	 */
	@Inject
	public DocumentAnalyzer(Set<IAnalyzerRule> rules) {
		this.rules = rules;
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

}
