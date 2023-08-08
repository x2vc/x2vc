package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.Set;

/**
 * Attribute of an XML Element.
 */
public interface IXMLAttribute extends IXMLSchemaObject {

	/**
	 * @return the name of the attribute
	 */
	String getName();

	/**
	 * @return the data type of the attribute
	 */
	XMLDatatype getType();

	/**
	 * @return <code>true</code> if the attribute is optional
	 */
	boolean isOptional();

	/**
	 * @return the maximum length of the value. Supported for
	 *         {@link XMLDatatype#STRING}.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * @return The minimum value of the attribute. Supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMinValue();

	/**
	 * @return The maximum value of the attribute. Supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMaxValue();

	/**
	 * @return the discrete values specified for this attribute. See
	 *         {@link #isFixedValueset()} for additional information on how to
	 *         interpret this value.
	 */
	Set<IXMLDiscreteValue> getDiscreteValues();

	/**
	 * Determines whether a set of discrete values specified for the attribute
	 * represent a fixed value set (i.e. a closed list of the only valid values) or
	 * a list of "interesting" values that should be checked to improve coverage,
	 * but do not comprise a restriction of valid values
	 *
	 * @return <code>true</code> if the values specified using
	 *         {@link #getDiscreteValues()} represent a fixed value set.
	 */
	boolean isFixedValueset();

	/**
	 * @return <code>true</code> if the value of the attribute can be influenced by
	 *         user input.
	 */
	boolean isUserModifiable();

}
