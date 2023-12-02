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
package org.x2vc.stylesheet.structure;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.xml.stream.Location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.xml.ILocationMap;
import org.x2vc.utilities.xml.ITagInfo;
import org.x2vc.utilities.xml.ITagMap;
import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
class StylesheetStructureExtractorTest {

	@Mock
	ILocationMap locationMap;

	@Mock
	ITagMap tagMap;

	IStylesheetStructureExtractor extractor;

	@BeforeEach
	void setup() {
		this.extractor = new StylesheetStructureExtractor();
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_SingleTemplate() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="bar">
							<span>foobar&apos;<![CDATA[Some<>Content]]></span>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		final ITagInfo tagInfoSpan = mockTagInfo(4, 7, 135);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode templateNode = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, templateNode);
		final IXSLTDirectiveNode templateNodeAsDirective = (IXSLTDirectiveNode) templateNode;
		assertEquals("template", templateNodeAsDirective.getName());
		assertSame(tagInfoTemplate, templateNodeAsDirective.getTagInformation());
		assertEquals("bar", templateNodeAsDirective.getXSLTAttributes().get("name"));
		assertEquals(1, templateNodeAsDirective.getChildElements().size());
		assertEquals(3, templateNodeAsDirective.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(5, templateNodeAsDirective.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode spanNode = templateNodeAsDirective.getChildElements().get(0);
		assertInstanceOf(IXMLNode.class, spanNode);
		final IXMLNode spanNodeAsXML = (IXMLNode) spanNode;
		assertEquals("span", spanNodeAsXML.getName().getLocalPart());
		assertSame(tagInfoSpan, spanNodeAsXML.getTagInformation());
		assertEquals(4, spanNodeAsXML.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(4, spanNodeAsXML.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_SingleTemplateWithNestedDirectives() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="bar">
							<span>foo <xsl:value-of select="bar"/> baz</span>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoSpan = mockTagInfo(4, 7, 135);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		final ITagInfo tagInfoValueOf = mockTagInfo(4, 39, 167);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode templateNode = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, templateNode);
		final IXSLTDirectiveNode templateNodeAsDirective = (IXSLTDirectiveNode) templateNode;
		assertEquals("template", templateNodeAsDirective.getName());
		assertSame(tagInfoTemplate, templateNodeAsDirective.getTagInformation());
		assertEquals("bar", templateNodeAsDirective.getXSLTAttributes().get("name"));
		assertEquals(1, templateNodeAsDirective.getChildElements().size());
		assertEquals(3, templateNodeAsDirective.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(5, templateNodeAsDirective.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode spanNode = templateNodeAsDirective.getChildElements().get(0);
		assertInstanceOf(IXMLNode.class, spanNode);
		final IXMLNode spanNodeAsXML = (IXMLNode) spanNode;
		assertEquals("span", spanNodeAsXML.getName().getLocalPart());
		assertSame(tagInfoSpan, spanNodeAsXML.getTagInformation());
		assertEquals(4, spanNodeAsXML.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(4, spanNodeAsXML.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IStructureTreeNode> spanChildren = spanNodeAsXML.getChildElements();
		assertEquals(3, spanChildren.size());

		final IStructureTreeNode spanChild1 = spanChildren.get(0);
		assertInstanceOf(ITextNode.class, spanChild1);
		assertEquals("foo ", ((ITextNode) spanChild1).getText());

		final IStructureTreeNode spanChild2 = spanChildren.get(1);
		assertInstanceOf(IXSLTDirectiveNode.class, spanChild2);
		final IXSLTDirectiveNode spanChild2AsDirective = (IXSLTDirectiveNode) spanChild2;
		assertEquals("value-of", spanChild2AsDirective.getName());
		assertSame(tagInfoValueOf, spanChild2AsDirective.getTagInformation());
		assertEquals("bar", spanChild2AsDirective.getXSLTAttributes().get("select"));
		assertEquals(4, spanChild2AsDirective.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(4, spanChild2AsDirective.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode spanChild3 = spanChildren.get(2);
		assertInstanceOf(ITextNode.class, spanChild3);
		assertEquals(" baz", ((ITextNode) spanChild3).getText());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TopLevelParameters_LocalNameOnly() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:param name="foo" select="bar"/>
							<xsl:template name="bar">
							<p>foobar&apos;<![CDATA[Some<>Content]]></p>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoParam = mockTagInfo(3, 37, 139);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoTemplate = mockTagInfo(4, 26, 165);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoP = mockTagInfo(5, 4, 169);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoParam, param.getTagInformation());
		assertEquals("foo", param.getLocalName());
		assertFalse(param.getNamespaceURI().isPresent());
		assertEquals(3, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(3, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TopLevelParameters_WithNamespaceLocally() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:param name="foo:bar" select="baz" xmlns:foo="http://foo.bar.baz"/>
							<xsl:template name="bar">
							<p>foobar&apos;<![CDATA[Some<>Content]]></p>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoParam = mockTagInfo(3, 72, 174);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoTemplate = mockTagInfo(4, 26, 200);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoP = mockTagInfo(5, 4, 204);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoParam, param.getTagInformation());
		assertEquals("bar", param.getLocalName());
		assertTrue(param.getNamespaceURI().isPresent());
		assertEquals("http://foo.bar.baz", param.getNamespaceURI().orElseThrow());
		assertEquals(3, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(3, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TopLevelParameters_WithNamespaceParent() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:foo="http://foo.bar.baz">
							<xsl:param name="foo:bar" select="baz"/>
							<xsl:template name="bar">
							<p>foobar&apos;<![CDATA[Some<>Content]]></p>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 111, 133);
		final ITagInfo tagInfoParam = mockTagInfo(3, 41, 174);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoTemplate = mockTagInfo(4, 26, 200);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoP = mockTagInfo(5, 4, 204);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(111, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoParam, param.getTagInformation());
		assertEquals("bar", param.getLocalName());
		assertTrue(param.getNamespaceURI().isPresent());
		assertEquals("http://foo.bar.baz", param.getNamespaceURI().orElseThrow());
		assertEquals(3, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(3, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithFormalParameters() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="bar">
							<xsl:param name="foo" select="bar"/>
							<p>foobar&apos;<![CDATA[Some<>Content]]></p>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		final ITagInfo tagInfoParam = mockTagInfo(4, 37, 165);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoP = mockTagInfo(5, 4, 169);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertSame(tagInfoTemplate, child1d.getTagInformation());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, child1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1d.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoParam, param.getTagInformation());
		assertEquals("foo", param.getLocalName());
		assertEquals(4, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(4, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithActualParametersAsAttribute() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="bar">
							<xsl:param name="foo" select="bar"/>
							<xsl:call-template name="bar">
							<xsl:with-param name="foo" select="baz"/>
							</xsl:call-template>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoParam = mockTagInfo(4, 37, 165);
		final ITagInfo tagInfoCallTemplate = mockTagInfo(5, 31, 196);
		final ITagInfo tagInfoWithParam = mockTagInfo(6, 42, 238);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertSame(tagInfoTemplate, child1d.getTagInformation());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("call-template", child1_1d.getName());
		assertSame(tagInfoCallTemplate, child1_1d.getTagInformation());
		assertEquals(5, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1_1d.getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoWithParam, param.getTagInformation());
		assertEquals("foo", param.getLocalName());
		assertTrue(param.getSelection().isPresent());
		assertEquals("baz", param.getSelection().get());
		assertEquals(6, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithActualParametersAsText() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="bar">
							<xsl:param name="foo"/>
							<xsl:call-template name="bar">
							<xsl:with-param name="foo">baz</xsl:with-param>
							</xsl:call-template>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoParam = mockTagInfo(4, 24, 152);
		final ITagInfo tagInfoCallTemplate = mockTagInfo(5, 31, 183);
		final ITagInfo tagInfoWithParam = mockTagInfo(6, 28, 211);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertSame(tagInfoTemplate, child1d.getTagInformation());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("call-template", child1_1d.getName());
		assertSame(tagInfoCallTemplate, child1_1d.getTagInformation());
		assertEquals(5, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1_1d.getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertSame(tagInfoWithParam, param.getTagInformation());
		assertEquals("foo", param.getLocalName());
		assertFalse(param.getSelection().isPresent());
		assertEquals(1, param.getChildElements().size());
		assertInstanceOf(ITextNode.class, param.getChildElements().get(0));
		assertEquals("baz", ((ITextNode) param.getChildElements().get(0)).getText());
		assertEquals(6, param.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, param.getEndLocation().orElseThrow().getLineNumber());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_ForEachWithSort() {
		final String in = """
							<?xml version="1.0"?>
							<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template name="foo">
							<xsl:for-each select="bar">
							<xsl:sort select="baz" lang="bazlang" data-type="baztype" order="descending" case-order="lower-first" />
							<p>test</p>
							</xsl:for-each>
							</xsl:template>
							</xsl:stylesheet>
							""";

		final ITagInfo tagInfoStylesheet = mockTagInfo(2, 80, 102);
		final ITagInfo tagInfoTemplate = mockTagInfo(3, 26, 128);
		final ITagInfo tagInfoForEach = mockTagInfo(4, 28, 156);
		final ITagInfo tagInfoSort = mockTagInfo(5, 105, 261);
		@SuppressWarnings("unused")
		final ITagInfo tagInfoP = mockTagInfo(6, 4, 265);

		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in, this.locationMap, this.tagMap)
			.getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertSame(tagInfoStylesheet, rootNode.getTagInformation());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertSame(tagInfoTemplate, child1d.getTagInformation());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("for-each", child1_1d.getName());
		assertSame(tagInfoForEach, child1_1d.getTagInformation());
		assertEquals("bar", child1_1d.getXSLTAttributes().get("select"));
		assertEquals(4, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTSortNode> sortNodes = child1_1d.getSorting();
		assertEquals(1, sortNodes.size());
		final IXSLTSortNode sortNode = sortNodes.get(0);
		assertSame(tagInfoSort, sortNode.getTagInformation());
		assertTrue(sortNode.getSortingExpression().isPresent());
		assertEquals("baz", sortNode.getSortingExpression().get());
		assertTrue(sortNode.getLanguage().isPresent());
		assertEquals("bazlang", sortNode.getLanguage().get());
		assertTrue(sortNode.getDataType().isPresent());
		assertEquals("baztype", sortNode.getDataType().get());
		assertTrue(sortNode.getSortOrder().isPresent());
		assertEquals("descending", sortNode.getSortOrder().get());
		assertTrue(sortNode.getCaseOrder().isPresent());
		assertEquals("lower-first", sortNode.getCaseOrder().get());
		assertEquals(5, sortNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(5, sortNode.getEndLocation().orElseThrow().getLineNumber());
	}

	private ITagInfo mockTagInfo(int line, int column, int offset) {
		final PolymorphLocation mockLocation = mock(PolymorphLocation.class,
				String.format("mock for location l = %d, c = %d, o = %d", line, column, offset));
		lenient().when(mockLocation.getLineNumber()).thenReturn(line);
		lenient().when(mockLocation.getColumnNumber()).thenReturn(column);
		lenient().when(mockLocation.getCharacterOffset()).thenReturn(offset);
		lenient().when(mockLocation.compareTo(any(PolymorphLocation.class)))
			.thenAnswer(innerInvocation -> {
				return Integer.compare(offset,
						((PolymorphLocation) innerInvocation.getArgument(0)).getCharacterOffset());
			});
		final LocationMatcher matcher = new LocationMatcher(line, column, offset);
		lenient().doReturn(mockLocation).when(this.locationMap).getLocationByOffset(argThat(matcher));
		lenient().doReturn(mockLocation).when(this.locationMap).getLocationByLineColumn(argThat(matcher));

		final ITagInfo mockTagInfo = mock(ITagInfo.class,
				String.format("mock for tag at l = %d, c = %d, o = %d", line, column, offset));
		// an offset of -1 must be used (see comments in extractor implementation)
		when(this.tagMap.getTag(mockLocation, -1)).thenReturn(Optional.of(mockTagInfo));

		return mockTagInfo;
	}

	private class LocationMatcher implements ArgumentMatcher<javax.xml.stream.Location> {

		private int lineNumber;
		private int columnNumber;
		private int offset;

		protected LocationMatcher(int lineNumber, int columnNumber, int offset) {
			super();
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.offset = offset;
		}

		@Override
		public boolean matches(Location location) {
			return (location.getLineNumber() == this.lineNumber) &&
					(location.getColumnNumber() == this.columnNumber) &&
					(location.getCharacterOffset() == this.offset);
		}

	}

}
