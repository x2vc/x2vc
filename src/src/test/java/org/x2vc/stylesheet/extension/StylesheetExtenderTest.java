package org.x2vc.stylesheet.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

class StylesheetExtenderTest {

	// CAUTION: The "stylesheets" used in these tests are usually not valid XSLT
	// stylesheets. They are designed to test individual extender elements with no
	// regards for XSLT consistency.

	IStylesheetExtender extender;

	@BeforeEach
	void prepareInstances() {
		this.extender = new StylesheetExtender();
	}

	private void compareXML(String input, String expectedOutput) {
		Diff d = DiffBuilder.compare(Input.fromString(expectedOutput)).ignoreWhitespace()
				.withTest(this.extender.extendStylesheet(input)).build();
		assertFalse(d.hasDifferences(), d.fullDescription());
	}

	// --- xsl:apply-imports
	// no parameters
	// trace before execution

	@Test
	void testApplyImportsSingle() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:apply-imports/>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=apply-imports id=1</xsl:message>
					<xsl:apply-imports ext0:trace-id="1"/>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testApplyImportsNamespaceOccupied() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://foobar">
					<xsl:apply-imports/>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://foobar" xmlns:ext1="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=apply-imports id=1</xsl:message>
					<xsl:apply-imports ext1:trace-id="1"/>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testApplyImportsMultiple() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:apply-imports/>
					<xsl:apply-imports/>
					<xsl:apply-imports/>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=apply-imports id=1</xsl:message>
					<xsl:apply-imports ext0:trace-id="1"/>
					<xsl:message>trace type=elem name=apply-imports id=2</xsl:message>
					<xsl:apply-imports ext0:trace-id="2"/>
					<xsl:message>trace type=elem name=apply-imports id=3</xsl:message>
					<xsl:apply-imports ext0:trace-id="3"/>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:apply-templates
	// parameters: mode (not relevant because static)
	// trace before execution
	// TODO XSLT extension: support select expression variables
	// TODO XSLT extension: support template call parameters

	@Test
	void testApplyTemplates() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:apply-templates/>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=apply-templates id=1</xsl:message>
					<xsl:apply-templates ext0:trace-id="1"/>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:for-each
	// parameters: none
	// trace: start of content
	// TODO XSLT extension: support select expression variables

	@Test
	void testForEach() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:for-each select="foobar">
					<p>test</p>
					</xsl:for-each>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:for-each select="foobar" ext0:trace-id="1">
					<xsl:message>trace type=elem name=for-each id=1</xsl:message>
					<p>test</p>
					</xsl:for-each>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testForEachWithSort() {
		// message has to be placed after any xsl:sort elements
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:for-each select="foo">
					<xsl:sort select="bar"/>
					<p>test</p>
					</xsl:for-each>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:for-each select="foo" ext0:trace-id="1">
					<xsl:sort select="bar"/>
					<xsl:message>trace type=elem name=for-each id=1</xsl:message>
					<p>test</p>
					</xsl:for-each>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:call-template
	// parameters: name (irrelevant because static)
	// trace before execution
	// TODO XSLT extension: support template call parameters

	@Test
	void testCallTemplate() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:call-template name="foobar"/>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=call-template id=1</xsl:message>
					<xsl:call-template name="foobar" ext0:trace-id="1"/>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testCallTemplateWithActualParameters() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:call-template name="foo">
					<xsl:with-param name="bar" select="baz"/>
					</xsl:call-template>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=call-template id=1</xsl:message>
					<xsl:call-template name="foo" ext0:trace-id="1">
					<xsl:with-param name="bar" select="baz"/>
					</xsl:call-template>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:if
	// parameters: none (see todo)
	// trace: start of content
	// TODO XSLT extension: support condition tracing

	@Test
	void testIf() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:if test="foobar">
					<p>test</p>
					</xsl:if>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:if test="foobar" ext0:trace-id="1">
					<xsl:message>trace type=elem name=if id=1</xsl:message>
					<p>test</p>
					</xsl:if>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:choose / xsl:when / xsl:otherwise
	// parameters: none / none (see todo) / none
	// trace: before execution / start of content / start of content
	// TODO XSLT extension: support condition tracing

	@Test
	void testChooseWhenOtherwise() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:message>trace type=elem name=choose id=1</xsl:message>
					<xsl:choose ext0:trace-id="1">
					<xsl:when test="fooA" ext0:trace-id="2">
					<xsl:message>trace type=elem name=when id=2</xsl:message>
					<p>test A</p>
					</xsl:when>
					<xsl:when test="fooB" ext0:trace-id="3">
					<xsl:message>trace type=elem name=when id=3</xsl:message>
					<p>test B</p>
					</xsl:when>
					<xsl:otherwise ext0:trace-id="4">
					<xsl:message>trace type=elem name=otherwise id=4</xsl:message>
					<p>test O</p>
					</xsl:otherwise>
					</xsl:choose>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	// --- xsl:template
	// parameters: match, name, priority, mode - all static, no need to trace
	// trace: start of content
	// TODO XSLT extension: support condition tracing (match)
	// TODO XSLT extension: support template actual parameters

	// match only, name only, priority and mode optional
	@Test
	void testTemplateMatch() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:template match="/">
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template match="/" ext0:trace-id="1">
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testTemplateName() {
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:template name="bar">
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="bar" ext0:trace-id="1">
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

	@Test
	void testTemplateWithFormalParameters() {
		// message has to be placed after the xsl:param element
		String in = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
					<xsl:template name="foo">
					<xsl:param name="bar" select="baz"/>
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		String ex = """
					<?xml version="1.0"?>
					<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
					<xsl:template name="foo" ext0:trace-id="1">
					<xsl:param name="bar" select="baz"/>
					<xsl:message>trace type=elem name=template id=1</xsl:message>
					<p>foobar</p>
					</xsl:template>
					</xsl:stylesheet>
					""";
		compareXML(in, ex);
	}

}
