package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());
		assertEquals("bar", child1.asDirective().getXSLTAttributes().get("name"));
		assertEquals(1, child1.asDirective().getChildElements().size());

		final IStructureTreeNode child1_1 = child1.asDirective().getChildElements().get(0);
		assertTrue(child1_1.isXML());
		assertEquals("p", child1_1.asXML().getName().getLocalPart());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TopLevelParameters() {
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		assertEquals("foo", params.get(0).getName());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithFormalParameters() {
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		final ImmutableList<IXSLTParameterNode> params = child1.asDirective().getFormalParameters();
		assertEquals(1, params.size());
		assertEquals("foo", params.get(0).getName());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithActualParametersAsAttribute() {
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		final IStructureTreeNode child1_1 = child1.asDirective().getChildElements().get(0);
		assertTrue(child1_1.isXSLTDirective());
		assertEquals("call-template", child1_1.asDirective().getName());

		final ImmutableList<IXSLTParameterNode> params = child1_1.asDirective().getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertEquals("foo", param.getName());
		assertTrue(param.getSelection().isPresent());
		assertEquals("baz", param.getSelection().get());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithActualParametersAsText() {
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		final IStructureTreeNode child1_1 = child1.asDirective().getChildElements().get(0);
		assertTrue(child1_1.isXSLTDirective());
		assertEquals("call-template", child1_1.asDirective().getName());

		final ImmutableList<IXSLTParameterNode> params = child1_1.asDirective().getActualParameters();
		assertEquals(1, params.size());
		final IXSLTParameterNode param = params.get(0);
		assertEquals("foo", param.getName());
		assertFalse(param.getSelection().isPresent());
		assertEquals(1, param.getChildElements().size());
		assertTrue(param.getChildElements().get(0).isText());
		assertEquals("baz", param.getChildElements().get(0).asText().getText());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_ForEachWithSort() {
		// TODO remove message statements from test case when adapting to new
		// position-based approach
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

		final IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		final IStructureTreeNode child1_1 = child1.asDirective().getChildElements().get(0);
		assertTrue(child1_1.isXSLTDirective());
		assertEquals("for-each", child1_1.asDirective().getName());
		assertEquals("bar", child1_1.asDirective().getXSLTAttributes().get("select"));

		final ImmutableList<IXSLTSortNode> sort = child1_1.asDirective().getSorting();
		assertEquals(1, sort.size());
		assertTrue(sort.get(0).getSortingExpression().isPresent());
		assertEquals("baz", sort.get(0).getSortingExpression().get());
		assertTrue(sort.get(0).getLanguage().isPresent());
		assertEquals("bazlang", sort.get(0).getLanguage().get());
		assertTrue(sort.get(0).getDataType().isPresent());
		assertEquals("baztype", sort.get(0).getDataType().get());
		assertTrue(sort.get(0).getSortOrder().isPresent());
		assertEquals("descending", sort.get(0).getSortOrder().get());
		assertTrue(sort.get(0).getCaseOrder().isPresent());
		assertEquals("lower-first", sort.get(0).getCaseOrder().get());
	}

}
