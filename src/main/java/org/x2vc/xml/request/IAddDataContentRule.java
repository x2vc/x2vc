package org.x2vc.xml.request;

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
 * An {@link IGenerationRule} to add data or text content to an element.
 */
public interface IAddDataContentRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element in the schema description that specifies what
	 *         kind of data content has to be generated
	 */
	UUID getElementID();

	/**
	 * @return a reference to the requested value for the text or data content, if
	 *         set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
