package org.x2vc.stylesheet.coverage;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.IExecutionTraceEvent;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ITraceEvent;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;
import org.x2vc.stylesheet.structure.StylesheetStructureExtractor;
import org.x2vc.utilities.PolymorphLocation;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
class CoverageTraceAnalyzerTest {

	private URI stylesheetURI;

	@Mock
	private IStylesheetManager stylesheetManager;

	@Mock
	private IStylesheetInformation stylesheetInfo;

	private IStylesheetStructureExtractor extractor;

	private IStylesheetStructure stylesheetStructure;

	@Mock
	private IXSLTDirectiveNode rootNode;

	@Mock
	private IHTMLDocumentContainer htmlContainer;

	private CoverageTraceAnalyzer analyzer;

	@Mock
	private IXMLDocumentContainer xmlContainer;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// wire up path from HTML document container to the stylesheet info object
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "myStylesheet");
		when(this.htmlContainer.getSource()).thenReturn(this.xmlContainer);
		when(this.xmlContainer.getStylesheeURI()).thenReturn(this.stylesheetURI);
		when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheetInfo);

		// TODO CoverageTraceAnalyzer: replace actual instance of StylesheetStructureExtractor with mock
		// (requires mocking of the entire stylesheet structure, which is A LOT of work)
		this.extractor = new StylesheetStructureExtractor();
		when(this.stylesheetInfo.getStructure()).thenAnswer(a -> this.stylesheetStructure);

		this.analyzer = new CoverageTraceAnalyzer(this.stylesheetManager);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#analyzeDocument(java.util.UUID, org.x2vc.processor.IHTMLDocumentContainer)}
	 * and {@link org.x2vc.stylesheet.coverage.CoverageTraceAnalyzer#getDirectiveCoverage()}.
	 */
	@Test
	void testGetDirectiveCoverage() {
		final String in = """
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
		this.stylesheetStructure = this.extractor.extractStructure(in);
		// REMEMBER:
		// ENTER and LEAVE elements both refer to the STARTING position of the directive.
		// The choose-when elements always are reported with reference to the "when" location.
		// See also ProcessorObserverExecutionTest.
		final Optional<ImmutableList<ITraceEvent>> traceEvents = Optional.of(ImmutableList.of(
				mockEvent(ExecutionEventType.ENTER, 3, "template"),
				mockEvent(ExecutionEventType.ENTER, 5, "choose"),

				mockEvent(ExecutionEventType.ENTER, 6, "element"),
				mockEvent(ExecutionEventType.ENTER, 6, "text"),
				mockEvent(ExecutionEventType.LEAVE, 6, "text"),
				mockEvent(ExecutionEventType.LEAVE, 6, "element"),
				mockEvent(ExecutionEventType.LEAVE, 5, "choose"),
				mockEvent(ExecutionEventType.LEAVE, 3, "template")));
		when(this.htmlContainer.getTraceEvents()).thenReturn(traceEvents);
		this.analyzer.analyzeDocument(this.htmlContainer);

		final ImmutableList<IDirectiveCoverage> coverage = this.analyzer.getDirectiveCoverage(this.stylesheetURI);
		assertNotNull(coverage);
		assertDirectiveEquals("stylesheet", 2, 16, 1, CoverageStatus.PARTIAL, coverage.get(0));
		assertDirectiveEquals("template", 3, 15, 0, CoverageStatus.PARTIAL, coverage.get(1));
		assertDirectiveEquals("choose", 4, 14, 1, CoverageStatus.PARTIAL, coverage.get(2));
		assertDirectiveEquals("when", 5, 7, 2, CoverageStatus.FULL, coverage.get(3));
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
		final String in = """
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
		this.stylesheetStructure = this.extractor.extractStructure(in);
		// REMEMBER:
		// ENTER and LEAVE elements both refer to the STARTING position of the directive.
		// The choose-when elements always are reported with reference to the "when" location.
		// See also ProcessorObserverExecutionTest.
		final Optional<ImmutableList<ITraceEvent>> traceEvents = Optional.of(ImmutableList.of(
				mockEvent(ExecutionEventType.ENTER, 3, "template"),
				mockEvent(ExecutionEventType.ENTER, 5, "choose"),

				mockEvent(ExecutionEventType.ENTER, 6, "element"),
				mockEvent(ExecutionEventType.ENTER, 6, "text"),
				mockEvent(ExecutionEventType.LEAVE, 6, "text"),
				mockEvent(ExecutionEventType.LEAVE, 6, "element"),
				mockEvent(ExecutionEventType.LEAVE, 5, "choose"),
				mockEvent(ExecutionEventType.LEAVE, 3, "template")));
		when(this.htmlContainer.getTraceEvents()).thenReturn(traceEvents);
		this.analyzer.analyzeDocument(this.htmlContainer);

		final CoverageStatus[] coverage = this.analyzer.getLineCoverage(this.stylesheetURI);
		assertNotNull(coverage);
		assertEquals(CoverageStatus.PARTIAL, coverage[0]); // <?xml version="1.0"?>
		assertEquals(CoverageStatus.PARTIAL, coverage[1]); // <xsl:stylesheet ...>
		assertEquals(CoverageStatus.PARTIAL, coverage[2]); // <xsl:template name="foo">
		assertEquals(CoverageStatus.PARTIAL, coverage[3]); // <xsl:choose>
		assertEquals(CoverageStatus.FULL, coverage[4]); // <xsl:when test="fooA">
		assertEquals(CoverageStatus.FULL, coverage[5]); // <p>test A</p>
		assertEquals(CoverageStatus.FULL, coverage[6]); // </xsl:when>
		assertEquals(CoverageStatus.NONE, coverage[7]); // <xsl:when test="fooB">
		assertEquals(CoverageStatus.NONE, coverage[8]); // <p>test B</p>
		assertEquals(CoverageStatus.NONE, coverage[9]); // </xsl:when>
		assertEquals(CoverageStatus.NONE, coverage[10]); // <xsl:otherwise>
		assertEquals(CoverageStatus.NONE, coverage[11]); // <p>test O</p>
		assertEquals(CoverageStatus.NONE, coverage[12]); // </xsl:otherwise>
		assertEquals(CoverageStatus.PARTIAL, coverage[13]); // </xsl:choose>
		assertEquals(CoverageStatus.PARTIAL, coverage[14]); // </xsl:template>
		assertEquals(CoverageStatus.PARTIAL, coverage[15]); // </xsl:stylesheet>
	}

	private IExecutionTraceEvent mockEvent(ExecutionEventType type, int line, String element) {
		final IExecutionTraceEvent event = mock(String.format("%s %s in line %d", type, element, line));
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
				String.format("location of event %s %s in line %d", type, element, line));
		lenient().when(location.getLineNumber()).thenReturn(line);
		lenient().when(event.getElementLocation()).thenReturn(location);
		return event;
	}

	private void assertDirectiveEquals(final String directiveName, final int startLine, final int endLine,
			final int executionCount, CoverageStatus coverage, final IDirectiveCoverage entry) {
		assertEquals(directiveName, entry.getDirective().getName());
		assertEquals(startLine, entry.getStartLocation().getLineNumber());
		assertEquals(endLine, entry.getEndLocation().getLineNumber());
		assertEquals(executionCount, entry.getExecutionCount());
		assertEquals(coverage, entry.getCoverage());
	}

}
