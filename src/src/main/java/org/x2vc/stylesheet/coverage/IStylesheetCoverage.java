package org.x2vc.stylesheet.coverage;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This object is used to track which elements of the stylesheet have been
 * evaluated. It is created based on the XSLT stylesheet structure information.
 *
 * This object can be serialized to transmit the results. It is possible to add
 * coverage statistics.
 */
public interface IStylesheetCoverage extends Serializable {

	/**
	 * Records the coverage of an element that has been marked for tracing.
	 *
	 * @param traceID    the tracing ID of the element
	 * @param parameters the variable and parameter values recorded by the trace
	 */
	void recordElementCoverage(int traceID, Map<String, String> parameters);

	/**
	 * Determines the number of times an element was covered
	 *
	 * @param traceID the tracing ID of the element
	 * @return the number of times the element was covered
	 */
	int getElementCoverage(int traceID);

	/**
	 * Retrieves an overview of the coverage data of all elements being traced
	 *
	 * @return a map assigning the number of executions (value) to each tracing ID
	 *         (key)
	 */
	Map<Integer, Integer> getElementCoverage();

	/**
	 * Retrieves the parameter sets that were recorded by the trace so far. Note
	 * that duplicate parameter sets may be dropped.
	 *
	 * @param traceID the tracing ID of the element
	 * @return a list of all unique parameter values recorded during tracing
	 */
	List<Map<String, String>> getCoverageParameters(int traceID);

	/**
	 * Returns a new coverage object that contains the sum of this object and the
	 * argument object.
	 *
	 * @param otherObject
	 * @return a new object constituting the sum of the two coverage objects
	 */
	IStylesheetCoverage add(IStylesheetCoverage otherObject);

}
