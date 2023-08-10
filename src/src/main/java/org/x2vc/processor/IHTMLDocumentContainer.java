package org.x2vc.processor;

import java.util.Optional;

import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * A container object that is used to transport the transformed HTML document
 * and the trace results.
 *
 * If the transformation failed, the container holds the structured error
 * information instead of the HTML document.
 *
 * An HTML document container contains a reference to the XML document container
 * used to generate the HTML document.
 */
public interface IHTMLDocumentContainer {

	/**
	 * Checks whether the transformation failed. Shortcut to
	 * <code>getDocument().isAbsent()</code>s
	 *
	 * @return <code>true</code> if the transformation failed for some reason.
	 */
	boolean isFailed();

	/**
	 * @return the transformed document, or empty if the transformation failed
	 */
	Optional<String> getDocument();

	/**
	 * @return any error that occurred during the stylesheet compilation
	 */
	Optional<SaxonApiException> getCompilationError();

	/**
	 * @return any error that occurred during the stylesheet processing
	 */
	Optional<SaxonApiException> getProcessingError();

	/**
	 * @return the XML document container that was used to generate the document
	 */
	IXMLDocumentContainer getSource();

	/**
	 * @return the {@link ITraceEvent}s collected during the execution
	 */
	Optional<ImmutableList<ITraceEvent>> getTraceEvents();

	/**
	 * @return the coverage information collected during the execution.
	 */
	Optional<IStylesheetCoverage> getCoverage();

}
