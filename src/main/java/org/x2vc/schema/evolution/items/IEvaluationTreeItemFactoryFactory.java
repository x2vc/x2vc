/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
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
