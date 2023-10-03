package org.x2vc.schema.structure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A XML schema element type, representing an element in the XML document. This object specifies everything but the
 * element name, which is specified by the {@link IElementReference}.
 */
public interface IElementType extends IDataObject {

	/**
	 * @return the attributes that can be set for the element. May be empty!
	 */
	Collection<IAttribute> getAttributes();

	/**
	 * This enum describes what kind of contents can be found inside this element.
	 */
	enum ContentType {
		/**
		 * The element does not contain any sub-elements or content.
		 */
		EMPTY,
		/**
		 * The element contains data of a certain type.
		 */
		DATA,
		/**
		 * The element only contains other elements.
		 */
		ELEMENT,
		/**
		 * The element contains a variety of text and element content.
		 */
		MIXED
	}

	/**
	 * @return the content type of the element.
	 */
	ContentType getContentType();

	/**
	 * @return <code>true</code> if the element contains data of a certain type.
	 */
	boolean hasDataContent();

	/**
	 * @return <code>true</code> if the element only contains other elements.
	 */
	boolean hasElementContent();

	/**
	 * @return <code>true</code> if the element contains a variety if text and element content
	 */
	boolean hasMixedContent();

	/**
	 * @return the elements that can be encountered inside this element. Only set if the content type is
	 *         {@link ContentType#ELEMENT} or {@link ContentType#MIXED}.
	 */
	List<IElementReference> getElements();

	/**
	 * The mode in which the sub-elements of an element can be arranged.
	 */
	enum ElementArrangement {
		/**
		 * All sub-elements may occur in any order according to the multiplicity specified by the element references.
		 */
		ALL,
		/**
		 * The sub-elements must occur in the order specified according to the multiplicity specified by the element
		 * references.
		 */
		SEQUENCE,
		/**
		 * Only one of the sub-elements may occur. The multiplicity specifications of the element references are
		 * disregarded.
		 */
		CHOICE
	}

	/**
	 * Determines the mode in which the sub-elements specified by {@link #getElements()} can be arranged. See
	 * {@link ElementArrangement}. Only set if the content type is {@link ContentType#ELEMENT}.
	 *
	 * @return the mode in which the sub-elements can be arranged
	 */
	ElementArrangement getElementArrangement();

	/**
	 * @return <code>true</code> if the contents of the element can be influenced by user input. Only set if the content
	 *         type is {@link ContentType#DATA} or {@link ContentType#MIXED}.
	 */
	Optional<Boolean> isUserModifiable();

}
