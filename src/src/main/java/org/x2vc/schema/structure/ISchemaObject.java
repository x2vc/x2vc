package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.UUID;

/**
 * Common interface for all schema objects.
 */
public interface ISchemaObject {

	/**
	 * @return the schema element ID
	 */
	UUID getID();

	/**
	 * @return an optional comment describing the schema object.
	 */
	Optional<String> getComment();

}
