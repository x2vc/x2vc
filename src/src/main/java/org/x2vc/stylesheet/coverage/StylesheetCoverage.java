package org.x2vc.stylesheet.coverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IStylesheetCoverage}.
 */
public class StylesheetCoverage implements IStylesheetCoverage {

	private static final Logger logger = LogManager.getLogger();
	private IStylesheetStructure parentStructure;
	private Map<Integer, Integer> executionCount;
	private HashMultimap<Integer, ImmutableMap<String, String>> executionParameters;

	/**
	 * Default constructor.
	 *
	 * @param structure the structure to cover
	 */
	public StylesheetCoverage(IStylesheetStructure structure) {
		this.parentStructure = structure;
		this.executionCount = new HashMap<>();
		// TODO XSLT Coverage: rebuild after structure extraction changes
//		for (final IXSLTDirectiveNode directive : this.parentStructure.getDirectivesWithTraceID()) {
//			final Optional<Integer> traceID = directive.getTraceID();
//			if (traceID.isPresent()) {
//				this.executionCount.put(traceID.get(), 0);
//			}
//		}
		this.executionParameters = HashMultimap.create();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.executionCount, this.executionParameters, this.parentStructure);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StylesheetCoverage other = (StylesheetCoverage) obj;
		return Objects.equals(this.executionCount, other.executionCount)
				&& Objects.equals(this.executionParameters, other.executionParameters)
				&& Objects.equals(this.parentStructure, other.parentStructure);
	}

	@Override
	public void recordElementCoverage(int traceID, Map<String, String> parameters) {
		validateTraceID(traceID);
		this.executionCount.put(traceID, this.executionCount.get(traceID) + 1);
		this.executionParameters.put(traceID, ImmutableMap.copyOf(parameters));
	}

	@Override
	public int getElementCoverage(int traceID) {
		validateTraceID(traceID);
		return this.executionCount.get(traceID);
	}

	@Override
	public ImmutableMap<Integer, Integer> getElementCoverage() {
		return ImmutableMap.copyOf(this.executionCount);
	}

	@Override
	public ImmutableList<ImmutableMap<String, String>> getCoverageParameters(int traceID) {
		validateTraceID(traceID);
		return ImmutableList.copyOf(this.executionParameters.get(traceID));
	}

	@Override
	public void add(IStylesheetCoverage otherObject) {
		logger.traceEntry();
		// TODO XSLT coverage: ensure that the other object relates to the same
		// structure
		final ImmutableMap<Integer, Integer> otherCoverage = otherObject.getElementCoverage();
		for (final Map.Entry<Integer, Integer> entry : otherCoverage.entrySet()) {
			if (entry.getValue() > 0) {
				this.executionCount.put(entry.getKey(), this.executionCount.get(entry.getKey()) + entry.getValue());
				this.executionParameters.putAll(entry.getKey(), otherObject.getCoverageParameters(entry.getKey()));
			}
		}
		logger.traceExit();
	}

	/**
	 * Ensures that the trace ID is known
	 *
	 * @param traceID
	 */
	private void validateTraceID(int traceID) {
		if (!this.executionCount.containsKey(traceID)) {
			throw logger.throwing(new IllegalArgumentException(
					String.format("No traced element with ID %s found in stylesheet information", traceID)));
		}
	}

}
