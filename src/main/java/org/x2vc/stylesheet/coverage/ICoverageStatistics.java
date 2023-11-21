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
 * A data object to represent the overall coverage statistics collected for a single stylesheet.
 */
public interface ICoverageStatistics {

	/**
	 * @return the total number of XSLT directives
	 */
	long getTotalDirectiveCount();

	/**
	 * @return the number of XSLT directives with full coverage
	 */
	long getDirectiveCountWithFullCoverage();

	/**
	 * @return the number of XSLT directives with partial coverage
	 */
	long getDirectiveCountWithPartialCoverage();

	/**
	 * @return the number of XSLT directives with no coverage
	 */
	long getDirectiveCountWithNoCoverage();

	/**
	 * @return the percentage of XSLT directives with full coverage
	 */
	double getDirectivePercentageWithFullCoverage();

	/**
	 * @return the percentage of XSLT directives with partial coverage
	 */
	double getDirectivePercentageWithPartialCoverage();

	/**
	 * @return the percentage of XSLT directives with no coverage
	 */
	double getDirectivePercentageWithNoCoverage();

	/**
	 * @return the total number of lines
	 */
	long getTotalLineCount();

	/**
	 * @return the number of lines that are considered empty
	 */
	long getLineCountEmpty();

	/**
	 * @return the number of lines with full coverage
	 */
	long getLineCountWithFullCoverage();

	/**
	 * @return the number of lines with partial coverage
	 */
	long getLineCountWithPartialCoverage();

	/**
	 * @return the number of lines with no coverage
	 */
	long getLineCountWithNoCoverage();

	/**
	 * @return the percentage of lines that are considered empty
	 */
	double getLinePercentageEmpty();

	/**
	 * @return the percentage of lines with full coverage
	 */
	double getLinePercentageWithFullCoverage();

	/**
	 * @return the percentage of lines with no coverage
	 */
	double getLinePercentageWithPartialCoverage();

	/**
	 * @return the percentage of lines with no coverage
	 */
	double getLinePercentageWithNoCoverage();

}
