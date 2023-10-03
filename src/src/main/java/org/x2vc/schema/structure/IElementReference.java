package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.UUID;

/**
 * A reference to an {@link IElementType}.
 */
public interface IElementReference extends ISchemaObject {

	/**
	 * @return the name of the element
	 */
	String getName();

	/**
	 * @return the ID of the referred element
	 */
	UUID getElementID();

	/**
	 * @return the referred element
	 */
	IElementType getElement();

	/**
	 * @return the minimum number of times the element should occur at the referred
	 *         position. Defaults to 0.
	 */
	Integer getMinOccurrence();

	/**
	 * @return the maximum number of times the element should occur at the referred
	 *         position. If unset, there is no upper limit set.
	 */
	Optional<Integer> getMaxOccurrence();
}
