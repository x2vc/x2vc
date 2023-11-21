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
package org.x2vc.common;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

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
	 * {@link org.x2vc.utilities.URIUtilities#makeMemoryURI(org.x2vc.utilities.URIUtilities.ObjectType, java.lang.String)}.
	 */
	@Test
	void testMakeMemoryURI() {
		final URI uri1 = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		assertEquals("memory:stylesheet/foobar", uri1.toString());
		final URI uri2 = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "boofar");
		assertEquals("memory:schema/boofar", uri2.toString());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.URIUtilities#makeMemoryURI(org.x2vc.utilities.URIUtilities.ObjectType, java.lang.String, int)}.
	 */
	@Test
	void testMakeMemoryURIWithVersion() {
		final URI uri1 = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar", 1);
		assertEquals("memory:stylesheet/foobar#v1", uri1.toString());
		final URI uri2 = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "boofar", 42);
		assertEquals("memory:schema/boofar#v42", uri2.toString());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.URIUtilities#isMemoryURI(java.net.URI)}.
	 */
	@Test
	void testIsMemoryURI() {
		assertTrue(URIUtilities.isMemoryURI(URI.create("memory:stylesheet/foobar")));
		assertTrue(URIUtilities.isMemoryURI(URI.create("memory:schema/foobar#v56")));
		assertFalse(URIUtilities.isMemoryURI(URI.create("file:///dev/null")));
		assertFalse(URIUtilities.isMemoryURI(URI.create("http://in.val.id")));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.URIUtilities#getObjectType(java.net.URI)}.
	 *
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException
	 */
	@Test
	void testGetObjectType() throws IllegalArgumentException, URISyntaxException {
		assertEquals(ObjectType.STYLESHEET, URIUtilities.getObjectType(URI.create("memory:stylesheet/foobar")));
		assertEquals(ObjectType.SCHEMA, URIUtilities.getObjectType(URI.create("memory:schema/foobar#v78")));
		final URI testURI = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getObjectType(testURI));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.utilities.URIUtilities#getIdentifier(java.net.URI)}.
	 *
	 * @throws IllegalArgumentException
	 */
	@Test
	void testGetIdentifier() throws IllegalArgumentException, URISyntaxException {
		assertEquals("foobar", URIUtilities.getIdentifier(URI.create("memory:stylesheet/foobar")));
		assertEquals("foobar", URIUtilities.getIdentifier(URI.create("memory:schema/foobar#v78")));
		final URI testURI1 = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getIdentifier(testURI1));
		final URI testURI2 = URI.create("memory:stylesheet/foo/bar");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getIdentifier(testURI2));
	}

	/**
	 * Test method for {@link org.x2vc.utilities.URIUtilities#getVersion(java.net.URI)}.
	 */
	@Test
	void testGetVersion() {
		assertFalse(URIUtilities.getVersion(URI.create("memory:stylesheet/foobar")).isPresent());
		assertTrue(URIUtilities.getVersion(URI.create("memory:stylesheet/foobar#v42")).isPresent());
		assertEquals(42, URIUtilities.getVersion(URI.create("memory:stylesheet/foobar#v42")).get());
		final URI testURI1 = URI.create("file:///dev/null");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getVersion(testURI1));
		final URI testURI2 = URI.create("memory:stylesheet/foobar#78");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getVersion(testURI2));
		final URI testURI3 = URI.create("memory:stylesheet/foobar#vasdf");
		assertThrows(IllegalArgumentException.class, () -> URIUtilities.getVersion(testURI3));
	}

}
