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
