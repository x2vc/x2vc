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
package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.net.URI;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.utilities.xml.ILocationMap;
import org.x2vc.utilities.xml.ILocationMapBuilder;
import org.x2vc.utilities.xml.ITagMap;
import org.x2vc.utilities.xml.ITagMapBuilder;

import com.google.common.collect.Multimap;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

@ExtendWith(MockitoExtension.class)
class StylesheetPreprocessorTest {

	// This series of tests uses the actual Saxon XSLT processor. Using a mocked
	// version would be more stable, but it's a lot of work...
	// TODO #32 XSLT: provide tests that do not require Saxon

	Processor xsltProcessor;

	@Mock
	INamespaceExtractor namespaceExtractor;

	@Mock
	Multimap<String, URI> namespacePrefixes;

	@Mock
	IStylesheetStructureExtractor structureExtractor;

	@Mock
	ILocationMapBuilder locationMapFactory;

	@Mock
	ILocationMap locationMap;

	@Mock
	ITagMapBuilder tagMapFactory;

	@Mock
	ITagMap tagMap;

	@Mock
	IStylesheetStructure structure;

	StylesheetPreprocessor preprocessor;

	@BeforeEach
	void prepareInstances() {
		this.xsltProcessor = new Processor();
		lenient().when(this.locationMapFactory.buildLocationMap(any())).thenAnswer(a -> this.locationMap);
		lenient().when(this.tagMapFactory.buildTagMap(anyString(), eq(this.locationMap))).thenAnswer(a -> this.tagMap);
		this.preprocessor = new StylesheetPreprocessor(this.xsltProcessor, this.namespaceExtractor,
				this.structureExtractor, this.locationMapFactory, this.tagMapFactory, false);
		lenient().when(this.structureExtractor.extractStructure(anyString(), eq(this.locationMap), eq(this.tagMap)))
			.thenReturn(this.structure);
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
		// TODO #30 XSLT check: check stylesheet for unsupported features
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
		// TODO #30 XSLT check: check stylesheet for unsupported features
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
		// TODO #30 XSLT check: check stylesheet for unsupported features
	}

	final String minimalStylesheet = """
										<?xml version="1.0" encoding="UTF-8"?>
										<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
										<xsl:template match="/">
										</xsl:template>
										</xsl:stylesheet>
										""";

	final String minimalStylesheet_Formatted = """
												<?xml version="1.0" encoding="UTF-8"?>

												<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
												    <xsl:template match="/"></xsl:template>
												</xsl:stylesheet>
												""";

	@Test
	void testStylesheetContents_WithFormatterEnabled() throws SaxonApiException {
		this.preprocessor = new StylesheetPreprocessor(this.xsltProcessor, this.namespaceExtractor,
				this.structureExtractor, this.locationMapFactory, this.tagMapFactory, true);
		when(this.namespaceExtractor
			.extractNamespaces(argThat(Matchers.equalToCompressingWhiteSpace(this.minimalStylesheet_Formatted))))
			.thenReturn(this.namespacePrefixes);
		when(this.namespaceExtractor.findUnusedPrefix(this.namespacePrefixes.keySet(), "trace"))
			.thenReturn("trace1234");
		final IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"),
				this.minimalStylesheet);
		assertEquals(URI.create("foobar"), info.getURI());
		assertEquals(this.minimalStylesheet, info.getOriginalStylesheet());
		assertTrue(Matchers.equalToCompressingWhiteSpace(this.minimalStylesheet_Formatted)
			.matches(info.getPreparedStylesheet()));
		assertSame(this.namespacePrefixes, info.getNamespacePrefixes());
		assertEquals("trace1234", info.getTraceNamespacePrefix());
		assertSame(this.locationMap, info.getLocationMap());
		assertSame(this.tagMap, info.getTagMap());
	}

	@Test
	void testStylesheetContents_WithFormatterDisabled() throws SaxonApiException {
		when(this.namespaceExtractor
			.extractNamespaces(argThat(Matchers.equalToCompressingWhiteSpace(this.minimalStylesheet))))
			.thenReturn(this.namespacePrefixes);
		when(this.namespaceExtractor.findUnusedPrefix(this.namespacePrefixes.keySet(), "trace"))
			.thenReturn("trace1234");
		final IStylesheetInformation info = this.preprocessor.prepareStylesheet(URI.create("foobar"),
				this.minimalStylesheet);
		assertEquals(URI.create("foobar"), info.getURI());
		assertEquals(this.minimalStylesheet, info.getOriginalStylesheet());
		assertTrue(Matchers.equalToCompressingWhiteSpace(this.minimalStylesheet).matches(info.getPreparedStylesheet()));
		assertSame(this.namespacePrefixes, info.getNamespacePrefixes());
		assertEquals("trace1234", info.getTraceNamespacePrefix());
		assertSame(this.locationMap, info.getLocationMap());
		assertSame(this.tagMap, info.getTagMap());
	}

}
