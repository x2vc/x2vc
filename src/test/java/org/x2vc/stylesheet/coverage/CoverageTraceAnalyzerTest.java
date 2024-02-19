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
package org.x2vc.stylesheet.coverage;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.process.CheckerModule;
import org.x2vc.processor.IExecutionTraceEvent;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ITraceEvent;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.utilities.xml.*;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.github.racc.tscg.TypesafeConfigModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@ExtendWith(MockitoExtension.class)
class CoverageTraceAnalyzerTest {

	private static final String TEST_STYLESHEET = """
													<?xml version="1.0"?>

													<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
														<xsl:template name="foo">
															<xsl:choose>
																<xsl:when test="fooA">
																	<p>test A</p>
																</xsl:when>
																<xsl:when test="fooB">
																	<p>test B</p>
																</xsl:when>
																<xsl:otherwise>
																	<p>test O</p>
																</xsl:otherwise>
															</xsl:choose>
														</xsl:template>
													</xsl:stylesheet>
													""";

	private URI stylesheetURI;

	private IHTMLDocumentContainer htmlContainer;

	private CoverageTraceAnalyzer analyzer;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {

		// prepare the HTML container that will be the input object for the analyzer
		this.htmlContainer = mock(IHTMLDocumentContainer.class, "HTML container");
		final Optional<ImmutableList<ITraceEvent>> events = mockTraceEvents();
		lenient().when(this.htmlContainer.getTraceEvents()).thenReturn(events);

		// prepare the XML container that describes the input data and wire it to the HTML container
		final IXMLDocumentContainer xmlContainer = mock(IXMLDocumentContainer.class, "XML container");
		lenient().when(this.htmlContainer.getSource()).thenReturn(xmlContainer);

		// invent a stylesheet URI for testing and let the XML container return that stylesheet
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "myStylesheet");
		lenient().when(xmlContainer.getStylesheeURI()).thenReturn(this.stylesheetURI);

		// prepare a stylesheet structure - this is a bit more involved
		// TODO #32 CoverageTraceAnalyzer: replace actual instance of StylesheetStructureExtractor with mock.
		// This is a lot of work, so for now we use the actual application classes to construct a representation of the-
		final Config config = ConfigFactory.load();
		final Injector injector = Guice.createInjector(new CheckerModule(config),
				TypesafeConfigModule.fromConfigWithPackage(config, "org.x2vc"));
		final IStylesheetStructureExtractor extractor = injector.getInstance(IStylesheetStructureExtractor.class);
		final ILocationMapBuilder locationMapBuilder = injector.getInstance(ILocationMapBuilder.class);
		final ITagMapBuilder tagMapBuilder = injector.getInstance(ITagMapBuilder.class);
		final ILocationMap locationMap = locationMapBuilder.buildLocationMap(TEST_STYLESHEET);
		final ITagMap tagMap = tagMapBuilder.buildTagMap(TEST_STYLESHEET, locationMap);
		final IStylesheetStructure stylesheetStructure = extractor.extractStructure(TEST_STYLESHEET, locationMap,
				tagMap);

		// prepare the stylesheet information object and have it return the stylesheet structure
		final IStylesheetInformation stylesheetInfo = mock(IStylesheetInformation.class, "stylesheet info");
		lenient().when(stylesheetInfo.getStructure()).thenReturn(stylesheetStructure);
		lenient().when(stylesheetInfo.getLocationMap()).thenReturn(locationMap);
		lenient().when(stylesheetInfo.getTagMap()).thenReturn(tagMap);

		// prepare a stylesheet manager and have it return the stylesheet information when requested
		final IStylesheetManager stylesheetManager = mock(IStylesheetManager.class, "stylesheet manager");
		lenient().when(stylesheetManager.get(this.stylesheetURI)).thenReturn(stylesheetInfo);

