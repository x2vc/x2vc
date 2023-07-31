package org.x2vc.stylesheet.coverage;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
	 * @param traceID    the trace ID of the element
	 * @param parameters the variable and parameter values recorded by the trace
	 */
	void recordElementCoverage(int traceID, Map<String, String> parameters);

	/**
	 * Determines the number of times an element was covered
	 *
	 * @param traceID the trace ID of the element
	 * @return the number of times the element was covered
	 */
	int getElementCoverage(int traceID);

	/**
	 * Retrieves an overview of the coverage data of all elements being traced
	 *
	 * @return a map assigning the number of executions (value) to each trace ID
	 *         (key)
	 */
	ImmutableMap<Integer, Integer> getElementCoverage();

	/**
	 * Retrieves the parameter sets that were recorded by the trace so far. Note
	 * that duplicate parameter sets may be dropped.
	 *
	 * @param traceID the trace ID of the element
	 * @return a list of all unique parameter values recorded during trace
	 */
	ImmutableList<ImmutableMap<String, String>> getCoverageParameters(int traceID);

	/**
	 * Adds the contents of the other coverage object to the current object.
	 *
	 * @param otherObject
	 */
	void add(IStylesheetCoverage otherObject);

}
