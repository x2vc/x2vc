package org.x2vc.schema.evolution;

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
