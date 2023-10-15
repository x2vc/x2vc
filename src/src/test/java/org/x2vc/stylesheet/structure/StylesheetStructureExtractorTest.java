package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

class StylesheetStructureExtractorTest {

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
							<p>foobar&apos;<![CDATA[Some<>Content]]></p>
							</xsl:template>
							</xsl:stylesheet>
							""";
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertEquals("bar", child1d.getXSLTAttributes().get("name"));
		assertEquals(1, child1d.getChildElements().size());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(5, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXMLNode.class, child1_1);
		final IXMLNode child1_1x = (IXMLNode) child1_1;
		assertEquals("p", child1_1x.getName().getLocalPart());
		assertEquals(4, child1_1x.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(4, child1_1x.getEndLocation().orElseThrow().getLineNumber());
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(111, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(6, child1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1d.getFormalParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("call-template", child1_1d.getName());
		assertEquals(5, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1_1d.getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("call-template", child1_1d.getName());
		assertEquals(5, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTParameterNode> params = child1_1d.getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
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
		final IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());
		assertEquals(2, rootNode.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(9, rootNode.getEndLocation().orElseThrow().getLineNumber());
		assertEquals(80, rootNode.getStartLocation().orElseThrow().getColumnNumber());
		assertEquals(18, rootNode.getEndLocation().orElseThrow().getColumnNumber());

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1);
		final IXSLTDirectiveNode child1d = (IXSLTDirectiveNode) child1;
		assertEquals("template", child1d.getName());
		assertEquals(3, child1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(8, child1d.getEndLocation().orElseThrow().getLineNumber());

		final IStructureTreeNode child1_1 = child1d.getChildElements().get(0);
		assertInstanceOf(IXSLTDirectiveNode.class, child1_1);
		final IXSLTDirectiveNode child1_1d = (IXSLTDirectiveNode) child1_1;
		assertEquals("for-each", child1_1d.getName());
		assertEquals("bar", child1_1d.getXSLTAttributes().get("select"));
		assertEquals(4, child1_1d.getStartLocation().orElseThrow().getLineNumber());
		assertEquals(7, child1_1d.getEndLocation().orElseThrow().getLineNumber());

		final ImmutableList<IXSLTSortNode> sortNodes = child1_1d.getSorting();
		assertEquals(1, sortNodes.size());
		final IXSLTSortNode sortNode = sortNodes.get(0);
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

}
