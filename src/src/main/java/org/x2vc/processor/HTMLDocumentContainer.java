package org.x2vc.processor;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Standard implementation of {@link IHTMLDocumentContainer}.
 */
class HTMLDocumentContainer implements IHTMLDocumentContainer {

	private static final Logger logger = LogManager.getLogger();
	private IXMLDocumentContainer source;
	private String htmlDocument;
	private SaxonApiException compilationError;
	private SaxonApiException processingError;
	private ImmutableList<ITraceEvent> traceEvents;
	private IStylesheetCoverage coverage;

	private HTMLDocumentContainer(Builder builder) {
		// we can either have a result document or error conditions, but not both
		if (builder.htmlDocument == null) {
			checkArgument((builder.compilationError != null) || (builder.processingError != null));
		} else {
			checkArgument((builder.compilationError == null) && (builder.processingError == null));
		}
		this.source = builder.source;
		this.htmlDocument = builder.htmlDocument;
		this.compilationError = builder.compilationError;
		this.processingError = builder.processingError;
		this.traceEvents = builder.traceEvents;
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
		final IStylesheetInformation stylesheet = this.source.getStylesheet();
		final IStylesheetStructure structure = stylesheet.getStructure();
		this.coverage = stylesheet.createCoverageStatistics();
		for (final ITraceEvent traceEvent : this.traceEvents) {
			final int traceID = traceEvent.getTraceID();
			final IXSLTDirectiveNode directive = structure.getDirectiveByTraceID(traceID);
			if (!directive.getName().equals(traceEvent.getElementName())) {
				logger.warn("Trace event element name {} differs from structure element name {}",
						traceEvent.getElementName(), directive.getName());
			}
			this.coverage.recordElementCoverage(traceID, Maps.newHashMap());
		}
		logger.traceExit();
	}

	/**
	 * Builder to build {@link HTMLDocumentContainer}.
	 */
	public static final class Builder {
		private IXMLDocumentContainer source;
		private String htmlDocument;
		private SaxonApiException compilationError;
		private SaxonApiException processingError;
		private ImmutableList<ITraceEvent> traceEvents;

		public Builder(IXMLDocumentContainer source) {
			this.source = source;
		}

		/**
		 * Builder method for htmlDocument parameter.
		 *
		 * @param htmlDocument field to set
		 * @return builder
		 */
		public Builder withHtmlDocument(String htmlDocument) {
			this.htmlDocument = htmlDocument;
			return this;
		}

		/**
		 * Builder method for compilationError parameter.
		 *
		 * @param compilationError field to set
		 * @return builder
		 */
		public Builder withCompilationError(SaxonApiException compilationError) {
			this.compilationError = compilationError;
			return this;
		}

		/**
		 * Builder method for processingError parameter.
		 *
		 * @param processingError field to set
		 * @return builder
		 */
		public Builder withProcessingError(SaxonApiException processingError) {
			this.processingError = processingError;
			return this;
		}

		/**
		 * @param traceEvents
		 * @return builder
		 */
		public Builder withTraceEvents(List<ITraceEvent> traceEvents) {
			this.traceEvents = ImmutableList.copyOf(traceEvents);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public HTMLDocumentContainer build() {
			return new HTMLDocumentContainer(this);
		}

	}

}
