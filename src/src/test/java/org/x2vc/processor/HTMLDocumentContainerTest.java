package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.common.ExtendedXSLTConstants;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.stylesheet.coverage.StylesheetCoverage;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.XSLTDirectiveNode;
import org.x2vc.xml.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class HTMLDocumentContainerTest {

	@Mock
	private IXMLDocumentContainer source;

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
		lenient().when(this.source.getStylesheet()).thenReturn(this.stylesheet);
		lenient().when(this.stylesheet.getStructure()).thenReturn(this.structure);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.HTMLDocumentContainer#getCoverage()}.
	 */
	@Test
	void testGetCoverage() {
		XSLTDirectiveNode directive1 = new XSLTDirectiveNode.Builder(this.structure, "foo")
				.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "1").build();
		XSLTDirectiveNode directive2 = new XSLTDirectiveNode.Builder(this.structure, "bar")
				.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "2").build();
		XSLTDirectiveNode directive3 = new XSLTDirectiveNode.Builder(this.structure, "baz")
				.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "3").build();
		lenient().when(this.structure.getDirectiveByTraceID(1)).thenReturn(directive1);
		lenient().when(this.structure.getDirectiveByTraceID(2)).thenReturn(directive2);
		lenient().when(this.structure.getDirectiveByTraceID(3)).thenReturn(directive3);
		when(this.structure.getDirectivesWithTraceID())
				.thenReturn(ImmutableList.of(directive1, directive2, directive3));

		this.coverage = new StylesheetCoverage(this.structure);
		lenient().when(this.stylesheet.createCoverageStatistics()).thenReturn(this.coverage);

		List<ITraceEvent> traceEvents = Lists.newArrayList(new TraceEvent(1, "foo"), new TraceEvent(1, "foo"),
				new TraceEvent(2, "bar"));
		HTMLDocumentContainer dc = new HTMLDocumentContainer.Builder(this.source).withHtmlDocument("<html></html>")
				.withTraceEvents(traceEvents).build();

		assertTrue(dc.getCoverage().isPresent());
		IStylesheetCoverage coverage = dc.getCoverage().get();

		assertEquals(2, coverage.getElementCoverage(1)); // foo
		assertEquals(1, coverage.getElementCoverage(2)); // bar
		assertEquals(0, coverage.getElementCoverage(3)); // baz - not present in the trace events
	}

}
