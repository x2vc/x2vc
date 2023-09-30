package org.x2vc.schema.evolution;

import java.util.Collection;

import org.x2vc.schema.structure.IXMLSchema;

/**
 * This component applies a set of {@link ISchemaModifier}s to an {@link IXMLSchema} to generate a new version of the
 * schema.
 *
 */
public interface ISchemaModificationProcessor {

	/**
	 * Applies the modifiers to the input schema and generates a new schema.
	 *
	 * @param inputSchema
	 * @param modifiers
	 * @return the next version of the schema
	 */
	IXMLSchema modifySchema(IXMLSchema inputSchema, Collection<ISchemaModifier> modifiers);

}
