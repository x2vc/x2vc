package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.UUID;

/**
 * Common interface for all schema objects.
 */
public interface IXMLSchemaObject {

	/**
	 * @return the schema element ID
	 */
	UUID getID();

	/**
	 * @return an optional comment describing the schema object.
	 */
	Optional<String> getComment();

	/**
	 * @return <code>true</code> if the object is an {@link IXMLAttribute}
	 */
	boolean isAttribute();

	/**
	 * @return a {@link IXMLAttribute} reference if the object is of the appropriate
	 *         type
	 */
	IXMLAttribute asAttribute();

	/**
	 * @return <code>true</code> if the object is an {@link IXMLElementType}
	 */
	boolean isElement();

	/**
	 * @return a {@link IXMLElementType} reference if the object is of the
	 *         appropriate type
	 */
	IXMLElementType asElement();

	/**
	 * @return <code>true</code> if the object is an {@link IXMLElementReference}
	 */
	boolean isReference();

	/**
	 * @return a {@link IXMLElementReference} reference if the object is of the
	 *         appropriate type
	 */
	IXMLElementReference asReference();

	/**
	 * @return <code>true</code> if the object is an {@link IXMLDiscreteValue}
	 */
	boolean isValue();

	/**
	 * @return a {@link IXMLDiscreteValue} reference if the object is of the
	 *         appropriate type
	 */
	IXMLDiscreteValue asValue();

}
