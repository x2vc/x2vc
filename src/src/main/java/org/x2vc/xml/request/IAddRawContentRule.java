package org.x2vc.xml.request;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to add raw (i.e. unsanitized) content to an
 * element.
 */
public interface IAddRawContentRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element in the schema description that allows for the
	 *         mixed content to be inserted
	 */
	UUID getElementID();

	/**
	 * @return a reference to the requested value for the content, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
