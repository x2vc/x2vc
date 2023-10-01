package org.x2vc.processor;

import java.util.List;
import java.util.UUID;

import org.x2vc.xml.document.IXMLDocumentContainer;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Provides an interface to construct an instance of {@link IHTMLDocumentContainer}
 */
public interface IHTMLDocumentFactory {

	/**
	 * Creates a new builder to construct a {@link IHTMLDocumentContainer} instance.
	 *
	 * @param source
	 * @return the builder
	 */
	public Builder newBuilder(IXMLDocumentContainer source);

	/**
	 * A builder to create new {@link IHTMLDocumentContainer} instances.
	 */
	public interface Builder {

		/**
		 * Adds a HTML document to the builder
		 *
		 * @param htmlDocument field to set
		 * @return builder
		 */
		public Builder withHtmlDocument(String htmlDocument);

		/**
		 * Sets a compilation error on the builder.
		 *
		 * @param compilationError field to set
		 * @return builder
		 */
		public Builder withCompilationError(SaxonApiException compilationError);

		/**
		 * Sets a processing error on the builder.
		 *
		 * @param processingError field to set
		 * @return builder
		 */
		public Builder withProcessingError(SaxonApiException processingError);

		/**
		 * Adds trace events to the builder.
		 *
		 * @param traceEvents
		 * @return builder
		 */
		public Builder withTraceEvents(List<ITraceEvent> traceEvents);

		/**
		 * Adds the document trace ID to the builder
		 *
		 * @param documentTraceID
		 * @return builder
		 */
		public Builder withDocumentTraceID(UUID documentTraceID);

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public IHTMLDocumentContainer build();
	}

}
