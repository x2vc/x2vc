package org.x2vc.schema.structure;

import java.util.Collection;
import java.util.Optional;

/**
 * A subset of {@link ISchemaObject} that can contain typed data, like an attribute or an element with content type
 * DATA.
 */
public interface IDataObject extends ISchemaObject {

	/**
	 * @return the data type of the data
	 */
	XMLDataType getDataType();

	/**
	 * @return the maximum length of the data. Supported for {@link XMLDataType#STRING}.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * @return The minimum value of the data. Supported for {@link XMLDataType#INTEGER}.
	 */
	Optional<Integer> getMinValue();

	/**
	 * @return The maximum value of the data. Supported for {@link XMLDataType#INTEGER}.
	 */
	Optional<Integer> getMaxValue();

	/**
	 * @return the discrete values specified for the data. See {@link #isFixedValueset()} for additional information on
	 *         how to interpret this value.
	 */
	Collection<IDiscreteValue> getDiscreteValues();

	/**
	 * Determines whether a set of discrete values specified for the data represent a fixed value set (i.e. a closed
	 * list of the only valid values) or a list of "interesting" values that should be checked to improve coverage, but
	 * do not comprise a restriction of valid values
	 *
	 * @return <code>true</code> if the values specified using {@link #getDiscreteValues()} represent a fixed value set.
	 *         Not present if no discrete values are specified.
	 */
	Optional<Boolean> isFixedValueset();

}
