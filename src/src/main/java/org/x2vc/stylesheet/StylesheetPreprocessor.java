package org.x2vc.stylesheet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.stream.StreamSource;

import com.google.inject.Inject;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;

public class StylesheetPreprocessor implements IStylesheetPreprocessor {

	private Processor processor;
	private IStylesheetExtender extender;
	private IStylesheetStructureExtractor extractor;

	@Inject
	StylesheetPreprocessor(Processor processor, IStylesheetExtender expander, IStylesheetStructureExtractor extractor) {
		this.processor = processor;
		this.extender = expander;
		this.extractor = extractor;
	}

	@Override
	public IStylesheetInformation prepareStylesheet(URI originalLocation, String originalSource) {
		checkNotNull(originalLocation);
		checkNotNull(originalSource);
		checkStylesheet(originalSource);
		String expandedSource = this.extender.extendStylesheet(originalSource);
		IStylesheetStructure structure = this.extractor.extractStructure(expandedSource);
		return new StylesheetInformation(originalLocation, originalSource, expandedSource, structure);
	}

	@Override
	public IStylesheetInformation prepareStylesheet(String originalSource) {
		checkNotNull(originalSource);
		checkStylesheet(originalSource);
		String expandedSource = this.extender.extendStylesheet(originalSource);
		IStylesheetStructure structure = this.extractor.extractStructure(expandedSource);
		return new StylesheetInformation(originalSource, expandedSource, structure);
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
		// try to try to compile the stylesheet to check the overall syntax and version
		try {
			XsltExecutable executable = this.processor.newXsltCompiler()
					.compile(new StreamSource(new StringReader(originalSource)));
			String stylesheetVersion = executable.getUnderlyingCompiledStylesheet().getPrimarySerializationProperties()
					.getProperty("{http://saxon.sf.net/}stylesheet-version");
			// currently only XSLT 1.0 is supported
			if (!stylesheetVersion.equals("10")) {
				throw new IllegalArgumentException("Stylesheet version not supported (only XSLT 1.0 allowed)");
			}
		} catch (SaxonApiException e) {
			throw new IllegalArgumentException("Stylesheet cannot be compiled", e);
		}

		// TODO XSLT check: check stylesheet for unsupported features
	}

}
