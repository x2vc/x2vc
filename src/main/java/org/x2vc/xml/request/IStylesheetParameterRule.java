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
 * An {@link IGenerationRule} to register a value for a template parameter to pass to the XSLT processor.
 */
public interface IStylesheetParameterRule extends IGenerationRule {

	/**
	 * @return the ID of the parameter as registered in the schema
	 */
	UUID getParameterID();

	/**
	 * @return a reference to the requested return value
	 */
	Optional<IRequestedValue> getRequestedValue();

}
