package org.x2vc.schema.evolution;

import java.util.function.Consumer;

import org.x2vc.schema.structure.IXMLSchema;

/**
 * Factory to create configured instances of {@link IModifierCreationCoordinator}.
 */
public interface IModifierCreationCoordinatorFactory {

	/**
	 * Creates a new coordinator.
	 *
	 * @param schema
	 * @param modifierCollector
	 * @return the new coordinator
	 */
	IModifierCreationCoordinator createCoordinator(IXMLSchema schema, Consumer<ISchemaModifier> modifierCollector);

}
