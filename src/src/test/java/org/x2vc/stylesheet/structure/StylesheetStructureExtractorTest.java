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
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar&apos;<![CDATA[Some<>Content]]></p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());
		assertEquals("bar", child1.asDirective().getXSLTAttributes().get("name"));
		assertEquals(1, child1.asDirective().getTraceID().get());
		assertEquals(2, child1.asDirective().getChildElements().size());

		IStructureTreeNode child1_1 = child1.asDirective().getChildElements().get(0);
		assertTrue(child1_1.isXSLTDirective());
		assertEquals("message", child1_1.asDirective().getName());
		assertFalse(child1_1.asDirective().getTraceID().isPresent());

		IStructureTreeNode child1_2 = child1.asDirective().getChildElements().get(1);
		assertTrue(child1_2.isXML());
		assertEquals("p", child1_2.asXML().getName().getLocalPart());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TopLevelParameters() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:param name="foo" select="bar"/>
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar&apos;<![CDATA[Some<>Content]]></p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		ImmutableList<IXSLTParameterNode> params = rootNode.getFormalParameters();
		assertEquals(1, params.size());
		assertEquals("foo", params.get(0).getName());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithFormalParameters() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:param name="foo" select="bar"/>
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar&apos;<![CDATA[Some<>Content]]></p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		ImmutableList<IXSLTParameterNode> params = child1.asDirective().getFormalParameters();
		assertEquals(1, params.size());
		assertEquals("foo", params.get(0).getName());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.structure.StylesheetStructureExtractor#extractStructure(java.lang.String)}.
	 */
	@Test
	void test_TemplateWithActualParametersAsAttribute() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:param name="foo" select="bar"/>
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<xsl:message>trace type=elem name=call-template id=2</xsl:message>
					<xsl:call-template name="bar" ext0:trace-id="2">
					<xsl:with-param name="foo" select="baz"/>
					</xsl:call-template>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		IStructureTreeNode child1_3 = child1.asDirective().getChildElements().get(2);
		assertTrue(child1_3.isXSLTDirective());
		assertEquals("call-template", child1_3.asDirective().getName());
		assertTrue(child1_3.asDirective().getTraceID().isPresent());
		assertEquals(2, child1_3.asDirective().getTraceID().get());

		ImmutableList<IXSLTParameterNode> params = child1_3.asDirective().getActualParameters();
		assertEquals(1, params.size());
		IXSLTParameterNode param = params.get(0);
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
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:param name="foo"/>
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<xsl:message>trace type=elem name=call-template id=2</xsl:message>
					<xsl:call-template name="bar" ext0:trace-id="2">
					<xsl:with-param name="foo">baz</xsl:with-param>
					</xsl:call-template>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		IStructureTreeNode child1_3 = child1.asDirective().getChildElements().get(2);
		assertTrue(child1_3.isXSLTDirective());
		assertEquals("call-template", child1_3.asDirective().getName());
		assertTrue(child1_3.asDirective().getTraceID().isPresent());
		assertEquals(2, child1_3.asDirective().getTraceID().get());

		ImmutableList<IXSLTParameterNode> params = child1_3.asDirective().getActualParameters();
		assertEquals(1, params.size());
		IXSLTParameterNode param = params.get(0);
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
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="foo" ext0:trace-id="1">
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<xsl:for-each select="bar" ext0:trace-id="2">
					<xsl:sort select="baz" lang="bazlang" data-type="baztype" order="descending" case-order="lower-first" />
					<xsl:message>trace type=elem name=for-each id=2</xsl:message>
					<p>test</p>
					</xsl:for-each>
					</xsl:template>
					</xsl:stylesheet>
					""";
		IXSLTDirectiveNode rootNode = this.extractor.extractStructure(in).getRootNode();

		assertEquals("stylesheet", rootNode.getName());
		assertEquals(1, rootNode.getChildElements().size());

		IStructureTreeNode child1 = rootNode.getChildElements().get(0);
		assertTrue(child1.isXSLTDirective());
		assertEquals("template", child1.asDirective().getName());

		IStructureTreeNode child1_2 = child1.asDirective().getChildElements().get(1);
		assertTrue(child1_2.isXSLTDirective());
		assertEquals("for-each", child1_2.asDirective().getName());
		assertTrue(child1_2.asDirective().getTraceID().isPresent());
		assertEquals(2, child1_2.asDirective().getTraceID().get());
		assertEquals("bar", child1_2.asDirective().getXSLTAttributes().get("select"));

		ImmutableList<IXSLTSortNode> sort = child1_2.asDirective().getSorting();
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
