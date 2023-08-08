package org.x2vc.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.x2vc.common.URIHandling.ObjectType;

/**
 *
 */
class URIHandlingTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.x2vc.common.URIHandling#makeMemoryURI(org.x2vc.common.URIHandling.ObjectType, java.lang.String)}.
	 */
	@Test
	void testMakeMemoryURI() {
		final URI uri1 = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		assertEquals("memory:stylesheet/foobar", uri1.toString());
		final URI uri2 = URIHandling.makeMemoryURI(ObjectType.SCHEMA, "boofar");
		assertEquals("memory:schema/boofar", uri2.toString());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.common.URIHandling#makeMemoryURI(org.x2vc.common.URIHandling.ObjectType, java.lang.String, int)}.
	 */
	@Test
	void testMakeMemoryURIWithVersion() {
		final URI uri1 = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar", 1);
		assertEquals("memory:stylesheet/foobar#v1", uri1.toString());
		final URI uri2 = URIHandling.makeMemoryURI(ObjectType.SCHEMA, "boofar", 42);
		assertEquals("memory:schema/boofar#v42", uri2.toString());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.common.URIHandling#isMemoryURI(java.net.URI)}.
	 */
	@Test
	void testIsMemoryURI() {
		assertTrue(URIHandling.isMemoryURI(URI.create("memory:stylesheet/foobar")));
		assertTrue(URIHandling.isMemoryURI(URI.create("memory:schema/foobar#v56")));
		assertFalse(URIHandling.isMemoryURI(URI.create("file:///dev/null")));
		assertFalse(URIHandling.isMemoryURI(URI.create("http://in.val.id")));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.common.URIHandling#getObjectType(java.net.URI)}.
	 *
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException
	 */
	@Test
	void testGetObjectType() throws IllegalArgumentException, URISyntaxException {
		assertEquals(ObjectType.STYLESHEET, URIHandling.getObjectType(URI.create("memory:stylesheet/foobar")));
		assertEquals(ObjectType.SCHEMA, URIHandling.getObjectType(URI.create("memory:schema/foobar#v78")));
		final URI testURI = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getObjectType(testURI));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.common.URIHandling#getIdentifier(java.net.URI)}.
	 *
	 * @throws IllegalArgumentException
	 */
	@Test
	void testGetIdentifier() throws IllegalArgumentException, URISyntaxException {
		assertEquals("foobar", URIHandling.getIdentifier(URI.create("memory:stylesheet/foobar")));
		assertEquals("foobar", URIHandling.getIdentifier(URI.create("memory:schema/foobar#v78")));
		final URI testURI1 = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getIdentifier(testURI1));
		final URI testURI2 = URI.create("memory:stylesheet/foo/bar");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getIdentifier(testURI2));
	}

	/**
	 * Test method for {@link org.x2vc.common.URIHandling#getVersion(java.net.URI)}.
	 */
	@Test
	void testGetVersion() {
		assertFalse(URIHandling.getVersion(URI.create("memory:stylesheet/foobar")).isPresent());
		assertTrue(URIHandling.getVersion(URI.create("memory:stylesheet/foobar#v42")).isPresent());
		assertEquals(42, URIHandling.getVersion(URI.create("memory:stylesheet/foobar#v42")).get());
		final URI testURI1 = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getVersion(testURI1));
		final URI testURI2 = URI.create("memory:stylesheet/foobar#78");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getVersion(testURI2));
		final URI testURI3 = URI.create("memory:stylesheet/foobar#vasdf");
		assertThrows(IllegalArgumentException.class, () -> URIHandling.getVersion(testURI3));
	}

}
