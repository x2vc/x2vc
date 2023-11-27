package org.x2vc.utilities.xml;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Default implementation of {@link ILocationMapBuilder}.
 */
public class LocationMapBuilder implements ILocationMapBuilder {

	private static final Logger logger = LogManager.getLogger();

	private final ILocationMapFactory locationMapFactory;

	@Inject
	LocationMapBuilder(ILocationMapFactory locationMapFactory) {
		this.locationMapFactory = locationMapFactory;
	}

	@Override
	public ILocationMap buildLocationMap(String xmlSource) throws IllegalArgumentException {
		logger.traceEntry("with {} characters of XML content", xmlSource.length());
		final List<Integer> lineLengths = Lists.newArrayList();
		final List<Integer> lineOffsets = Lists.newArrayList();
		final int maxOffset = countLinesAndOffsets(xmlSource, lineLengths, lineOffsets);
		final ILocationMap result = this.locationMapFactory.create(maxOffset,
				lineLengths.stream().mapToInt(Integer::intValue).toArray(),
				lineOffsets.stream().mapToInt(Integer::intValue).toArray());
		return logger.traceExit(result);
	}

	/**
	 * @param xmlSource
	 * @param lineLengths
	 * @param lineOffsets
	 * @return
	 */
	@SuppressWarnings("java:S3776") // no real way to simplify this method w/o impairing readability
	private int countLinesAndOffsets(String xmlSource, List<Integer> lineLengths,
			List<Integer> lineOffsets) {
		logger.traceEntry();
		try {
			try (final StringReader stringReader = new StringReader(xmlSource)) {
				try (final PushbackReader pushbackReader = new PushbackReader(stringReader)) {

					// check whether there's an initial BOM to swallow
					int value = pushbackReader.read();
					if (value == -1) {
						logger.debug("file appears to be empty");
						return logger.traceExit(0);
					} else {
						// Java input processing maps all other BOMs to 0xFEFF:
						// check whether we've caught one we want to ignore
						if (((char) value) != '\uFEFF') {
							pushbackReader.unread(value);
						}
					}

					int currentOffset = 0;
					lineOffsets.add(currentOffset); // first line always starts at offset 0
					int currentLineLength = 0;
					while (true) {
						// read next char
						value = pushbackReader.read();
						if (value == -1) {
							// end of file reached - record length of last line
							lineLengths.add(currentLineLength);
							return logger.traceExit(currentOffset);
						}
						char currentChar = (char) value;

						// check if we've reached the end of a line
						if ((currentChar == '\r') || (currentChar == '\n')) {
							// yes - push current line length to output
							lineLengths.add(currentLineLength);
							currentLineLength = 0;

							// swallow current character and an eventual following second line break character
							currentOffset++;
							if (currentChar == '\r') {
								value = pushbackReader.read();
								if (value == -1) {
									// end of file reached
									return logger.traceExit(currentOffset);
								}
								currentChar = (char) value;
								if (currentChar == '\n') {
									currentOffset++;
								} else {
									pushbackReader.unread(value);
								}
							}

							// note new line offset
							lineOffsets.add(currentOffset);
						} else {
							// normal character - count and move on
							currentOffset++;
							currentLineLength++;
						}
					}
				}
			}
		} catch (final IOException e) {
			throw logger.throwing(new IllegalArgumentException("unable to read XML file contents", e));
		}
	}

}
