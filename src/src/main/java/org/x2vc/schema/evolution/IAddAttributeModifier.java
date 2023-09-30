package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.XMLDataType;

/**
 * An {@link ISchemaModifier} to add a new attribute to an existing element or an element to be created. The UUID of the
 * attribute is generated when creating the modifier in order to allow for the modifiers to be chained.
 *
 */
public interface IAddAttributeModifier extends ISchemaModifier {

	/**
	 * @return the ID of the attribute
	 */
	UUID getAttributeID();

	/**
	 * @return the name of the attribute
	 */
	String getName();

	/**
	 * @return the comment of the attribute
	 */
	Optional<String> getComment();

	/**
	 * @return the data type of the data. Defaults to STRING if not set.
	 */
	XMLDataType getDataType();

}
