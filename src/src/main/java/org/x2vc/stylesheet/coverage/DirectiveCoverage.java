package org.x2vc.stylesheet.coverage;

import java.util.Objects;

import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;
import org.x2vc.utilities.PolymorphLocation;

/**
 * Standard implementation of {@link IDirectiveCoverage}.
 */
@SuppressWarnings("java:S6206") // class will likely be expanded down the line
public final class DirectiveCoverage implements IDirectiveCoverage {

	private final IXSLTDirectiveNode directive;
	private final int executionCount;
	private final CoverageStatus coverage;

	/**
	 * @param directive
	 * @param executionCount
	 * @param coverage
	 */
	public DirectiveCoverage(IXSLTDirectiveNode directive, int executionCount, CoverageStatus coverage) {
		super();
		this.directive = directive;
		this.executionCount = executionCount;
		this.coverage = coverage;
	}

	@Override
	public PolymorphLocation getStartLocation() {
		return this.directive.getStartLocation().orElseThrow();
	}

	@Override
	public PolymorphLocation getEndLocation() {
		return this.directive.getEndLocation().orElseThrow();
	}

	@Override
	public IXSLTDirectiveNode getDirective() {
		return this.directive;
	}

	@Override
	public CoverageStatus getCoverage() {
		return this.coverage;
	}

	@Override
	public int getExecutionCount() {
		return this.executionCount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coverage, this.directive, this.executionCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DirectiveCoverage)) {
			return false;
		}
		final DirectiveCoverage other = (DirectiveCoverage) obj;
		return this.coverage == other.coverage && Objects.equals(this.directive, other.directive)
				&& this.executionCount == other.executionCount;
	}

}
