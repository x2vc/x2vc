package org.x2vc.schema.evolution;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

/**
 * Base type of an object that describes a modification to an XML schema.
 */
public interface ISchemaModifier {

	/**
	 * @return the URI of the schema itself (containing the URI)
	 */
	URI getSchemaURI();

	/**
	 * @return the version of the schema
	 */
	int getSchemaVersion();

	/**
	 * @return the ID of the schema element being modified
	 */
	UUID getElementID();

	/**
	 * @return the modifiers that this modifier depends upon (e.g. because the dependency adds an element that this
	 *         modifier then adds an attribute to)
	 */
	Set<ISchemaModifier> getDependencies();

	/**
	 * @param otherModifier
	 * @return <code>true</code> if the two modifiers are equal except for the newly generated schema object IDs and
	 *         dependencies
	 */
	boolean equalsIgnoringIDs(ISchemaModifier otherModifier);

	/**
	 * @return a hashCode for the modifier
	 */
	int hashCodeIgnoringIDs();

	/**
	 * @return the total number of modifiers including this modifier and all sub-modifiers
	 */
	int count();

}
