package org.x2vc.xml.request;

import java.util.Optional;

/**
 * An {@link IGenerationRule} to add text content to an element.
 */
public interface IAddTextContentRule extends IContentGenerationRule {

	/**
	 * @return a reference to the requested value for the text, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
