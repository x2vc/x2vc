package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A reference to
 * <ul>
 * <li>an existing {@link IElementType},</li>
 * <li>an existing {@link IAttribute},</li>
 * <li>an {@link IAddElementModifier},</li>
 * <li>an {@link IAddAttributeModifier} or</li>
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
		 * Proxy for an existing {@link IElementType} and {@link IElementReference}
		 */
		ELEMENT,
		/**
		 * Proxy for an {@link IAddElementModifier}
		 */
		ELEMENT_MODIFIER,
		/**
		 * Proxy for an existing {@link IAttribute}
		 */
		ATTRIBUTE,
		/**
		 * Proxy for an {@link IAddAttributeModifier}
		 */
		ATTRIBUTE_MODIFIER,
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
	 * @return <code>true</code> if the proxy refers to an existing {@link IElementType} and {@link IElementReference}
	 */
	boolean isElement();

	/**
	 * @return <code>true</code> if the proxy refers to an {@link IAddElementModifier}
	 */
	boolean isElementModifier();

	/**
	 * @return <code>true</code> if the proxy refers to an existing {@link IAttribute}
	 */
	boolean isAttribute();

	/**
	 * @return <code>true</code> if the proxy refers to an {@link IAddAttributeModifier}
	 */
	boolean isAttributeModifier();

	/**
	 * @return <code>true</code> if the proxy refers to the document root node
	 */
	boolean isDocument();

	/**
	 * @return the ID of the element type (either existing or to be generated) or an empty object for attributes or the
	 *         document root node
	 */
	Optional<UUID> getElementTypeID();

	/**
	 * @return the element name for existing elements (with unique references) or element modifiers or an empty object
	 *         for attributes or the document root node
	 */
	Optional<String> getElementName();

	/**
	 * @return the existing element type if the proxy type is ELEMENT
	 */
	Optional<IElementType> getElementType();

	/**
	 * @return the existing element type if the proxy type is ELEMENT
	 */
	Optional<IElementReference> getElementReference();

	/**
	 * @return the modifier to create an element type if the proxy type is ELEMENT_MODIFIER
	 */
	Optional<IAddElementModifier> getElementModifier();

	/**
	 * @return the ID of the attribute (either existing or to be generated) or an empty object for the non-element proxy
	 *         types
	 */
	Optional<UUID> getAttributeID();

	/**
	 * @return the name of the attribute (either existing or to be generated) or an empty object for the non-element
	 *         proxy types
	 */
	Optional<String> getAttributeName();

	/**
	 * @return the existing attribute if the proxy type is ATTRIBUTE
	 */
	Optional<IAttribute> getAttribute();

	/**
	 * @return the modifier to create an attribute if the proxy type is ATTRIBUTE_MODIFIER
	 */
	Optional<IAddAttributeModifier> getAttributeModifier();

	/**
	 * @return the IXMLSchema for document proxies or an empty element for other types
	 */
	Optional<IXMLSchema> getSchema();

	/**
	 * Returns the proxy representing the sub-element of the given name if present. Will always return an empty object
	 * for attributes, attribute modifiers or the document root node.
	 *
	 * @param name the name of the element
	 * @return the proxy representing the sub-element of the given name if present
	 */
	Optional<ISchemaElementProxy> getSubElement(String name);

	/**
	 * @return a collection of all sub-elements of an existing element, an element modifier or the document root, or an
	 *         empty collection for attributes and attribute modifiers
	 */
	ImmutableList<ISchemaElementProxy> getSubElements();

	/**
	 * @param name the name of the element
	 * @return <code>true</code> if the element has a sub-element of the given name (always <code>false</code> for
	 *         attributes, attribute modifiers or the document root node)
	 */
	boolean hasSubElement(String name);

	/**
	 * Returns the proxy representing the attribute of the given name if present. Will always return an empty object for
	 * attributes, attribute modifiers or the document root node.
	 *
	 * @param name the name of the attribute
	 * @return the proxy representing the sub-element of the given name if present
	 */
	Optional<ISchemaElementProxy> getSubAttribute(String name);

	/**
	 * @return a collection of all attributes of an existing element or an element modifier, or an empty collection for
	 *         attributes, attribute modifiers or the document root
	 */
	ImmutableSet<ISchemaElementProxy> getSubAttributes();

	/**
	 * @param name the name of the attribute
	 * @return <code>true</code> if the element has an attribute of the given name (always <code>false</code> for the
	 *         document root node)
	 */
	boolean hasSubAttribute(String name);

}
