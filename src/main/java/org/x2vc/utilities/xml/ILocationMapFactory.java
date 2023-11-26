package org.x2vc.utilities.xml;

import com.google.inject.assistedinject.Assisted;

/**
 * Factory to obtain instances of {@link ILocationMap}.
 */
public interface ILocationMapFactory {

	/**
	 * Creates a new {@link ILocationMap} instance with the values provided
	 *
	 * @param maxOffset   the maximum offset recorded in the file, i.e. the file length without BOMs
	 * @param lineLengths the array of individual line lengths, excluding line break characters
	 * @param lineOffsets the array of cumulative line lengths (the offsets of the beginning of each line), including
	 *                    line break characters
	 * @return the location map
	 */
	ILocationMap create(@Assisted int maxOffset,
			@Assisted("lineLengths") int[] lineLengths,
			@Assisted("lineOffsets") int[] lineOffsets);

}
