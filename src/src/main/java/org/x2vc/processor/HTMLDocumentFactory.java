package org.x2vc.processor;

import java.util.List;

import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Standard implementation of {@link IHTMLDocumentFactory}
 */
@Singleton
public class HTMLDocumentFactory implements IHTMLDocumentFactory {

	private IStylesheetManager stylesheetManager;

	/**
	 * @param stylesheetManager
	 */
	@Inject
	HTMLDocumentFactory(IStylesheetManager stylesheetManager) {
		super();
		this.stylesheetManager = stylesheetManager;
	}

	@Override
	public Builder newBuilder(IXMLDocumentContainer source) {
		return new BuilderImpl(this.stylesheetManager, source);
	}

	class BuilderImpl implements IHTMLDocumentFactory.Builder {
		private IStylesheetManager stylesheetManager;
		private IXMLDocumentContainer source;
		private String htmlDocument;
		private SaxonApiException compilationError;
		private SaxonApiException processingError;
		private ImmutableList<ITraceEvent> traceEvents;

		/**
		 * @param stylesheetManager
		 * @param source
		 */
		BuilderImpl(IStylesheetManager stylesheetManager, IXMLDocumentContainer source) {
			super();
			this.stylesheetManager = stylesheetManager;
			this.source = source;
		}

		@Override
		public Builder withHtmlDocument(String htmlDocument) {
			this.htmlDocument = htmlDocument;
			return this;
		}

		@Override
		public Builder withCompilationError(SaxonApiException compilationError) {
			this.compilationError = compilationError;
			return this;
		}

		@Override
		public Builder withProcessingError(SaxonApiException processingError) {
			this.processingError = processingError;
			return this;
		}

		@Override
		public Builder withTraceEvents(List<ITraceEvent> traceEvents) {
			this.traceEvents = ImmutableList.copyOf(traceEvents);
			return this;
		}

		@Override
		public IHTMLDocumentContainer build() {
			return new HTMLDocumentContainer(this.stylesheetManager, this.source, this.htmlDocument,
					this.compilationError, this.processingError, this.traceEvents);
		}

	}

}
