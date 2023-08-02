package org.x2vc.processor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.xmldoc.IXMLDocumentContainer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Standard implementation of {@link IXSLTProcessor}.
 */
public class XSLTProcessor implements IXSLTProcessor {

	private static Logger logger = LogManager.getLogger();

	private Processor processor;

	private LoadingCache<IStylesheetInformation, XsltExecutable> stylesheetCache;

	@Inject
	XSLTProcessor(Processor processor) {
		this.processor = processor;
		this.stylesheetCache = CacheBuilder.newBuilder().maximumSize(25).build(new StylesheetCacheLoader(processor));
	}

	@Override
	public IHTMLDocumentContainer processDocument(IXMLDocumentContainer xmlDocument) {
		logger.traceEntry();
		HTMLDocumentContainer.Builder builder = new HTMLDocumentContainer.Builder(xmlDocument);
		XsltExecutable stylesheet = null;
		try {
			stylesheet = this.stylesheetCache.get(xmlDocument.getStylesheet());
		} catch (ExecutionException e) {
			logger.error("Error retrieving compiled stylesheet from cache", e);
			builder.withCompilationError((SaxonApiException) e.getCause());
		}
		if (stylesheet != null) {
			try {
				StringWriter stringWriter = new StringWriter();
				Serializer out = this.processor.newSerializer(stringWriter);
				TraceMessageCollector messageCollector = new TraceMessageCollector();
				Xslt30Transformer transformer = stylesheet.load30();
				transformer.setMessageHandler(messageCollector);
				transformer.transform(new StreamSource(new StringReader(xmlDocument.getDocument())), out);
				builder.withHtmlDocument(stringWriter.toString());
				builder.withTraceEvents(messageCollector.getTraceEvents());
			} catch (SaxonApiException e) {
				logger.error("Error processing XML document", e);
				builder.withProcessingError(e);
			}
		}
		HTMLDocumentContainer container = builder.build();
		return logger.traceExit(container);
	}

	/**
	 * A {@link CacheLoader} to provide precompiled instances for stylesheets.
	 */
	private final class StylesheetCacheLoader extends CacheLoader<IStylesheetInformation, XsltExecutable> {

		private static Logger logger = LogManager.getLogger();
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
			XsltExecutable result = this.compiler
					.compile(new StreamSource(new StringReader(stylesheet.getPreparedStylesheet())));
			return logger.traceExit(result);
		}
	}

}
