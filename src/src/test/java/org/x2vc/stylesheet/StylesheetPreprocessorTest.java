package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

@ExtendWith(MockitoExtension.class)
class StylesheetPreprocessorTest {

	// This series of tests uses the actual Saxon XSLT processor. Using a mocked
	// version would be more stable, but it's a lot of work...
	// TODO XSLT: provide tests that do not require Saxon

	Processor xsltProcessor;

	@Mock
	IStylesheetExtender stylesheetExtender;

	@Mock
	IStylesheetStructureExtractor structureExtractor;

	@Mock
	IStylesheetStructure structure;

	StylesheetPreprocessor preprocessor;

	@BeforeEach
	void prepareInstances() {
		this.xsltProcessor = new Processor();
		this.preprocessor = new StylesheetPreprocessor(this.xsltProcessor, this.stylesheetExtender,
				this.structureExtractor);
		lenient().when(this.structureExtractor.extractStructure(anyString())).thenReturn(this.structure);
	}

	@Test
	void testInvalidStylesheet_whenFileBased() throws SaxonApiException {
		URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, "invalid_stylesheet");
		});
	}

	@Test
	void testInvalidStylesheet_whenNotFileBased() throws SaxonApiException {
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet("invalid_stylesheet");
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
	void testUnsupportedStylesheet_Version2_whenFileBased() throws SaxonApiException {
		URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Version2);
		});
	}

	@Test
	void testUnsupportedStylesheet_Version2_whenNotFileBased() throws SaxonApiException {
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(this.unsupportedStylesheet_Version2);
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
	void testUnsupportedStylesheet_Import_whenFileBased() throws SaxonApiException {
		URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Version2);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_Import_whenNotFileBased() throws SaxonApiException {
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(this.unsupportedStylesheet_Version2);
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
	void testUnsupportedStylesheet_ApplyImports_whenFileBased() throws SaxonApiException {
		URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_ApplyImports);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_ApplyImports_whenNotFileBased() throws SaxonApiException {
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(this.unsupportedStylesheet_ApplyImports);
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
	void testUnsupportedStylesheet_Include_whenFileBased() throws SaxonApiException {
		URI testURI = URI.create("foobar");
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(testURI, this.unsupportedStylesheet_Include);
		});

		fail("test not completed");
		// TODO XSLT check: check stylesheet for unsupported features
	}

	@Test
	@Disabled("feature is not supported yet")
	void testUnsupportedStylesheet_Include_whenNotFileBased() throws SaxonApiException {
		assertThrows(IllegalArgumentException.class, () -> {
			this.preprocessor.prepareStylesheet(this.unsupportedStylesheet_Include);
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
	void testStylesheetLocation_whenFileBased() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"), this.minimalStylesheet);
		assertTrue(info.isFileBased());
		assertEquals(URI.create("foobar"), info.getOriginalLocation());
	}

	@Test
	void testStylesheetLocation_whenNotFileBased() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		IStylesheetInformation info = this.preprocessor.prepareStylesheet(this.minimalStylesheet);
		assertFalse(info.isFileBased());
	}

	@Test
	void testStylesheetContents_whenFileBased() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"), this.minimalStylesheet);
		assertEquals(this.minimalStylesheet, info.getOriginalStylesheet());
	}

	@Test
	void testStylesheetContents_whenNotFileBased() throws SaxonApiException {
		when(this.stylesheetExtender.extendStylesheet(this.minimalStylesheet)).thenReturn(this.minimalStylesheet_Extended);
		IStylesheetInformation info = this.preprocessor.prepareStylesheet(this.minimalStylesheet);
		assertEquals(this.minimalStylesheet, info.getOriginalStylesheet());
	}

	// TODO XSLT structure: support XSLT structure extraction
	// TODO XSLT coverage: support XSLT coverage statistics

}
