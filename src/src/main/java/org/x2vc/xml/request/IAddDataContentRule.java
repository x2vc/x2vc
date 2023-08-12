package org.x2vc.xml.request;

import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to add data or text content to an element.
 */
public interface IAddDataContentRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element in the schema description that specifies what
	 *         kind of data content has to be generated
	 */
	UUID getElementID();

	/**
	 * @return a reference to the requested value for the text or data content, if
	 *         set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
