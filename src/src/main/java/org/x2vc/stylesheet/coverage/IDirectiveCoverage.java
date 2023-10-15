package org.x2vc.stylesheet.coverage;

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
