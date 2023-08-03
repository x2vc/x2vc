package org.x2vc.xmldoc;

import java.io.Serializable;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

/**
 * This object contains information about the XML data that is contained in a
 * specific instance of an XML document. It can be used to examine the contents
 * of the HTML document and determine which parts of the output can be traced
 * back to input document values. It also links the XML values to the
 * corresponding XML schema elements to provide access to additional properties.
 *
 * The description of each value contains information about the source of the
 * value (i. e. whether the value was randomly selected by the document
 * generator or proposed as a possible XSS attack).
 */
public interface IXMLDocumentDescriptor extends Serializable {

	/**
	 * @return the common prefix of all generated string values
	 */
	String getValuePrefix();

	/**
	 * @return the length of the generated string values
	 */
	int getValueLength();

	/**
	 * @return <code>true</code> if the descriptor contains values that were
	 *         requested by another component
	 */
	boolean isMutated();

	/**
	 * @param value the value to determine the source descriptor for.
	 * @return the set of value descriptors that may have contributed the value
	 */
	Optional<ImmutableSet<IDocumentValueDescriptor>> getValueDescriptors(String value);

}
