package org.x2vc.xml.request;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to set an attribute of an element.
 */
public interface ISetAttributeRule extends IGenerationRule {

	/**
	 * @return the ID of the attribute in the schema description
	 */
	UUID getAttributeID();

	/**
	 * @return a reference to the requested value for the attribute, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
