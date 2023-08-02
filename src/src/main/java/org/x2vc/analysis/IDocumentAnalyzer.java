package org.x2vc.analysis;

import java.util.function.Consumer;

import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

/**
 * This component processes the HTML document and examines each context that
 * offers a potential for XSS injection.
 *
 * The contents are checked with the help of the {@link IXMLDocumentDescriptor}.
 * If the XML data descriptor points to values injected by the XSS vulnerability
 * check in a previous release, these are checked first. If the injected
 * contents are found in the HTML document without appropriate content
 * sanitization, a vulnerability is reported.
 *
 * If a critical context contains data that can be influenced by user input, a
 * set of possible XSS attack values are generated in the form of input data
 * requests.
 */
public interface IDocumentAnalyzer {

	/**
	 * Analyze a HTML document and check for potential XSS vulnerabilities.
	 *
	 * @param document
	 * @param modifierCollector
	 */
	void analyzeDocument(IHTMLDocumentContainer document, Consumer<IRuleDataModifier> modifierCollector);

	// TODO XSS Analyzer: determine suitable output form

}
