package org.x2vc.process.tasks;

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

import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.x2vc.schema.evolution.ISchemaModifier;

/**
 * Factory to obtain instances of {@link ISchemaExplorationTask}.
 */
public interface ISchemaExplorationTaskFactory {

	/**
	 * Creates a new {@link ISchemaExplorationTask}
	 *
	 * @param xsltFile
	 * @param modifierCollector
	 * @param callback
	 * @return the task
	 */
	ISchemaExplorationTask create(File xsltFile, Consumer<ISchemaModifier> modifierCollector,
			BiConsumer<UUID, Boolean> callback);

}
