package org.x2vc.processor;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;

import org.x2vc.xmldoc.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Standard implementation of {@link IHTMLDocumentContainer}.
 */
class HTMLDocumentContainer implements IHTMLDocumentContainer {

	IXMLDocumentContainer source;
	String htmlDocument;
	SaxonApiException compilationError;
	SaxonApiException processingError;
	private ImmutableList<ITraceEvent> traceEvents;

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
	public ImmutableList<ITraceEvent> getTraceEvents() {
		return this.traceEvents;
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
		 */
		public void withTraceEvents(List<ITraceEvent> traceEvents) {
			this.traceEvents = ImmutableList.copyOf(traceEvents);
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
