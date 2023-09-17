package org.x2vc.stylesheet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.utilities.XMLUtilities;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
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

	@Inject
	StylesheetPreprocessor(Processor processor, INamespaceExtractor namespaceExtractor,
			IStylesheetStructureExtractor extractor) {
		this.processor = processor;
		this.namespaceExtractor = namespaceExtractor;
		this.extractor = extractor;
	}

	@Override
	public IStylesheetInformation prepareStylesheet(URI uri, String originalSource) {
		checkNotNull(uri);
		checkNotNull(originalSource);
		checkStylesheet(originalSource);

		// pretty-print the stylesheet to make it easier to identify elements by
		// position
		final String formattedSource = XMLUtilities.prettyPrint(originalSource, format -> {
			format.setIndentSize(4);
			format.setNewlines(true);
			format.setNewLineAfterNTags(1);
		});

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
	 * Compile the original stylesheet to check for syntax errors or other
	 * irregularities (the results of this compilation are then discarded). Also
	 * check whether one of the unsupported features is used (version > 1.0,
	 * xsl:import, xsl:include, xsl:apply-imports, ...).
	 *
	 * @param originalSource
	 * @throws throws IllegalArgumentException if the stylesheet is not usable
	 */
	private void checkStylesheet(String originalSource) throws IllegalArgumentException {
		logger.traceEntry();
		// try to try to compile the stylesheet to check the overall syntax and version
		try {
			final XsltExecutable executable = this.processor.newXsltCompiler()
				.compile(new StreamSource(new StringReader(originalSource)));
			final String stylesheetVersion = executable.getUnderlyingCompiledStylesheet()
				.getPrimarySerializationProperties().getProperty("{http://saxon.sf.net/}stylesheet-version");
			// currently only XSLT 1.0 is supported
			if (!stylesheetVersion.equals("10")) {
				throw logger
					.throwing(new IllegalArgumentException("Stylesheet version not supported (only XSLT 1.0 allowed)"));
			}
		} catch (final SaxonApiException e) {
			throw logger.throwing(new IllegalArgumentException("Stylesheet cannot be compiled", e));
		}

		// TODO XSLT check: check stylesheet for unsupported features
		logger.traceExit();
	}

}
