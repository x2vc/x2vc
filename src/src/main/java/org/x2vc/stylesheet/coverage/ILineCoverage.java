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
	 * @return the coverage status
	 */
	CoverageStatus getCoverage();

}
