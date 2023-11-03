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

import org.x2vc.xml.document.IDocumentModifier;

/**
 * A request to the document generator to use a certain value for a particular
 * target element.
 */
public interface IRequestedValue {

	/**
	 * @return the requested value
	 */
	String getValue();

	/**
	 * @return information about the component that requested the value, if
	 *         available
	 */
	Optional<IDocumentModifier> getModifier();

	/**
	 * Creates a normalized copy of the value. The normalized value is equal to the
	 * original rule in all functional aspects, i.e. incorporating it will cause the
	 * same effects to the document. To make the normalized values comparable,
	 * attributes that do not directly influence the generation process like the
	 * modifier are normalized or removed.
	 *
	 * @return a normalized copy of the value
	 */
	IRequestedValue normalize();
}
