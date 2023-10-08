package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.structure.IXMLSchema;

/**
 * Yup. A factory to create a factory. No, that's not a joke.
 *
 * Reason: The {@link IEvaluationTreeItemFactory} is a contextual object that passes references to the items it creates.
 * Somehow, these references need to be passed to the factory - in this case via Assisted Injection. Hence - a
 * FactoryFactory.
 */
public interface IEvaluationTreeItemFactoryFactory {

	/**
	 * @param schema
	 * @param coordinator
	 * @return a new factory for items referring to the schema provided
	 */
	IEvaluationTreeItemFactory createFactory(IXMLSchema schema, IModifierCreationCoordinator coordinator);

}