		// finally, prepare the analyzer under test
		this.analyzer = new CoverageTraceAnalyzer(stylesheetManager);
	}

	protected Optional<ImmutableList<ITraceEvent>> mockTraceEvents() {
		// REMEMBER:
		// ENTER and LEAVE elements both refer to the END of the STARTING tag of the directive.
		// The choose-when elements always are reported with reference to the "when" location.
		// See also ProcessorObserverExecutionTest.
		final Optional<ImmutableList<ITraceEvent>> traceEvents = Optional.of(ImmutableList.of(
				mockEvent(ExecutionEventType.ENTER, 4, 2, 104, "template"),
				mockEvent(ExecutionEventType.ENTER, 6, 4, 148, "choose"),
				mockEvent(ExecutionEventType.ENTER, 7, 5, 175, "element"),
				mockEvent(ExecutionEventType.ENTER, 7, 5, 179, "text"),
				mockEvent(ExecutionEventType.LEAVE, 7, 5, 179, "text"),
				mockEvent(ExecutionEventType.LEAVE, 7, 5, 175, "element"),
				mockEvent(ExecutionEventType.LEAVE, 6, 4, 148, "choose"),
				mockEvent(ExecutionEventType.LEAVE, 4, 2, 104, "template")));
		return traceEvents;
	}

	private IExecutionTraceEvent mockEvent(ExecutionEventType type, int line, int column, int offset, String element) {
		final IExecutionTraceEvent event = mock(
				String.format("%s %s in line %d, column %d, offset %d", type, element, line, column, offset));
		lenient().when(event.getEventType()).thenReturn(type);
		switch (type) {
		case ENTER:
			lenient().when(event.isEnterEvent()).thenReturn(true);
			lenient().when(event.isLeaveEvent()).thenReturn(false);
			break;
		case LEAVE:
			lenient().when(event.isEnterEvent()).thenReturn(false);
			lenient().when(event.isLeaveEvent()).thenReturn(true);
			break;
		}
		lenient().when(event.getExecutedElement()).thenReturn(Optional.of(element));
		final PolymorphLocation location = mock(
				String.format("location of event %s %s in l%d/c%d=ch%d",
						type, element, line, column, offset));
		lenient().when(location.getLineNumber()).thenReturn(line);
		lenient().when(location.getColumnNumber()).thenReturn(column);
		lenient().when(location.getCharacterOffset()).thenReturn(offset);
		lenient().when(event.getElementLocation()).thenReturn(location);
		return event;
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#analyzeDocument(java.util.UUID, org.x2vc.processor.IHTMLDocumentContainer)}
	 * and {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#getDirectiveCoverage()}.
	 */
	@Test
	void testGetDirectiveCoverage() {
		this.analyzer.analyzeDocument(this.htmlContainer);

		final ImmutableList<IDirectiveCoverage> coverage = this.analyzer.getDirectiveCoverage(this.stylesheetURI);
		assertNotNull(coverage);
		assertDirectiveEquals("stylesheet", 2, 16, 0, CoverageStatus.PARTIAL, coverage.get(0));
		assertDirectiveEquals("template", 3, 15, 1, CoverageStatus.PARTIAL, coverage.get(1));
		assertDirectiveEquals("choose", 4, 14, 0, CoverageStatus.PARTIAL, coverage.get(2));
		assertDirectiveEquals("when", 5, 7, 3, CoverageStatus.FULL, coverage.get(3));
		assertDirectiveEquals("when", 8, 10, 0, CoverageStatus.NONE, coverage.get(4));
		assertDirectiveEquals("otherwise", 11, 13, 0, CoverageStatus.NONE, coverage.get(5));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#analyzeDocument(java.util.UUID, org.x2vc.processor.IHTMLDocumentContainer)}
	 * and {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#getLineCoverage()}.
	 */
	@Test
	void testGetLineCoverage() {
		this.analyzer.analyzeDocument(this.htmlContainer);

		final CoverageStatus[] coverage = this.analyzer.getLineCoverage(this.stylesheetURI);
		assertNotNull(coverage);
		assertEquals(CoverageStatus.PARTIAL, coverage[0], "coverage of line 0 (<?xml version=\"1.0\"?>)");
		assertEquals(CoverageStatus.PARTIAL, coverage[1], "coverage of line 1 (empty line)");
		assertEquals(CoverageStatus.PARTIAL, coverage[2], "coverage of line 2 (<xsl:stylesheet ...>)");
		assertEquals(CoverageStatus.PARTIAL, coverage[3], "coverage of line 3 (<xsl:template name=\"foo\">)");
		assertEquals(CoverageStatus.PARTIAL, coverage[4], "coverage of line 4 (<xsl:choose>)");
		assertEquals(CoverageStatus.FULL, coverage[5], "coverage of line 5 (<xsl:when test=\"fooA\">)");
		assertEquals(CoverageStatus.FULL, coverage[6], "coverage of line 6 (<p>test A</p>)");
		assertEquals(CoverageStatus.FULL, coverage[7], "coverage of line 7 (</xsl:when>)");
		assertEquals(CoverageStatus.NONE, coverage[8], "coverage of line 8 (<xsl:when test=\"fooB\">)");
		assertEquals(CoverageStatus.NONE, coverage[9], "coverage of line 9 (<p>test B</p>)");
		assertEquals(CoverageStatus.NONE, coverage[10], "coverage of line 10 (</xsl:when>)");
		assertEquals(CoverageStatus.NONE, coverage[11], "coverage of line 11 (<xsl:otherwise>)");
		assertEquals(CoverageStatus.NONE, coverage[12], "coverage of line 12 (<p>test O</p>)");
		assertEquals(CoverageStatus.NONE, coverage[13], "coverage of line 13 (</xsl:otherwise>)");
		assertEquals(CoverageStatus.PARTIAL, coverage[14], "coverage of line 14 (</xsl:choose>)");
		assertEquals(CoverageStatus.PARTIAL, coverage[15], "coverage of line 15 (</xsl:template>)");
		assertEquals(CoverageStatus.PARTIAL, coverage[16], "coverage of line 16 (</xsl:stylesheet>)");
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#getStatistics(URI)}.
	 */
	@Test
	void testGetStatistics() {
		this.analyzer.analyzeDocument(this.htmlContainer);
		final ICoverageStatistics statistics = this.analyzer.getStatistics(this.stylesheetURI);

		assertEquals(6, statistics.getTotalDirectiveCount(), "TotalDirectiveCount");
		assertEquals(1, statistics.getDirectiveCountWithFullCoverage(), "DirectiveCountWithFullCoverage");
		assertEquals(3, statistics.getDirectiveCountWithPartialCoverage(), "DirectiveCountWithPartialCoverage");
		assertEquals(2, statistics.getDirectiveCountWithNoCoverage(), "DirectiveCountWithNoCoverage");

		assertEquals(17, statistics.getTotalLineCount(), "TotalLineCount");
		assertEquals(0, statistics.getLineCountEmpty(), "LineCountEmpty");
		assertEquals(3, statistics.getLineCountWithFullCoverage(), "LineCountWithFullCoverage");
		assertEquals(8, statistics.getLineCountWithPartialCoverage(), "LineCountWithPartialCoverage");
		assertEquals(6, statistics.getLineCountWithNoCoverage(), "LineCountWithNoCoverage");
	}

	private void assertDirectiveEquals(final String directiveName, final int startLine, final int endLine,
			final int executionCount, CoverageStatus coverage, final IDirectiveCoverage entry) {
		assertEquals(directiveName, entry.getDirective().getName(),
				String.format("directive name in line %d", startLine));
		assertEquals(executionCount, entry.getExecutionCount(),
				String.format("execution count of directive %s in line %d", directiveName, startLine));
		assertEquals(coverage, entry.getCoverage(),
				String.format("coverage of directive %s in line %d", directiveName, startLine));
	}

}
