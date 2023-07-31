package org.x2vc.stylesheet.coverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IStylesheetCoverage}.
 */
public class StylesheetCoverage implements IStylesheetCoverage {

	private static final long serialVersionUID = 8825773547954992141L;
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
		for (IXSLTDirectiveNode directive : this.parentStructure.getDirectivesWithTraceID()) {
			Optional<Integer> traceID = directive.getTraceID();
			if (traceID.isPresent()) {
				this.executionCount.put(traceID.get(), 0);
			}
		}
		this.executionParameters = HashMultimap.create();
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
		// TODO ensure that the other object relates to the same structure
		ImmutableMap<Integer, Integer> otherCoverage = otherObject.getElementCoverage();
		for (Map.Entry<Integer, Integer> entry : otherCoverage.entrySet()) {
			if (entry.getValue() > 0) {
				this.executionCount.put(entry.getKey(), this.executionCount.get(entry.getKey()) + entry.getValue());
				this.executionParameters.putAll(entry.getKey(), otherObject.getCoverageParameters(entry.getKey()));
			}
		}
	}

	/**
	 * Ensures that the trace ID is known
	 *
	 * @param traceID
	 */
	private void validateTraceID(int traceID) {
		if (!this.executionCount.containsKey(traceID)) {
			throw new IllegalArgumentException(
					String.format("No traced element with ID %s found in stylesheet information", traceID));
		}
	}

}
