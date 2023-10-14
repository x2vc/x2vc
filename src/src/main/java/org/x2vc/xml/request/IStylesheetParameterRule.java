package org.x2vc.xml.request;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to register an value for a template parameter to pass to the XSLT processor.
 */
public interface IStylesheetParameterRule extends IGenerationRule {

	/**
	 * @return the ID of the parameter as registered in the schema
	 */
	UUID getParameterID();

	/**
	 * @return a reference to the requested return value
	 */
	Optional<IRequestedValue> getRequestedValue();

}
