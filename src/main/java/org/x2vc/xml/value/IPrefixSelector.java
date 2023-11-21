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
package org.x2vc.xml.value;


import java.net.URI;

/**
 * This component determines a unique prefix to use when generating the random
 * values for an XML document as well as the default length of the values.
 */
public interface IPrefixSelector {

	/**
	 * @param prefix      a unique prefix to use when generating the random values
	 *                    for an XML document
	 * @param valueLength the default length of the values
	 */
	public record PrefixData(String prefix, Integer valueLength) {
	}

	/**
	 * Randomly generates a unique prefix to generate values for a stylesheet.
	 *
	 * @param stylesheetURI the URI of the stylesheet
	 * @return the prefix and value length
	 */
	PrefixData selectPrefix(URI stylesheetURI);

}
