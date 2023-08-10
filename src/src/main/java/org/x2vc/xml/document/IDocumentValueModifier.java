package org.x2vc.xml.document;

import java.util.Optional;
import java.util.UUID;

/**
 * A type of {@link IDocumentModifier} that requests the modification of a data
 * value.
 */
public interface IDocumentValueModifier extends IDocumentModifier {

	/**
	 * @return the ID of the schema element that describes the value to be modified
	 */
	UUID getSchemaElementID();

	/**
	 * @return the original value that should be replaced.
	 */
	Optional<String> getOriginalValue();

	/**
	 * @return the new value to be used
	 */
	String getReplacementValue();

}
