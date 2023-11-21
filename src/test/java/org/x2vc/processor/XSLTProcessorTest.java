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
package org.x2vc.processor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IStylesheetParameter;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IStylesheetParameterValue;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmValue;

@ExtendWith(MockitoExtension.class)
class XSLTProcessorTest {

	private XSLTProcessor wrapper;

	@Mock
	private IStylesheetManager stylesheetManager;

	@Mock
	private ISchemaManager schemaManager;

	@Mock
	private IStylesheetInformation stylesheet;

	@Mock
	private IXMLDocumentContainer xmlDocument;

	@Mock
	private IXMLDocumentDescriptor xmlDescriptor;

	private List<IStylesheetParameterValue> parameterValues;

	private URI stylesheetURI;
	private URI schemaURI;
	private int schemaVersion;

	@Mock
	private IXMLSchema schema;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.wrapper = new XSLTProcessor(this.stylesheetManager, this.schemaManager, 25);

		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		lenient().when(this.xmlDocument.getStylesheeURI()).thenReturn(this.stylesheetURI);
		lenient().when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheet);

		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		lenient().when(this.xmlDocument.getSchemaURI()).thenReturn(this.schemaURI);
		this.schemaVersion = 1;
		lenient().when(this.xmlDocument.getSchemaVersion()).thenReturn(this.schemaVersion);
		lenient().when(this.xmlDocument.getDocumentDescriptor()).thenReturn(this.xmlDescriptor);
		lenient().when(this.xmlDescriptor.getExtensionFunctionResults()).thenReturn(ImmutableList.of());

		this.parameterValues = Lists.newArrayList();
		lenient().when(this.xmlDescriptor.getStylesheetParameterValues())
			.thenAnswer(a -> ImmutableSet.copyOf(this.parameterValues));

		lenient().when(this.schemaManager.getSchema(this.stylesheetURI)).thenReturn(this.schema);
		lenient().when(this.schemaManager.getSchema(this.stylesheetURI, this.schemaVersion)).thenReturn(this.schema);

		lenient().when(this.schema.getExtensionFunctions()).thenReturn(ImmutableList.of());

	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testBasicProcessing() {
		final String xslt = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template match="root">
							<html><body><ul>
							<xsl:apply-templates/>
							</ul></body></html>
							</xsl:template>
							<xsl:template match="elem">
							<li>
							<xsl:value-of select="@name"/>
							<xsl:message>some unrelated text that may not cause trouble</xsl:message>
							</li>
							</xsl:template>
							</xsl:stylesheet>
							""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);
		final String input = """
								<?xml version="1.0"?>
								<root>
								<elem name="abc"/>
								<elem name="def"/>
								<elem name="ghi"/>
								</root>
								""";
		when(this.xmlDocument.getDocument()).thenReturn(input);

		final IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertFalse(htmlDocument.getCompilationError().isPresent());
		assertFalse(htmlDocument.getProcessingError().isPresent());
		assertTrue(htmlDocument.getDocument().isPresent());

		final String document = htmlDocument.getDocument().get();
		// only check a snippet - after all, we're not here to test the correct function
		// of the XSLT processor itself
		assertTrue(document.contains("<li>abc</li>"));

		// TODO #39 re-enable once new trace listener is complete
//		assertTrue(htmlDocument.getTraceEvents().isPresent());
//		final ImmutableList<ITraceEvent> traceEvents = htmlDocument.getTraceEvents().get();
//		assertEquals(
//				ImmutableList.of(new TraceEvent(1, "template"), new TraceEvent(2, "apply-templates"),
//						new TraceEvent(3, "template"), new TraceEvent(3, "template"), new TraceEvent(3, "template")),
//				traceEvents);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testCompilationError() {
		final String xslt = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:this-is-not-a-valid-stylesheet/>
							</xsl:stylesheet>
							""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);

		final IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertTrue(htmlDocument.getCompilationError().isPresent());
		assertFalse(htmlDocument.getProcessingError().isPresent());
		assertFalse(htmlDocument.getDocument().isPresent());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testProcessingError() {
		final String xslt = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template match="root">
							</xsl:template>
							</xsl:stylesheet>
							""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);
		final String input = """
								<?xml version="1.0"?>
								<foobar>
								<elem noName="abc"/>
								<elem whatName="def"/>
								</root>
								""";
		when(this.xmlDocument.getDocument()).thenReturn(input);

		final IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertFalse(htmlDocument.getCompilationError().isPresent());
		assertTrue(htmlDocument.getProcessingError().isPresent());
		assertFalse(htmlDocument.getDocument().isPresent());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.processor.XSLTProcessor#processDocument(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testParameterValue() {
		final String xslt = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:param name="myParam" select="0"/>
							<xsl:template match="root">
							<div>
							<xsl:value-of select="$myParam"/>
							</div>
							</xsl:template>
							</xsl:stylesheet>
							""";
		when(this.stylesheet.getPreparedStylesheet()).thenReturn(xslt);
		final String input = """
								<?xml version="1.0"?>
								<root />
								""";
		when(this.xmlDocument.getDocument()).thenReturn(input);

		final UUID parameterID = UUID.randomUUID();
		final IStylesheetParameterValue paramValue = mock();
		when(paramValue.getParameterID()).thenReturn(parameterID);
		when(paramValue.getXDMValue()).thenReturn(XdmValue.makeValue("FooBarBaz"));
		this.parameterValues.add(paramValue);

		final IStylesheetParameter parameterDefinition = mock();
		when(parameterDefinition.getQualifiedName()).thenReturn(new QName("myParam"));
		when(this.schema.getObjectByID(parameterID, IStylesheetParameter.class)).thenReturn(parameterDefinition);

		final IHTMLDocumentContainer htmlDocument = this.wrapper.processDocument(this.xmlDocument);

		assertFalse(htmlDocument.getCompilationError().isPresent());
		assertFalse(htmlDocument.getProcessingError().isPresent());
		assertTrue(htmlDocument.getDocument().isPresent());

		final String document = htmlDocument.getDocument().get();
		assertTrue(document.contains("<div>FooBarBaz</div>"));
	}

}
