package org.x2vc.stylesheet;

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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.utilities.SaxonLoggerAdapter;
import org.x2vc.utilities.XMLUtilities;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Standard implementation of {@link IStylesheetPreprocessor}.
 */
public class StylesheetPreprocessor implements IStylesheetPreprocessor {

	private static final Logger logger = LogManager.getLogger();

	private static final String TRACE_NAMESPACE_PFREIX = "trace";

	private Processor processor;
	private INamespaceExtractor namespaceExtractor;
	private IStylesheetStructureExtractor extractor;

	private Boolean prettyPrinterEnabled;

	@Inject
	StylesheetPreprocessor(Processor processor, INamespaceExtractor namespaceExtractor,
			IStylesheetStructureExtractor extractor,
			@TypesafeConfig("x2vc.stylesheet.pretty_print") Boolean prettyPrinterEnabled) {
		this.processor = processor;
		this.namespaceExtractor = namespaceExtractor;
		this.extractor = extractor;
		this.prettyPrinterEnabled = prettyPrinterEnabled;
	}

	@Override
	public IStylesheetInformation prepareStylesheet(URI uri, String originalSource) {
		checkNotNull(uri);
		checkNotNull(originalSource);
		checkStylesheet(uri, originalSource);

		String formattedSource = originalSource;
		if (Boolean.TRUE.equals(this.prettyPrinterEnabled)) {
			// pretty-print the stylesheet to make it easier to identify elements by
			// position
			formattedSource = XMLUtilities.prettyPrint(originalSource, format -> {
				format.setIndentSize(4);
				format.setNewlines(true);
				format.setNewLineAfterNTags(1);
			});
		}

		// collect the namespaces and prefixes and select an unused one for the trace
		// elements
		final Multimap<String, URI> namespacePrefixes = this.namespaceExtractor.extractNamespaces(formattedSource);
		final String traceNamespacePrefix = this.namespaceExtractor.findUnusedPrefix(namespacePrefixes.keySet(),
				TRACE_NAMESPACE_PFREIX);

		// extract the stylesheet structure
		final IStylesheetStructure structure = this.extractor.extractStructure(formattedSource);

		return new StylesheetInformation(uri, originalSource, formattedSource, namespacePrefixes, traceNamespacePrefix,
				structure);
	}

	/**
	 * Compile the original stylesheet to check for syntax errors or other irregularities (the results of this
	 * compilation are then discarded). Also check whether one of the unsupported features is used (version > 1.0,
	 * xsl:import, xsl:include, xsl:apply-imports, ...).
	 *
	 * @param uri
	 *
	 * @param originalSource
	 * @throws throws IllegalArgumentException if the stylesheet is not usable
	 */
	private void checkStylesheet(URI uri, String originalSource) throws IllegalArgumentException {
		logger.traceEntry();
		// try to try to compile the stylesheet to check the overall syntax and version
		try {
			final XsltCompiler compiler = this.processor.newXsltCompiler();
			compiler.setErrorReporter(SaxonLoggerAdapter.makeReporter());
			final XsltExecutable executable = compiler.compile(new StreamSource(new StringReader(originalSource)));
			final String stylesheetVersion = executable.getUnderlyingCompiledStylesheet()
				.getPrimarySerializationProperties().getProperty("{http://saxon.sf.net/}stylesheet-version");
			// currently only XSLT 1.0 is supported
			if (!stylesheetVersion.equals("10")) {
				throw logger
					.throwing(new IllegalArgumentException("Stylesheet version not supported (only XSLT 1.0 allowed)"));
			}
		} catch (final SaxonApiException e) {
			final Throwable cause = e.getCause();
			if (cause == null) {
				logger.error("Stylesheet {} cannot be compiled: {}", uri, e.getMessage());
			} else {
				logger.error("Stylesheet {} cannot be compiled: {}, cause: {}", uri, e.getMessage(),
						cause.getMessage());
			}
			logger.debug("Compilation error", e);
			throw new IllegalArgumentException("Stylesheet cannot be compiled", e);
		}

		// TODO XSLT check: check stylesheet for unsupported features
		logger.traceExit();
	}

}
