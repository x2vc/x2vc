package org.x2vc.utilities.xml;

import com.google.inject.ImplementedBy;

/**
 * A factory that creates the {@link ILocationMap} instances
 */
@ImplementedBy(LocationMapBuilder.class)
public interface ILocationMapBuilder {

	/**
	 * Creates an {@link ILocationMap} instance based on an input file.
	 *
	 * @param xmlSource the XML source
	 * @return the location map
	 * @throws IllegalArgumentException
	 */
	ILocationMap buildLocationMap(String xmlSource) throws IllegalArgumentException;

}
