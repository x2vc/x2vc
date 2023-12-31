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
package org.x2vc.stylesheet.coverage;


/**
 * Information about the collected coverage information concerning a line of the stylesheet. This is mostly intended to
 * produce the output format.
 */
public interface ILineCoverage {

	/**
	 * @return the line number
	 */
	int getLineNumber();

	/**
	 * @return the XSLT source code of the line
	 */
	String getContents();

	/**
	 * @return the coverage status
	 */
	CoverageStatus getCoverage();

}
