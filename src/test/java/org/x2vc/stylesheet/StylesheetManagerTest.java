package org.x2vc.stylesheet;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StylesheetManagerTest {

	@Mock
	private IStylesheetPreprocessor preprocessor;

	@Mock
	private IStylesheetInformation stylesheet;

	private StylesheetManager manager;

	@BeforeEach
	void setUp() throws Exception {
		this.manager = new StylesheetManager(this.preprocessor, 10);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.StylesheetManager#insert(java.lang.String)} and
	 * subsequent {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)}.
	 */
	@Test
	void testTemporaryStylesheet() {
		when(this.preprocessor.prepareStylesheet(any(), eq("source"))).thenReturn(this.stylesheet);
		final URI tempURI = this.manager.insert("source");
		assertNotNull(tempURI);
		final IStylesheetInformation stylesheet2 = this.manager.get(tempURI);
		assertSame(this.stylesheet, stylesheet2);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)} with existing
	 * file.
	 */
	@Test
	void testExistingFile() {
		final URI uri = new File(
				"src/test/resources/data/org.x2vc.stylesheet.StylesheetManager/existingStylesheet.xslt")
			.toURI();
		when(this.preprocessor.prepareStylesheet(uri,
				"<!-- this is not a stylesheet, but the mocked components don't really care -->"))
			.thenReturn(this.stylesheet);
		final IStylesheetInformation stylesheet2 = this.manager.get(uri);
		assertSame(this.stylesheet, stylesheet2);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)} with
	 * nonexisting file.
	 */
	@Test
	void testNonexistingFile() {
		final URI uri = new File(
				"src/test/resources/data/org.x2vc.stylesheet.StylesheetManager/nonexistingStylesheet.xslt")
			.toURI();
		assertThrows(IllegalArgumentException.class, () -> this.manager.get(uri));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)} with invalid
	 * / unsupported URI.
	 *
	 * @throws URISyntaxException
	 */
	@Test
	void testUnsupportedFileURI() throws URISyntaxException {
		final URI uri = new URI("ftp", "foobar", "/dev/null", null);
		assertThrows(IllegalArgumentException.class, () -> this.manager.get(uri));
	}

	/**
	 * Test method for error handling within
	 * {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)}.
	 */
	@Test
	void testProcessorErrorHandling() {
		final URI uri = new File(
				"src/test/resources/data/org.x2vc.stylesheet.StylesheetManager/existingStylesheet.xslt")
			.toURI();
		when(this.preprocessor.prepareStylesheet(eq(uri), anyString())).thenThrow(IllegalArgumentException.class);
		assertThrows(IllegalArgumentException.class, () -> this.manager.get(uri));
	}

}
