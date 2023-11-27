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
package org.x2vc.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Default implementation of {@link IFileReader}.
 */
public class FileReader implements IFileReader {

	private static final Logger logger = LogManager.getLogger();

	private CharsetDetector detector;

	/**
	 * @param detector
	 */
	@Inject
	public FileReader(CharsetDetector detector) {
		this.detector = detector;
	}

	@Override
	public String readFile(File file) throws IOException {
		logger.traceEntry("with file {}", file.getPath());
		final Charset charset = determineCharset(file);
		String result = Files.readString(file.toPath(), charset);

		// Java input processing maps all other BOMs to 0xFEFF:
		// check whether we've caught one we want to ignore
		if (result.startsWith("\uFEFF")) {
			result = result.substring(1);
		}

		logger.traceExit("with {} characters read", result.length());
		return result;
	}

	/**
	 * @param xmlFile
	 * @return
	 */
	protected Charset determineCharset(File xmlFile) throws UnsupportedCharsetException, IOException {
		logger.traceEntry();
		String charset = null;
		try (final FileInputStream fileInputStream = new FileInputStream(xmlFile)) {
			try (BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
				this.detector.setText(bufferedInputStream);
				final CharsetMatch charsetMatch = this.detector.detect();
				if (charsetMatch != null) {
					charset = charsetMatch.getName();
					logger.debug("identified file encoding as {}", charset);
				}
			}
		}
		if (Strings.isNullOrEmpty(charset)) {
			throw new UnsupportedCharsetException("unable to determine encoding of file");
		}
		return logger.traceExit(Charset.forName(charset));
	}

}
