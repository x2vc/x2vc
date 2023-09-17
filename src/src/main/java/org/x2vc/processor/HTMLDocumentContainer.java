package org.x2vc.processor;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Standard implementation of {@link IHTMLDocumentContainer}.
 */
public class HTMLDocumentContainer implements IHTMLDocumentContainer {

	private static final Logger logger = LogManager.getLogger();
	private IStylesheetManager stylesheetManager;

	private IXMLDocumentContainer source;
	private String htmlDocument;
	private SaxonApiException compilationError;
	private SaxonApiException processingError;
	private ImmutableList<ITraceEvent> traceEvents;
	private IStylesheetCoverage coverage;

	/**
	 * Internal constructor - use an injected {@link IHTMLDocumentFactory} to create
	 * a new document container.
	 *
	 * @param stylesheetManager
	 * @param source
	 * @param htmlDocument
	 * @param compilationError
	 * @param processingError
	 * @param traceEvents
	 */
	HTMLDocumentContainer(IStylesheetManager stylesheetManager, IXMLDocumentContainer source, String htmlDocument,
			SaxonApiException compilationError, SaxonApiException processingError,
			ImmutableList<ITraceEvent> traceEvents) {
		this.stylesheetManager = stylesheetManager;
		// we can either have a result document or error conditions, but not both
		if (htmlDocument == null) {
			checkArgument((compilationError != null) || (processingError != null));
		} else {
			checkArgument((compilationError == null) && (processingError == null));
		}
		this.source = source;
		this.htmlDocument = htmlDocument;
		this.compilationError = compilationError;
		this.processingError = processingError;
		this.traceEvents = traceEvents;
	}

	@Override
	public boolean isFailed() {
		return this.htmlDocument == null;
	}

	@Override
	public Optional<String> getDocument() {
		return Optional.ofNullable(this.htmlDocument);
	}

	@Override
	public Optional<SaxonApiException> getCompilationError() {
		return Optional.ofNullable(this.compilationError);
	}

	@Override
	public Optional<SaxonApiException> getProcessingError() {
		return Optional.ofNullable(this.processingError);
	}

	@Override
	public IXMLDocumentContainer getSource() {
		return this.source;
	}

	@Override
	public Optional<ImmutableList<ITraceEvent>> getTraceEvents() {
		return Optional.ofNullable(this.traceEvents);
	}

	@Override
	public Optional<IStylesheetCoverage> getCoverage() {
		if ((this.traceEvents != null) && (this.coverage == null)) {
			buildCoverage();
		}
		return Optional.ofNullable(this.coverage);
	}

	/**
	 * Fills a {@link IStylesheetCoverage} object with the information supplied by
	 * the trace events.
	 */
	private void buildCoverage() {
		logger.traceEntry();
		// TODO XSLT Coverage: rebuild after structure extraction changes
//		final IStylesheetInformation stylesheet = this.stylesheetManager.get(this.source.getStylesheeURI());
//		final IStylesheetStructure structure = stylesheet.getStructure();
//		this.coverage = stylesheet.createCoverageStatistics();
//		for (final ITraceEvent traceEvent : this.traceEvents) {
//			final int traceID = traceEvent.getTraceID();
//			final IXSLTDirectiveNode directive = structure.getDirectiveByTraceID(traceID);
//			if (!directive.getName().equals(traceEvent.getElementName())) {
//				logger.warn("Trace event element name {} differs from structure element name {}",
//						traceEvent.getElementName(), directive.getName());
//			}
//			this.coverage.recordElementCoverage(traceID, Maps.newHashMap());
//		}
		logger.traceExit();
	}

}
