package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.xmldoc.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.Processor;

@ExtendWith(MockitoExtension.class)
class XSLTProcessorTest {

	// This series of tests uses the actual Saxon XSLT processor. Using a mocked
	// version would be more stable, but it's a lot of work...
	// TODO XSLT: provide tests that do not require Saxon

	private Processor saxonProcessor;
	private XSLTProcessor wrapper;

	@Mock
	private IXMLDocumentContainer xmlDocument;

	@Mock
	private IStylesheetInformation stylesheet;

	@Mock
	private IXMLSchema schema;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.saxonProcessor = new Processor();
		this.wrapper = new XSLTProcessor(this.saxonProcessor);
		// wire up interconnected mock instances
		lenient().when(this.xmlDocument.getStylesheet()).thenReturn(this.stylesheet);
		lenient().when(this.xmlDocument.getSchema()).thenReturn(this.schema);
		lenient().when(this.schema.getStylesheet()).thenReturn(this.stylesheet);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xmldoc.IXMLDocumentContainer)}.
	 */
	@Test
	void testBasicProcessing() {
		String xslt = """
						<?xml version="1.0"?>
						<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
						<xsl:template match="root" ext0:trace-id="1">
						<xsl:message><ext0:trace element="template" trace-id="1"/></xsl:message>
						<html><body><ul>
						<xsl:message><ext0:trace element="apply-templates" trace-id="2"/></xsl:message>
						<xsl:apply-templates ext0:trace-id="2"/>
						</ul></body></html>
						</xsl:template>
						<xsl:template match="elem" ext0:trace-id="3">
						<xsl:message><ext0:trace element="template" trace-id="3"/></xsl:message>
						<li>
						<xsl:value-of select="@name"/>
						<xsl:message>some unrelated text that may not cause trouble</xsl:message>
						</li>
						</xsl:template>
						</xsl:stylesheet>
						""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);
		String input = """
						<?xml version="1.0"?>
						<root>
						<elem name="abc"/>
						<elem name="def"/>
						<elem name="ghi"/>
						</root>
						""";
		when(this.xmlDocument.getDocument()).thenReturn(input);

		IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertFalse(htmlDocument.getCompilationError().isPresent());
		assertFalse(htmlDocument.getProcessingError().isPresent());
		assertTrue(htmlDocument.getDocument().isPresent());

		String document = htmlDocument.getDocument().get();
		// only check a snippet - after all, we're not here to test the correct function
		// of the XSLT processor itself
		assertTrue(document.contains("<li>abc</li>"));

		assertTrue(htmlDocument.getTraceEvents().isPresent());
		ImmutableList<ITraceEvent> traceEvents = htmlDocument.getTraceEvents().get();
		assertEquals(
				ImmutableList.of(new TraceEvent(1, "template"), new TraceEvent(2, "apply-templates"),
						new TraceEvent(3, "template"), new TraceEvent(3, "template"), new TraceEvent(3, "template")),
				traceEvents);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xmldoc.IXMLDocumentContainer)}.
	 */
	@Test
	void testCompilationError() {
		String xslt = """
						<?xml version="1.0"?>
						<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
						<xsl:this-is-not-a-valid-stylesheet/>
						</xsl:stylesheet>
						""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);

		IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertTrue(htmlDocument.getCompilationError().isPresent());
		assertFalse(htmlDocument.getProcessingError().isPresent());
		assertFalse(htmlDocument.getDocument().isPresent());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xmldoc.IXMLDocumentContainer)}.
	 */
	@Test
	void testProcessingError() {
		String xslt = """
						<?xml version="1.0"?>
						<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
						<xsl:template match="root" ext0:trace-id="1">
						</xsl:template>
						</xsl:stylesheet>
						""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);
		String input = """
						<?xml version="1.0"?>
						<foobar>
						<elem noName="abc"/>
						<elem whatName="def"/>
						</root>
						""";
		when(this.xmlDocument.getDocument()).thenReturn(input);

		IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertFalse(htmlDocument.getCompilationError().isPresent());
		assertTrue(htmlDocument.getProcessingError().isPresent());
		assertFalse(htmlDocument.getDocument().isPresent());
	}

}