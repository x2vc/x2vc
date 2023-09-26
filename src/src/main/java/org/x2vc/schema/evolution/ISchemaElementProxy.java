package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IXMLElementType;

/**
 * A reference either to an existing {@link IXMLElementType} or to an {@link IAddElementModifier} that was recorded in
 * order to create an element reference and type.
 */
public interface ISchemaElementProxy {

	/**
	 * @return the ID of the element type (either existing or to be generated)
	 */
	UUID getID();

	/**
	 * @return <code>true</code> if the element already exists, <code>false</code> if this reference contains a modifier
	 */
	boolean exists();

	/**
	 * @return the existing element type if set
	 */
	Optional<IXMLElementType> getElementType();

	/**
	 * @return the modifier to create an element type if set
	 */
	Optional<IAddElementModifier> getModifier();

	/**
	 * @param name the name of the element
	 * @return the proxy representing the sub-element of the given name if present
	 */
	Optional<ISchemaElementProxy> getSubElement(String name);

	/**
	 * @param name the name of the attribute
	 * @return <code>true</code> if the element has an attribute of the given name
	 */
	boolean hasAttribute(String name);

}
