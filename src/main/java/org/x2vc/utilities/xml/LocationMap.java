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
package org.x2vc.utilities.xml;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.Objects;

import javax.xml.stream.Location;
import javax.xml.transform.SourceLocator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Default implementation of {@link ILocationMap}.
 */
public final class LocationMap implements ILocationMap {

	private static final Logger logger = LogManager.getLogger();

	private final int maxOffset;
	private final int[] lineLengths;
	private final int[] lineOffsets;

	/**
	 * Use {@link ILocationMapFactory} to create a new {@link ILocationMap} instance with the values provided.
	 *
	 * @param maxOffset   the maximum offset recorded in the file, i.e. the file length without BOMs
	 * @param lineLengths the array of individual line lengths, excluding line break characters
	 * @param lineOffsets the array of cumulative line lengths (the offsets of the beginning of each line), including
	 *                    line break characters
	 */
	@Inject
	LocationMap(@Assisted int maxOffset,
			@Assisted("lineLengths") int[] lineLengths,
			@Assisted("lineOffsets") int[] lineOffsets) {
		checkArgument(maxOffset >= 0, "negative file length is not possible");
		checkArgument(lineLengths.length > 0, "at least one line must be present");
		checkArgument(lineLengths.length == lineOffsets.length, "number of lines must be equal in both parameters");
		checkArgument(lineOffsets[0] == 0, "first line must start at offset 0");
		for (int i = 0; i < lineOffsets.length; i++) {
			checkArgument(lineLengths[i] >= 0, "negative line length is not possible");
			checkArgument(lineOffsets[i] >= 0, "negative line offset is not possible");
		}
		this.maxOffset = maxOffset;
		this.lineLengths = lineLengths;
		this.lineOffsets = lineOffsets;
	}

	@Override
	public int getOffset(int line, int column) throws IllegalArgumentException {
		if (line <= 0) {
			throw new IllegalArgumentException("Negative or zero line numbers are not possible");
		}
		if (column < 0) {
			throw new IllegalArgumentException("Negative column numbers are not possible");
		} else if (column == 0) {
			logger.debug("Column number 0 corrected to 1");
			column = 1;
		}
		if (line > this.lineOffsets.length) {
			throw new IllegalArgumentException(String.format("Line number %d exceeds number of lines in file (%d)",
					line, this.lineOffsets.length));
		}
		if (column > this.lineLengths[line - 1] + 1) {
			throw new IllegalArgumentException(String.format("Column number %d exceeds length line %d in file (%d)",
					column, line, this.lineLengths[line - 1]));
		}
		return this.lineOffsets[line - 1] + column - 1;
	}

	@Override
	public int getOffset(Location location) throws IllegalArgumentException {
		return getOffset(location.getLineNumber(), location.getColumnNumber());
	}

	@Override
	public int getOffset(SourceLocator locator) throws IllegalArgumentException {
		return getOffset(locator.getLineNumber(), locator.getColumnNumber());
	}

	@Override
	public PolymorphLocation getLocation(int line, int column) throws IllegalArgumentException {
		final int offset = getOffset(line, column);
		return PolymorphLocation.builder()
			.withCharacterOffset(offset)
			.withLineNumber(line)
			.withColumnNumber(column)
			.build();
	}

	@Override
	public PolymorphLocation getLocationByLineColumn(Location location) throws IllegalArgumentException {
		return getLocation(location.getLineNumber(), location.getColumnNumber());
	}

	@Override
	public PolymorphLocation getLocation(SourceLocator locator) throws IllegalArgumentException {
		return getLocation(locator.getLineNumber(), locator.getColumnNumber());
	}

	@Override
	public PolymorphLocation getLocation(int offset) throws IllegalArgumentException {
		if (offset < 0) {
			throw new IllegalArgumentException("Negative offset is not possible");
		}
		if (offset > this.maxOffset) {
			throw new IllegalArgumentException(String.format("Offset %d exceeds file length (%d)",
					offset, this.maxOffset));
		}
		int line = 0;
		while ((line < this.lineOffsets.length) && (this.lineOffsets[line] <= offset)) {
			line++;
		}
		// line is now the target line number - NOT the array index!
		final int column = offset - this.lineOffsets[line - 1] + 1;
		return PolymorphLocation.builder()
			.withCharacterOffset(offset)
			.withLineNumber(line)
			.withColumnNumber(column)
			.build();
	}

	@Override
	public PolymorphLocation getLocationByOffset(Location location) throws IllegalArgumentException {
		return getLocation(location.getCharacterOffset());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.lineLengths);
		result = prime * result + Arrays.hashCode(this.lineOffsets);
		result = prime * result + Objects.hash(this.maxOffset);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LocationMap)) {
			return false;
		}
		final LocationMap other = (LocationMap) obj;
		return Arrays.equals(this.lineLengths, other.lineLengths) && Arrays.equals(this.lineOffsets, other.lineOffsets)
				&& this.maxOffset == other.maxOffset;
	}

}
