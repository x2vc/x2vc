package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.Multimap;

@ExtendWith(MockitoExtension.class)
class NamespaceExtractorTest {

	private NamespaceExtractor extractor;

	@BeforeEach
	void setUp() throws Exception {
		this.extractor = new NamespaceExtractor();
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#extractNamespaces(java.lang.String)}.
	 */
	@Test
	void testExtractNamespaces_Minimal() {
		final String xslt = """
							<?xml version="1.0" encoding="UTF-8"?>
							<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template match="/">
							</xsl:template>
							</xsl:stylesheet>
							""";
		final Multimap<String, URI> result = this.extractor.extractNamespaces(xslt);
		assertNotNull(result);
		assertEquals(Set.of(URI.create("http://www.w3.org/1999/XSL/Transform")), Set.copyOf(result.get("xsl")));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#extractNamespaces(java.lang.String)}.
	 */
	@Test
	void testExtractNamespaces_AdditionalTopLevel() {
		final String xslt = """
							<?xml version="1.0" encoding="UTF-8"?>
							<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							                              xmlns:foobar="http://www.foo.bar/baz">
							<xsl:template match="/">
								<foobar:one>
									<xsl:apply-templates/>
								</foobar:one>
							</xsl:template>
							</xsl:stylesheet>
							""";
		final Multimap<String, URI> result = this.extractor.extractNamespaces(xslt);
		assertNotNull(result);
		assertEquals(Set.of(URI.create("http://www.w3.org/1999/XSL/Transform")), Set.copyOf(result.get("xsl")));
		assertEquals(Set.of(URI.create("http://www.foo.bar/baz")), Set.copyOf(result.get("foobar")));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#extractNamespaces(java.lang.String)}.
	 */
	@Test
	void testExtractNamespaces_MultipleLocal() {
		final String xslt = """
							<?xml version="1.0" encoding="UTF-8"?>
							<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
							<xsl:template match="foo">
								<test:something xmlns:test="http://www.test.org/one">
									<test:content />
								</test:something>
							</xsl:template>
							<xsl:template match="bar">
								<test:something xmlns:test="http://www.test.org/two">
									<test:content />
								</test:something>
							</xsl:template>
							</xsl:stylesheet>
							""";
		final Multimap<String, URI> result = this.extractor.extractNamespaces(xslt);
		assertNotNull(result);
		assertEquals(Set.of(URI.create("http://www.w3.org/1999/XSL/Transform")), Set.copyOf(result.get("xsl")));
		assertEquals(Set.of(URI.create("http://www.test.org/one"), URI.create("http://www.test.org/two")),
				Set.copyOf(result.get("test")));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#extractNamespaces(java.lang.String)}.
	 */
	@Test
	void testExtractNamespaces_DefaultNamespace() {
		final String xslt = """
							<?xml version="1.0" encoding="UTF-8"?>
							<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							                              xmlns="http://www.foo.bar/baz">
							<xsl:template match="/">
								<one>
									<xsl:apply-templates/>
								</one>
							</xsl:template>
							</xsl:stylesheet>
							""";
		final Multimap<String, URI> result = this.extractor.extractNamespaces(xslt);
		assertNotNull(result);
		assertEquals(Set.of(URI.create("http://www.w3.org/1999/XSL/Transform")), Set.copyOf(result.get("xsl")));
		assertEquals(Set.of(URI.create("http://www.foo.bar/baz")),
				Set.copyOf(result.get(INamespaceExtractor.DEFAULT_NAMESPACE)));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#findUnusedPrefix(Set, String)}.
	 */
	@Test
	void testFindUnusedPrefix_SinglePrefix() {
		final String newPrefix = this.extractor.findUnusedPrefix(Set.of("xsl"), "ext");
		assertTrue(newPrefix.startsWith("ext"));
		assertTrue(newPrefix.length() > 3);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#findUnusedPrefix(Set, String)}.
	 */
	@Test
	void testFindUnusedPrefix_MultiplePrefixes() {
		final String newPrefix = this.extractor.findUnusedPrefix(Set.of("xsl", "foo", "bar"), "ext");
		assertTrue(newPrefix.startsWith("ext"));
		assertTrue(newPrefix.length() > 3);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#findUnusedPrefix(Set, String)}.
	 */
	@Test
	void testFindUnusedPrefix_CollidingPrefix() {
		final String newPrefix = this.extractor.findUnusedPrefix(Set.of("xsl", "ext0"), "ext");
		assertTrue(newPrefix.startsWith("ext"));
		assertTrue(newPrefix.length() > 3);
		assertNotEquals("ext0", newPrefix);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.NamespaceExtractor#findUnusedPrefix(Set, String)}.
	 */
	@Test
	void testFindUnusedPrefix_CollidingPrefixes() {
		final String newPrefix = this.extractor.findUnusedPrefix(Set.of("xsl", "ext0", "ext1", "ext2", "ext4"), "ext");
		assertTrue(newPrefix.startsWith("ext"));
		assertTrue(newPrefix.length() > 3);
		assertNotEquals("ext0", newPrefix);
		assertNotEquals("ext1", newPrefix);
		assertNotEquals("ext2", newPrefix);
		assertNotEquals("ext4", newPrefix);
	}

}
