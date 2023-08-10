package org.x2vc.xml.request;

import java.io.Serializable;
import java.util.Optional;

import org.x2vc.xml.document.IDocumentModifier;

/**
 * A request to the document generator to use a certain value for a particular
 * target element.
 */
public interface IRequestedValue extends Serializable {

	/**
	 * @return the requested value
	 */
	String getValue();

	/**
	 * @return information about the component that requested the value, if
	 *         available
	 */
	Optional<IDocumentModifier> getModifier();

}
