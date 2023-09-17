package org.x2vc.processor;

import static org.mockito.Mockito.lenient;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IXMLDocumentContainer;

@ExtendWith(MockitoExtension.class)
class HTMLDocumentContainerTest {

	@Mock
	private IXMLDocumentContainer source;

	@Mock
	private IStylesheetManager stylesheetManager;

	private URI stylesheetURI;

	@Mock
	private IStylesheetInformation stylesheet;

	@Mock
	private IStylesheetStructure structure;

	private IStylesheetCoverage coverage;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		lenient().when(this.source.getStylesheeURI()).thenReturn(this.stylesheetURI);
		lenient().when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheet);
		lenient().when(this.stylesheet.getStructure()).thenReturn(this.structure);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.HTMLDocumentContainer#getCoverage()}.
	 */
	@Test
	void testGetCoverage() {
		// TODO XSLT Coverage: rebuild after structure extraction changes
//
//		final XSLTDirectiveNode directive1 = new XSLTDirectiveNode.Builder(this.structure, "foo")
//			.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "1").build();
//		final XSLTDirectiveNode directive2 = new XSLTDirectiveNode.Builder(this.structure, "bar")
//			.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "2").build();
//		final XSLTDirectiveNode directive3 = new XSLTDirectiveNode.Builder(this.structure, "baz")
//			.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "3").build();
//		lenient().when(this.structure.getDirectiveByTraceID(1)).thenReturn(directive1);
//		lenient().when(this.structure.getDirectiveByTraceID(2)).thenReturn(directive2);
//		lenient().when(this.structure.getDirectiveByTraceID(3)).thenReturn(directive3);
//		when(this.structure.getDirectivesWithTraceID())
//			.thenReturn(ImmutableList.of(directive1, directive2, directive3));
//
//		this.coverage = new StylesheetCoverage(this.structure);
//		lenient().when(this.stylesheet.createCoverageStatistics()).thenReturn(this.coverage);
//
//		final ImmutableList<ITraceEvent> traceEvents = ImmutableList.of(new TraceEvent(1, "foo"),
//				new TraceEvent(1, "foo"), new TraceEvent(2, "bar"));
//
//		final HTMLDocumentContainer dc = new HTMLDocumentContainer(this.stylesheetManager, this.source, "<html></html>",
//				null, null, traceEvents);
//
//		assertTrue(dc.getCoverage().isPresent());
//		final IStylesheetCoverage coverage = dc.getCoverage().get();
//
//		assertEquals(2, coverage.getElementCoverage(1)); // foo
//		assertEquals(1, coverage.getElementCoverage(2)); // bar
//		assertEquals(0, coverage.getElementCoverage(3)); // baz - not present in the trace events
	}

}
