package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.Set;

/**
 * A subset of {@link IXMLSchemaObject} that can contain typed data, like an
 * attribute or an element with content type DATA.
 */
public interface IXMLDataObject extends IXMLSchemaObject {

	/**
	 * @return the data type of the data
	 */
	XMLDatatype getDatatype();

	/**
	 * @return the maximum length of the data. Supported for
	 *         {@link XMLDatatype#STRING}.
	 */
	Optional<Integer> getMaxLength();

	/**
	 * @return The minimum value of the data. Supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMinValue();

	/**
	 * @return The maximum value of the data. Supported for
	 *         {@link XMLDatatype#INTEGER}.
	 */
	Optional<Integer> getMaxValue();

	/**
	 * @return the discrete values specified for the data. See
	 *         {@link #isFixedValueset()} for additional information on how to
	 *         interpret this value.
	 */
	Set<IXMLDiscreteValue> getDiscreteValues();

	/**
	 * Determines whether a set of discrete values specified for the data represent
	 * a fixed value set (i.e. a closed list of the only valid values) or a list of
	 * "interesting" values that should be checked to improve coverage, but do not
	 * comprise a restriction of valid values
	 *
	 * @return <code>true</code> if the values specified using
	 *         {@link #getDiscreteValues()} represent a fixed value set. Not present
	 *         if no discrete values are specified.
	 */
	Optional<Boolean> isFixedValueset();

}
