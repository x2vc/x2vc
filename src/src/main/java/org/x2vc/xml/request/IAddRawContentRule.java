package org.x2vc.xml.request;

import java.util.Optional;

/**
 * An {@link IGenerationRule} to add raw (i.e. unsanitized) content to
 * an element.
 */
public interface IAddRawContentRule extends IContentGenerationRule {

	/**
	 * @return a reference to the requested value for the content, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
