package org.x2vc.stylesheet.coverage;

import java.net.URI;

import org.x2vc.processor.IHTMLDocumentContainer;

import com.google.common.collect.ImmutableList;

/**
 * This component uses the trace output generated by the XSLT processor to determine which parts of the stylesheet were
 * traversed while producing an output element.
 */
public interface ICoverageTraceAnalyzer {

	/**
	 * Analyze a HTML document and add the coverage information to the analyzer data.
	 *
	 * @param taskID        the ID of the task being executed
	 * @param htmlContainer the document to analyze
	 */
	void analyzeDocument(IHTMLDocumentContainer htmlContainer);

	/**
	 * @param stylesheetURI
	 * @return the coverage information for all directives as a list ordered by position of the directive
	 */
	ImmutableList<IDirectiveCoverage> getDirectiveCoverage(URI stylesheetURI);

	/**
	 * @param stylesheetURI
	 * @return the coverage information for all lines (with the array index being the line number minus one)
	 */
	CoverageStatus[] getLineCoverage(URI stylesheetURI);

	/**
	 * @param stylesheetURI
	 * @return the coverage statistics for the stylesheet
	 */
	ICoverageStatistics getStatistics(URI stylesheetURI);

}
