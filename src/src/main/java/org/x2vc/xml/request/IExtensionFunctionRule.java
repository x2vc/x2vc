package org.x2vc.xml.request;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to register an extension function with the XSLT processor.
 */
public interface IExtensionFunctionRule extends IGenerationRule {

	/**
	 * @return the ID of the function as registered in the schema
	 */
	UUID getFunctionID();

	/**
	 * @return a reference to the requested return value
	 */
	Optional<IRequestedValue> getRequestedValue();

}
