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
