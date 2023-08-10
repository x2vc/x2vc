package org.x2vc.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.sf.saxon.s9api.*;

/**
 * Standard implementation of {@link IXSLTProcessor}.
 */
@Singleton
public class XSLTProcessor implements IXSLTProcessor {

	private static final Logger logger = LogManager.getLogger();

	private Processor processor;

	private LoadingCache<IStylesheetInformation, XsltExecutable> stylesheetCache;

	@Inject
	XSLTProcessor(Processor processor) {
		this.processor = processor;
		// TODO Infrastructure: make cache sizes configurable
		this.stylesheetCache = CacheBuilder.newBuilder().maximumSize(25).build(new StylesheetCacheLoader(processor));
	}

	@Override
	public IHTMLDocumentContainer processDocument(IXMLDocumentContainer xmlDocument) {
		logger.traceEntry();
		final HTMLDocumentContainer.Builder builder = new HTMLDocumentContainer.Builder(xmlDocument);
		XsltExecutable stylesheet = null;
		try {
			stylesheet = this.stylesheetCache.get(xmlDocument.getStylesheet());
		} catch (final ExecutionException e) {
			logger.error("Error retrieving compiled stylesheet from cache", e);
			builder.withCompilationError((SaxonApiException) e.getCause());
		}
		if (stylesheet != null) {
			try {
				final StringWriter stringWriter = new StringWriter();
				final Serializer out = this.processor.newSerializer(stringWriter);
				final TraceMessageCollector messageCollector = new TraceMessageCollector();
				final Xslt30Transformer transformer = stylesheet.load30();
				transformer.setMessageHandler(messageCollector);
				transformer.transform(new StreamSource(new StringReader(xmlDocument.getDocument())), out);
				builder.withHtmlDocument(stringWriter.toString());
				builder.withTraceEvents(messageCollector.getTraceEvents());
			} catch (final SaxonApiException e) {
				logger.error("Error processing XML document", e);
				builder.withProcessingError(e);
			}
		}
		final HTMLDocumentContainer container = builder.build();
		return logger.traceExit(container);
	}

	/**
	 * A {@link CacheLoader} to provide precompiled instances for stylesheets.
	 */
	private final class StylesheetCacheLoader extends CacheLoader<IStylesheetInformation, XsltExecutable> {

		private static final Logger logger = LogManager.getLogger();
		private XsltCompiler compiler;

		/**
		 * @param processor the XSLT processor to uses
		 */
		private StylesheetCacheLoader(Processor processor) {
			this.compiler = processor.newXsltCompiler();
		}

		@Override
		public XsltExecutable load(IStylesheetInformation stylesheet) throws SaxonApiException {
			logger.traceEntry();
			logger.debug("Compiling stylesheet to provide cache entry");
			final XsltExecutable result = this.compiler
				.compile(new StreamSource(new StringReader(stylesheet.getPreparedStylesheet())));
			return logger.traceExit(result);
		}
	}

}
