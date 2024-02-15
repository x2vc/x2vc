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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IStylesheetParameter;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.SaxonLoggerAdapter;
import org.x2vc.xml.document.IStylesheetParameterValue;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.*;

/**
 * Standard implementation of {@link IXSLTProcessor}.
 */
@Singleton
public class XSLTProcessor implements IXSLTProcessor {

	/**
	 * Internal class to keep all the cached stuff bundled together.
	 */
	private record ProcessorCacheEntry(
			Processor processor,
			XsltCompiler compiler,
			IXMLSchema schema,
			IExtensionFunctionHandler functionHandler,
			XsltExecutable executable) {
	}

	/**
	 * The key used to organize the cache.
	 */
	private record CacheKey(URI stylesheetURI, URI schemaURI, int schemaVersion) {
		public static CacheKey fromXMLDocumentContainer(IXMLDocumentContainer dc) {
			return new CacheKey(dc.getStylesheeURI(), dc.getSchemaURI(), dc.getSchemaVersion());
		}
	}

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private Integer cacheSize;

	@Inject
	XSLTProcessor(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			@TypesafeConfig("x2vc.stylesheet.compiled.cachesize") Integer cacheSize) {
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.cacheSize = cacheSize;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<LoadingCache<CacheKey, ProcessorCacheEntry>> processorCacheSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		logger.debug("Initializing XSLT processor cache (max. {} entries)", this.cacheSize);
		final LoadingCache<CacheKey, ProcessorCacheEntry> procesorCache = CacheBuilder.newBuilder()
			.maximumSize(this.cacheSize)
			.build(new ProcessorCacheLoader(this.stylesheetManager, this.schemaManager));
		return logger.traceExit(procesorCache);
	});

	@Override
	public IHTMLDocumentContainer processDocument(IXMLDocumentContainer xmlDocument) {
		logger.traceEntry();
		final HTMLDocumentContainer.Builder builder = HTMLDocumentContainer.builder(xmlDocument);
		ProcessorCacheEntry cacheEntry = null;
		try {
			cacheEntry = this.processorCacheSupplier.get().get(CacheKey.fromXMLDocumentContainer(xmlDocument));
		} catch (final ExecutionException e) {
			logger.error("Error retrieving compiled stylesheet from cache", e);
			builder.withCompilationError((SaxonApiException) e.getCause());
		}
		if (cacheEntry != null) {
			try {
				logger.debug("preparing for transformation");
				final StringWriter stringWriter = new StringWriter();
				final Serializer out = cacheEntry.processor.newSerializer(stringWriter);
				final ProcessorObserver observer = new ProcessorObserver();
				final Xslt30Transformer transformer = cacheEntry.executable.load30();
				transformer.setMessageHandler(observer);
				transformer.setErrorListener(observer);
				transformer.setErrorReporter(SaxonLoggerAdapter.makeReporter());
				transformer.setTraceListener(observer);
				cacheEntry.functionHandler.storeFunctionResults(xmlDocument.getDocumentDescriptor());
				transferStylesheetParameters(xmlDocument, cacheEntry.schema, transformer);
				logger.debug("preparations complete, launching transformer");
				transformer.transform(new StreamSource(new StringReader(xmlDocument.getDocument())), out);
				logger.debug("transformation complete, processing results");
				cacheEntry.functionHandler.clearFunctionResults();
				builder.withHtmlDocument(stringWriter.toString());
				builder.withTraceEvents(observer.getTraceEvents());
				builder.withDocumentTraceID(observer.getDocumentTraceID());
			} catch (final SaxonApiException e) {
				// we expect some errors due to the explorative nature of the tests (monkey
				// testing), so don't log them, just add them to the result object
				builder.withProcessingError(e);
				cacheEntry.functionHandler.clearFunctionResults();
			}
		}
		final IHTMLDocumentContainer container = builder.build();
		return logger.traceExit(container);
	}

	/**
	 *
	 * @param xmlDocument
	 * @param schema
	 * @param transformer
	 * @throws SaxonApiException
	 */
	private void transferStylesheetParameters(IXMLDocumentContainer xmlDocument, IXMLSchema schema,
			Xslt30Transformer transformer) throws SaxonApiException {
		logger.traceEntry();
		final ImmutableCollection<IStylesheetParameterValue> valuesFromGenerator = xmlDocument.getDocumentDescriptor()
			.getStylesheetParameterValues();
		final Map<QName, XdmValue> valuesForProcessor = Maps.newHashMap();
		for (final IStylesheetParameterValue valueFromGenerator : valuesFromGenerator) {
			final IStylesheetParameter parameterDefinition = schema.getObjectByID(valueFromGenerator.getParameterID(),
					IStylesheetParameter.class);
			final QName parameterName = parameterDefinition.getQualifiedName();
			final XdmValue parameterValue = valueFromGenerator.getXDMValue();
			logger.debug("setting stylesheet parameter {} to value {}", parameterName, parameterValue);
			valuesForProcessor.put(parameterName, parameterValue);
		}
		transformer.setStylesheetParameters(valuesForProcessor);
		logger.traceExit();
	}

	/**
	 * A {@link CacheLoader} to provide precompiled instances for stylesheets.
	 */
	private final class ProcessorCacheLoader extends CacheLoader<CacheKey, ProcessorCacheEntry> {

		private static final Logger logger = LogManager.getLogger();
		private IStylesheetManager stylesheetManager;
		private ISchemaManager schemaManager;

		/**
		 * @param stylesheetManager
		 * @param schemaManager
		 */
		public ProcessorCacheLoader(IStylesheetManager stylesheetManager, ISchemaManager schemaManager) {
			this.stylesheetManager = stylesheetManager;
			this.schemaManager = schemaManager;
		}

		@Override
		public ProcessorCacheEntry load(CacheKey cacheKey) throws SaxonApiException {
			logger.traceEntry();
			// load stylesheet and schema
			final IStylesheetInformation stylesheet = this.stylesheetManager.get(cacheKey.stylesheetURI());
			final IXMLSchema schema = this.schemaManager.getSchema(cacheKey.stylesheetURI(),
					cacheKey.schemaVersion());

			logger.debug("preparing new XSLT processor and compiler");

			// because the extension functions are registered on a processor level, we
			// need to acquire a new processor instance for each schema version
			final Processor processor = new Processor();
			processor.setConfigurationProperty(Feature.LINE_NUMBERING, true);

			// create and register the extension handler
			final IExtensionFunctionHandler functionHandler = new ExtensionFunctionHandler(schema);
			functionHandler.registerFunctions(processor);

			// create compiler in trace mode
			final XsltCompiler compiler = processor.newXsltCompiler();
			compiler.setCompileWithTracing(true);
			compiler.setErrorReporter(SaxonLoggerAdapter.makeReporter());

			logger.debug("Compiling stylesheet");

			// compile the stylesheet to produce the executable
			final XsltExecutable executable = compiler
				.compile(new StreamSource(new StringReader(stylesheet.getPreparedStylesheet())));

			logger.debug("Stylesheet compiled successfully");

			// bundle it all together...
			final ProcessorCacheEntry result = new ProcessorCacheEntry(processor, compiler, schema, functionHandler,
					executable);

			return logger.traceExit(result);
		}
	}

}
