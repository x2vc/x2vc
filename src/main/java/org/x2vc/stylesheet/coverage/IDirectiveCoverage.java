package org.x2vc.stylesheet.coverage;

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

import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;
import org.x2vc.utilities.PolymorphLocation;

/**
 * Information about the collected coverage information concerning a single directive of the stylesheet.
 */
public interface IDirectiveCoverage {

	/**
	 * @return the starting location of the directive
	 */
	PolymorphLocation getStartLocation();

	/**
	 * @return the ending location of the directive
	 */
	PolymorphLocation getEndLocation();

	/**
	 * @return the directive as read from the stylesheet
	 */
	IXSLTDirectiveNode getDirective();

	/**
	 * @return the coverage status
	 */
	CoverageStatus getCoverage();

	/**
	 * @return the number of times the directive was processed
	 */
	int getExecutionCount();

}
