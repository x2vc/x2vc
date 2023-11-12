package org.x2vc.stylesheet;

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

import java.net.URI;

/**
 * This component prepares an XSLT stylesheet for further processing. During the
 * preparation, the original XSLT stylesheet undergoes the following steps:
 * <ul>
 * <li>compile the original stylesheet to check for syntax errors or other
 * irregularities (the results of this compilation are then discarded);</li>
 * <li>check whether one of the unsupported features is used (version > 1.0,
 * xsl:import, xsl:include, xsl:apply-imports, ...)</li>
 * <li>extend the original stylesheet with message elements to enable tracing of
 * the execution (this is deferred to the XSLT Stylesheet extender) and compile
 * the extended stylesheet and</li>
 * <li>store the resulting precompiled stylesheet and the structure information
 * for later use by the XSLT processor.</li>
 * </ul>
 * The process also extracts the stylesheet structure information that can be
 * used to interpret the messages resulting from the execution.
 *
 * Known limitations: Does not support multi-file stylesheets (import and
 * include statements as listed above). Will need major structural changes for
 * that (probably a URIResolver added to the XSLT processors).
 */

public interface IStylesheetPreprocessor {

	/**
	 * Prepares an XSLT stylesheet for further processing. See the interface
	 * documentation for more information on the process.
	 *
	 * @param uri            the URI of the stylesheet
	 * @param originalSource the source code of the stylesheet
	 * @return an {@link IStylesheetInformation} object containing the assembled
	 *         information
	 * @throws IllegalArgumentException if an invalid stylesheet source was passed
	 */
	IStylesheetInformation prepareStylesheet(URI uri, String originalSource) throws IllegalArgumentException;

}
