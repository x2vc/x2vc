package org.x2vc.analysis;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

import com.google.inject.Inject;

/**
 * Standard implementation of {@link IDocumentAnalyzer}.
 */
public class DocumentAnalyzer implements IDocumentAnalyzer {

	private static final Logger logger = LogManager.getLogger();
	private Set<IAnalyzerRule> rules;

	/**
	 * Creates a new analyzer instace using the rules provided.
	 *
	 * @param rules
	 */
	@Inject
	public DocumentAnalyzer(Set<IAnalyzerRule> rules) {
		this.rules = rules;
		logger.info("Analyzer initialized using a set of {} rules", this.rules.size());
	}

	@Override
	public void analyzeDocument(IHTMLDocumentContainer document, Consumer<IDocumentModifier> modifierCollector) {
		checkArgument(!document.isFailed());
		logger.traceEntry();
		final IXMLDocumentDescriptor descriptor = document.getSource().getDocumentDescriptor();
		if (!descriptor.isMutated()) {
			performFirstPass(document, descriptor, modifierCollector);
		} else {
			// TODO XSS Analyzer: implement follow-up pass
		}
		logger.traceExit();
	}

	/**
	 * @param document
	 * @param descriptor
	 */
	private void performFirstPass(IHTMLDocumentContainer container, IXMLDocumentDescriptor descriptor,
			Consumer<IDocumentModifier> modifierCollector) {
		logger.traceEntry();
		final Optional<String> doc = container.getDocument();
		checkArgument(doc.isPresent());
		logger.debug("parsing HTML document");
		final Document parsedDocument = Jsoup.parse(doc.get());
		parsedDocument.traverse((node, depth) -> {
			for (final IAnalyzerRule rule : DocumentAnalyzer.this.rules) {
				rule.checkNode(node, descriptor, modifier -> {
					logger.debug("rule {} produced modifier {} at depth {}", rule.getRuleID(), modifier, depth);
					modifierCollector.accept(modifier);
				});
			}
		});
		logger.traceExit();
	}

}
