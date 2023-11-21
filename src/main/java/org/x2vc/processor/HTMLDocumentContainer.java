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
package org.x2vc.processor;


import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Standard implementation of {@link IHTMLDocumentContainer}.
 */
public final class HTMLDocumentContainer implements IHTMLDocumentContainer {

	private final IXMLDocumentContainer source;
	private final String htmlDocument;
	private final Optional<SaxonApiException> compilationError;
	private final Optional<SaxonApiException> processingError;
	private final ImmutableList<ITraceEvent> traceEvents;
	private final UUID documentTraceID;

	/**
	 * Internal constructor - use {@link #builder(IXMLDocumentContainer)} to create a new document container.
	 *
	 * @param source
	 * @param htmlDocument
	 * @param compilationError
	 * @param processingError
	 * @param traceEvents
	 * @param documentTraceID
	 */
	HTMLDocumentContainer(IXMLDocumentContainer source, String htmlDocument,
			SaxonApiException compilationError, SaxonApiException processingError,
			ImmutableList<ITraceEvent> traceEvents, UUID documentTraceID) {
		// we can either have a result document or error conditions, but not both
		if (htmlDocument == null) {
			checkArgument((compilationError != null) || (processingError != null));
		} else {
			checkArgument((compilationError == null) && (processingError == null));
		}
		this.source = source;
		this.htmlDocument = htmlDocument;
		this.compilationError = Optional.ofNullable(compilationError);
		this.processingError = Optional.ofNullable(processingError);
		this.traceEvents = traceEvents;
		this.documentTraceID = documentTraceID;
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
		return this.compilationError;
	}

	@Override
	public Optional<SaxonApiException> getProcessingError() {
		return this.processingError;
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
	public UUID getDocumentTraceID() {
		return this.documentTraceID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.compilationError, this.documentTraceID, this.htmlDocument, this.processingError,
				this.source, this.traceEvents);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof HTMLDocumentContainer)) {
			return false;
		}
		final HTMLDocumentContainer other = (HTMLDocumentContainer) obj;
		return Objects.equals(this.compilationError, other.compilationError)
				&& Objects.equals(this.documentTraceID, other.documentTraceID)
				&& Objects.equals(this.htmlDocument, other.htmlDocument)
				&& Objects.equals(this.processingError, other.processingError)
				&& Objects.equals(this.source, other.source)
				&& Objects.equals(this.traceEvents, other.traceEvents);
	}

	/**
	 * Creates a new builder to construct a {@link IHTMLDocumentContainer} instance.
	 *
	 * @param source
	 * @return the builder
	 */
	public static Builder builder(IXMLDocumentContainer source) {
		return new Builder(source);
	}

	/**
	 * A builder to create new {@link IHTMLDocumentContainer} instances.
	 */
	public static final class Builder {
		private IXMLDocumentContainer source;
		private String htmlDocument;
		private SaxonApiException compilationError;
		private SaxonApiException processingError;
		private ImmutableList<ITraceEvent> traceEvents;
		private UUID documentTraceID;

		/**
		 * @param stylesheetManager
		 * @param source
		 */
		Builder(IXMLDocumentContainer source) {
			this.source = source;
		}

		/**
		 * Adds a HTML document to the builder
		 *
		 * @param htmlDocument field to set
		 * @return builder
		 */
		public Builder withHtmlDocument(String htmlDocument) {
			this.htmlDocument = htmlDocument;
			return this;
		}

		/**
		 * Sets a compilation error on the builder.
		 *
		 * @param compilationError field to set
		 * @return builder
		 */
		public Builder withCompilationError(SaxonApiException compilationError) {
			this.compilationError = compilationError;
			return this;
		}

		/**
		 * Sets a processing error on the builder.
		 *
		 * @param processingError field to set
		 * @return builder
		 */
		public Builder withProcessingError(SaxonApiException processingError) {
			this.processingError = processingError;
			return this;
		}

		/**
		 * Adds trace events to the builder.
		 *
		 * @param traceEvents
		 * @return builder
		 */
		public Builder withTraceEvents(List<ITraceEvent> traceEvents) {
			this.traceEvents = ImmutableList.copyOf(traceEvents);
			return this;
		}

		/**
		 * Adds the document trace ID to the builder
		 *
		 * @param documentTraceID
		 * @return builder
		 */
		public Builder withDocumentTraceID(UUID documentTraceID) {
			this.documentTraceID = documentTraceID;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public IHTMLDocumentContainer build() {
			return new HTMLDocumentContainer(this.source, this.htmlDocument,
					this.compilationError, this.processingError, this.traceEvents, this.documentTraceID);
		}

	}

}
