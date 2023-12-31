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

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;

import com.google.inject.ImplementedBy;

/**
 * This component reads an input file into memory. It is intended to read the XSLT programs for processing - so to
 * handle relatively small files. It attempts to guess the input file encoding using the ICU library and perform the
 * appropriate conversions, removing any BOM characters in the process.
 */
@ImplementedBy(FileReader.class)
public interface IFileReader {

	/**
	 * Reads the file contents into memory.
	 *
	 * @param file
	 * @return the file contents
	 * @throws IOException
	 * @throws UnsupportedCharsetException
	 */
	String readFile(File file) throws IOException, UnsupportedCharsetException;

}
