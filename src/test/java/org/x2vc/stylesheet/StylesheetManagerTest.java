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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.IFileReader;

@ExtendWith(MockitoExtension.class)
class StylesheetManagerTest {

	@Mock
	private IFileReader fileReader;

	@Mock
	private IStylesheetPreprocessor preprocessor;

	@Mock
	private IStylesheetInformation stylesheet;

	private StylesheetManager manager;

	@BeforeEach
	void setUp() throws Exception {
		this.manager = new StylesheetManager(this.fileReader, this.preprocessor, 10);
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.StylesheetManager#insert(java.lang.String)} and subsequent
	 * {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)}.
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
	 * Test method for {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)} with existing file.
	 *
	 * @throws IOException
	 * @throws UnsupportedCharsetException
	 */
	@Test
	void testExistingFile() throws UnsupportedCharsetException, IOException {
		final URI uri = new File("someStylesheet.xslt").toURI();
		when(this.fileReader.readFile(any())).thenReturn("<!-- dummy XSLT content -->");
		when(this.preprocessor.prepareStylesheet(uri, "<!-- dummy XSLT content -->"))
			.thenReturn(this.stylesheet);
		final IStylesheetInformation stylesheet2 = this.manager.get(uri);
		assertSame(this.stylesheet, stylesheet2);
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)} with reader error
	 *
	 * @throws IOException
	 * @throws UnsupportedCharsetException
	 */
	@Test
	void testReaderError() throws UnsupportedCharsetException, IOException {
		when(this.fileReader.readFile(any())).thenThrow(IOException.class);
		final URI uri = new File("nonexistingStylesheet.xslt").toURI();
		assertThrows(IllegalArgumentException.class, () -> this.manager.get(uri));
	}

	/**
	 * Test method for error handling within {@link org.x2vc.stylesheet.StylesheetManager#get(java.net.URI)}.
	 *
	 * @throws IOException
	 * @throws UnsupportedCharsetException
	 */
	@Test
	void testProcessorErrorHandling() throws UnsupportedCharsetException, IOException {
		final URI uri = new File("someStylesheet.xslt").toURI();
		when(this.fileReader.readFile(any())).thenReturn("<!-- dummy XSLT content -->");
		when(this.preprocessor.prepareStylesheet(eq(uri), anyString())).thenThrow(IllegalArgumentException.class);
		assertThrows(IllegalArgumentException.class, () -> this.manager.get(uri));
	}

}
