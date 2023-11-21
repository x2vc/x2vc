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
package org.x2vc.xml.request;


import java.util.Optional;
import java.util.UUID;

/**
 * An {@link IGenerationRule} to add raw (i.e. unsanitized) content to an
 * element.
 */
public interface IAddRawContentRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element in the schema description that allows for the
	 *         mixed content to be inserted
	 */
	UUID getElementID();

	/**
	 * @return a reference to the requested value for the content, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
