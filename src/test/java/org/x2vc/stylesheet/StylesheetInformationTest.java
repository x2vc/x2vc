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
package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.utilities.xml.ILocationMap;
import org.x2vc.utilities.xml.ITagMap;

import com.google.common.collect.Multimap;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class StylesheetInformationTest {

	@Mock
	IStylesheetStructure mockStructure;

	@Mock
	Multimap<String, URI> namespacePrefixes;

	@Mock
	ILocationMap mockLocationMap;

	@Mock
	ITagMap mockTagMap;

	URI testURI = URI.create("foo");

	String traceNamespacePrefix = "https://foo.bar";

	@Test
	void testConstructor_whenLocationNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(null, "a", "b", this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure, this.mockLocationMap, this.mockTagMap);
		});
	}

	@Test
	void testConstructor_whenOriginalContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, null, "b", this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure, this.mockLocationMap, this.mockTagMap);
		});
	}

	@Test
	void testConstructor_whenPreparedContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", null, this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure, this.mockLocationMap, this.mockTagMap);
		});
	}

	@Test
	void testConstructor_whenStructureNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", "b", this.namespacePrefixes, this.traceNamespacePrefix, null,
					this.mockLocationMap, this.mockTagMap);
		});
	}

	@Test
	void testGetOriginalLocation() {
		final IStylesheetInformation si = new StylesheetInformation(this.testURI, "a", "b", this.namespacePrefixes,
				this.traceNamespacePrefix, this.mockStructure, this.mockLocationMap, this.mockTagMap);
		assertEquals(this.testURI, si.getURI());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.StylesheetInformation#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(StylesheetInformation.class).verify();
	}

}
