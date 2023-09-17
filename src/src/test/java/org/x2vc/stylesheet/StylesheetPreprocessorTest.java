package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.extension.IStylesheetExtender;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

@ExtendWith(MockitoExtension.class)
class StylesheetPreprocessorTest {

	// This series of tests uses the actual Saxon XSLT processor. Using a mocked
	// version would be more stable, but it's a lot of work...
	// TODO XSLT: provide tests that do not require Saxon

	Processor xsltProcessor;

	@Mock
	INamespaceExtractor namespaceExtractor;

	// need a "real" object here because it will be duplicated in the transfer
	Multimap<String, URI> namespacePrefixes;

	@Mock
	IStylesheetExtender stylesheetExtender;

	@Mock
	IStylesheetStructureExtractor structureExtractor;

	@Mock
	IStylesheetStructure structure;

	StylesheetPreprocessor preprocessor;

	@BeforeEach
	void prepareInstances() {
		this.namespacePrefixes = MultimapBuilder.hashKeys().arrayListValues().build();
		this.namespacePrefixes.put("xsl", URI.create("http://www.w3.org/1999/XSL/Transform"));

		this.xsltProcessor = new Processor();
		this.preprocessor = new StylesheetPreprocessor(this.xsltProcessor, this.namespaceExtractor,
				this.stylesheetExtender,
				this.structureExtractor);
		lenient().when(this.structureExtractor.extractStructure(anyString())).thenReturn(this.structure);
	}

	@Test
	void testInvalidStylesheet() throws SaxonApiException {
		final URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, "invalid_stylesheet");
		});
	}

	final String unsupportedStylesheet_Version2 = """
													<?xml version="1.0" encoding="UTF-8"?>
													<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
													<xsl:template match="/">
													</xsl:template>
													</xsl:stylesheet>
													""";

	@Test
	void testUnsupportedStylesheet_Version2() throws SaxonApiException {
		final URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Version2);
		});
	}

	final String unsupportedStylesheet_Import = """
												<?xml version="1.0" encoding="UTF-8"?>
												<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
												<xsl:import href="otherStylesheet.xsl"/>
												<xsl:template match="/">
												</xsl:template>
												</xsl:stylesheet>
												""";

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_Import() throws SaxonApiException {
		final URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Version2);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	final String unsupportedStylesheet_ApplyImports = """
														<?xml version="1.0" encoding="UTF-8"?>
														<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
														<xsl:template match="/">
														<xsl:apply-imports/> <!-- prolly not strictly legal without an import statement -->
														</xsl:template>
														</xsl:stylesheet>
														""";

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_ApplyImports() throws SaxonApiException {
		final URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_ApplyImports);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	final String unsupportedStylesheet_Include = """
													<?xml version="1.0" encoding="UTF-8"?>
													<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
													<xsl:include href="otherStylesheet.xsl"/>
													<xsl:template match="/">
													</xsl:template>
													</xsl:stylesheet>
													""";

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_Include() throws SaxonApiException {
		final URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Include);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	final String minimalStylesheet = """
										<?xml version="1.0" encoding="UTF-8"?>
										<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
										<xsl:template match="/">
										</xsl:template>
										</xsl:stylesheet>
										""";

	final String minimalStylesheet_Extended = """
												<?xml version="1.0" encoding="UTF-8"?>
												<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
												<xsl:template match="/">
												<xsl:message>foobar</xsl:message>
												</xsl:template>
												</xsl:stylesheet>
												""";

	@Test
	void testStylesheetLocation() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		when(this.namespaceExtractor.extractNamespaces(this.minimalStylesheet)).thenReturn(this.namespacePrefixes);
		when(this.namespaceExtractor.findUnusedPrefix(this.namespacePrefixes.keySet(), "trace")).thenReturn("trace1234");
		final IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"), this.minimalStylesheet);
		assertEquals(URI.create("foobar"), info.getURI());
		assertEquals(this.namespacePrefixes, info.getNamespacePrefixes());
		assertEquals("trace1234", info.getTraceNamespacePrefix());
	}

	@Test
	void testStylesheetContents() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		when(this.namespaceExtractor.extractNamespaces(this.minimalStylesheet)).thenReturn(this.namespacePrefixes);
		when(this.namespaceExtractor.findUnusedPrefix(this.namespacePrefixes.keySet(), "trace")).thenReturn("trace1234");
		final IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"), this.minimalStylesheet);
		assertEquals(this.minimalStylesheet, info.getOriginalStylesheet());
		assertEquals(this.namespacePrefixes, info.getNamespacePrefixes());
		assertEquals("trace1234", info.getTraceNamespacePrefix());
	}

	// TODO XSLT structure: support XSLT structure extraction
	// TODO XSLT coverage: support XSLT coverage statistics

}
