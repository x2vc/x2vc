package org.x2vc.schema.structure;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A XML schema element type, representing an element in the XML document. This
 * object specifies everything but the element name, which is specified by the
 * {@link IXMLElementReference}.
 */
public interface IXMLElementType extends IXMLSchemaObject {

	/**
	 * @return the attributes that can be set for the element. May be empty!
	 */
	ImmutableSet<IXMLAttribute> getAttributes();

	/**
	 * This enum describes what kind of contents can be found inside this element.
	 */
	enum ContentType {
		/**
		 * The element may contain any kind of unstructured text.
		 */
		TEXT,
		/**
		 * The element contains data of a certain type.
		 */
		DATA,
		/**
		 * The element only contains other elements.
		 */
		ELEMENT,
		/**
		 * The element contains a variety if text and element content.
		 */
		MIXED
	}

	/**
	 * @return the content type of the element.
	 */
	ContentType getContentType();

	/**
	 * @return <code>true</code> if the element contains unstructured text.
	 */
	boolean hasTextContent();

	/**
	 * @return <code>true</code> if the element contains data of a certain type.
	 */
	boolean hasDataContent();

	/**
	 * @return <code>true</code> if the element only contains other elements.
	 */
	boolean hasElementContent();

	/**
	 * @return <code>true</code> if the element contains a variety if text and
	 *         element content
	 */
	boolean hasMixedContent();

	/**
	 * @return the data type of the element content. Only set if the content type is
	 *         {@link ContentType#DATA}.
	 */
	XMLDatatype getDatatype();

	/**
	 * @return the maximum length of the value. Only set if the content type is
	 *         {@link ContentType#DATA}, only supported for
	 *         {@link XMLDatatype#STRING}.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * @return The minimum value of the attribute. Only set if the content type is
	 *         {@link ContentType#DATA}. Only supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMinValue();

	/**
	 * @return The maximum value of the attribute. Only set if the content type is
	 *         {@link ContentType#DATA}, only supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMaxValue();

	/**
	 * @return the discrete values specified for this attribute. Only set if the
	 *         content type is {@link ContentType#DATA}. See
	 *         {@link #isFixedValueset()} for additional information on how to
	 *         interpret this value.
	 */
	ImmutableSet<IXMLDiscreteValue> getDiscreteValues();

	/**
	 * Determines whether a set of discrete values specified for the attribute
	 * represent a fixed value set (i.e. a closed list of the only valid values) or
	 * a list of "interesting" values that should be checked to improve coverage,
	 * but do not comprise a restriction of valid values. Only set if the content
	 * type is {@link ContentType#DATA}.
	 *
	 * @return <code>true</code> if the values specified using
	 *         {@link #getDiscreteValues()} represent a fixed value set.
	 */
	Boolean isFixedValueset();

	/**
	 * @return the elements that can be encountered inside this element. Only set if
	 *         the content type is {@link ContentType#ELEMENT} or
	 *         {@link ContentType#MIXED}.
	 */
	ImmutableList<IXMLElementReference> getElements();

	/**
	 * The mode in which the sub-elements of an element can be arranged.
	 */
	enum ElementArrangement {
		/**
		 * All sub-elements may occur in any order according to the multiplicity
		 * specified by the element references.
		 */
		ALL,
		/**
		 * The sub-elements must occur in the order specified according to the
		 * multiplicity specified by the element references.
		 */
		SEQUENCE,
		/**
		 * Only one of the sub-elements may occur. The multiplicity specifications of
		 * the element references are disregarded.
		 */
		CHOICE
	}

	/**
	 * Determines the mode in which the sub-elements specified by
	 * {@link #getElements()} can be arranged. See {@link ElementArrangement}. Only
	 * set if the content type is {@link ContentType#ELEMENT}.
	 *
	 * @return the mode in which the sub-elements can be arranged
	 */
	ElementArrangement getElementArrangement();

	/**
	 * @return <code>true</code> if the contents of the element can be influenced by
	 *         user input. Only set if the content type is {@link ContentType#TEXT},
	 *         {@link ContentType#DATA} or {@link ContentType#MIXED}.
	 */
	Optional<Boolean> isUserModifiable();

}
