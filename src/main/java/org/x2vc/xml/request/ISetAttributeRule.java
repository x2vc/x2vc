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
 * An {@link IGenerationRule} to set an attribute of an element.
 */
public interface ISetAttributeRule extends IGenerationRule {

	/**
	 * @return the ID of the attribute in the schema description
	 */
	UUID getAttributeID();

	/**
	 * @return a reference to the requested value for the attribute, if set
	 */
	Optional<IRequestedValue> getRequestedValue();

}
