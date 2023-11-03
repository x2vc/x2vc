package org.x2vc.schema.structure;

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

import java.util.Optional;
import java.util.UUID;

/**
 * Common interface for all schema objects.
 */
public interface ISchemaObject {

	/**
	 * @return the schema element ID
	 */
	UUID getID();

	/**
	 * @return an optional comment describing the schema object.
	 */
	Optional<String> getComment();

}
