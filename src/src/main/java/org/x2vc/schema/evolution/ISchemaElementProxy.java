package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IElementType;

/**
 * A reference to
 * <ul>
 * <li>an existing {@link IElementType},</li>
 * <li>an {@link IAddElementModifier} or</li>
 * <li>the document root node</li>
 * </ul>
 * that was recorded in order to create an element reference and type.
 */
public interface ISchemaElementProxy {

	/**
	 * The type of object the proxy refers to.
	 */
	public enum ProxyType {
		/**
		 * Proxy for an existing {@link IElementType}
		 */
		ELEMENT,
		/**
		 * Proxy for an {@link IAddElementModifier}
		 */
		MODIFIER,
		/**
		 * Proxy for the document root node
		 */
		DOCUMENT
	}

	/**
	 * @return the type of element the proxy refers to
	 */
	ProxyType getType();

	/**
	 * @return <code>true</code> if the proxy refers to an existing {@link IElementType}
	 */
	boolean isElement();

	/**
	 * @return <code>true</code> if the proxy refers to an {@link IAddElementModifier}
	 */
	boolean isModifier();

	/**
	 * @return <code>true</code> if the proxy refers to the document root node
	 */
	boolean isDocument();

	/**
	 * @return the ID of the element type (either existing or to be generated) or an empty object for the document root
	 *         node
	 */
	Optional<UUID> getElementTypeID();

	/**
	 * @return the existing element type if the proxy type is ELEMENT
	 */
	Optional<IElementType> getElementType();

	/**
	 * @return the modifier to create an element type if the proxy type is MODIFIER
	 */
	Optional<IAddElementModifier> getModifier();

	/**
	 * Returns the proxy representing the sub-element of the given name if present. Will always return an empty object
	 * for the document root node.
	 *
	 * @param name the name of the element
	 * @return the proxy representing the sub-element of the given name if present
	 */
	Optional<ISchemaElementProxy> getSubElement(String name);

	/**
	 * @param name the name of the attribute
	 * @return <code>true</code> if the element has an attribute of the given name (always <code>false</code> for the
	 *         document root node)
	 */
	boolean hasAttribute(String name);

}
