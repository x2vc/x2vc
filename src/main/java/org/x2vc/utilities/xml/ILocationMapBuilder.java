package org.x2vc.utilities.xml;

import java.io.File;

/**
 * A factory that creates the {@link ILocationMap} instances
 */
public interface ILocationMapBuilder {

	/**
	 * Creates an {@link ILocationMap} instance based on an input file.
	 *
	 * @param xmlFile the input file
	 * @return the location map
	 * @throws IllegalArgumentException
	 */
	ILocationMap buildLocationMap(File xmlFile) throws IllegalArgumentException;

}
