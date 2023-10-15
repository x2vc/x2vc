package org.x2vc.stylesheet.coverage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.structure.IXSLTDirectiveNode;
import org.x2vc.utilities.PolymorphLocation;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * Internal class used by the {@link CoverageTraceAnalyzer}.
 */
class CoverageTreeNode {

	private static final Logger logger = LogManager.getLogger();

	private IXSLTDirectiveNode directiveNode;
	private Range<PolymorphLocation> area;
	private List<CoverageTreeNode> children;
	private int executionCount = 0;

	CoverageTreeNode(IXSLTDirectiveNode directiveNode) {
		super();
		this.directiveNode = directiveNode;
		this.area = Range.closed(directiveNode.getStartLocation().orElseThrow(),
				directiveNode.getEndLocation().orElseThrow());
		this.children = directiveNode.getChildDirectives()
			.stream()
			.map(CoverageTreeNode::new)
			.toList();
	}

	protected Optional<CoverageTreeNode> findNode(PolymorphLocation location) {
		final List<CoverageTreeNode> matchingChildren = this.children
			.stream()
			.map(c -> c.findNode(location))
			.filter(Optional<CoverageTreeNode>::isPresent)
			.map(Optional<CoverageTreeNode>::get)
			.toList();
		if (matchingChildren.isEmpty()) {
			if (this.area.contains(location)) {
				return Optional.of(this);
			} else {
				return Optional.empty();
			}
		} else if (matchingChildren.size() == 1) {
			return Optional.of(matchingChildren.get(0));
		} else {
			throw new IllegalStateException(String.format("The location %d/%d is covered by multiple directives",
					location.getLineNumber(), location.getColumnNumber()));
		}
	}

	@Override
	public String toString() {
		return "directive " + this.directiveNode.getName() + " at " + this.area;
	}

	public void incrementExecutionCounter() {
		this.executionCount++;
	}

	public List<IDirectiveCoverage> getDirectiveCoverage() {
		final DirectiveCoverage ownCoverage = new DirectiveCoverage(this.directiveNode, this.executionCount,
				getCoverageStatus());
		final List<IDirectiveCoverage> result = Lists.newArrayList(ownCoverage);
		this.children.forEach(child -> result.addAll(child.getDirectiveCoverage()));
		return result;
	}

	private CoverageStatus getCoverageStatus() {
		if (this.children.isEmpty()) {
			return (this.executionCount > 0) ? CoverageStatus.FULL : CoverageStatus.NONE;
		} else {
			final Set<CoverageStatus> childStatus = this.children
				.stream()
				.map(c -> c.getCoverageStatus())
				.filter(s -> s != CoverageStatus.EMPTY) // shouldn't occur here, so away with it
				.collect(Collectors.toSet());
			// possible states:
			// FULL --> FULL
			// FULL, PARTIAL --> PARTIAL
			// FULL, PARTIAL, NONE --> PARTIAL
			// FULL, NONE --> PARTIAL
			// PARTIAL --> PARTIAL
			// PARTIAL, NONE --> PARTIAL
			// NONE --> NONE
			// empty --> NONE
			if ((childStatus.size() == 1) && (childStatus.contains(CoverageStatus.FULL))) {
				return CoverageStatus.FULL;
			} else if ((childStatus.isEmpty())
					|| ((childStatus.size() == 1) && (childStatus.contains(CoverageStatus.NONE)))) {
				return CoverageStatus.NONE;
			} else {
				return CoverageStatus.PARTIAL;
			}
		}
	}

	private boolean spansLine(int lineNumber) {
		return ((this.directiveNode.getStartLocation().orElseThrow().getLineNumber() <= lineNumber)
				&&
				(this.directiveNode.getEndLocation().orElseThrow().getLineNumber() >= lineNumber));
	}

	public CoverageStatus getCoverageStatusAtLine(int lineNumber) {
		logger.traceEntry("checking line {} for {}", lineNumber, this);
		// caution: a line might contain multiple nodes!
		CoverageStatus result = CoverageStatus.EMPTY;
		if (this.children.isEmpty()) {
			if (spansLine(lineNumber)) {
				result = (this.executionCount > 0) ? CoverageStatus.FULL : CoverageStatus.NONE;
			} else {
				logger.warn(
						"coverage tree node asked to report on line status outside of its own range - please investigate");
			}
		} else {
			// select the child nodes that cover the line
			final Set<CoverageStatus> childStatus = this.children
				.stream()
				.filter(c -> c.spansLine(lineNumber))
				.map(c -> c.getCoverageStatusAtLine(lineNumber))
				.filter(s -> s != CoverageStatus.EMPTY) // make it easier to handle the mapping
				.collect(Collectors.toSet());
			// possible states:
			// FULL --> FULL
			// FULL, PARTIAL --> PARTIAL
			// FULL, PARTIAL, NONE --> PARTIAL
			// FULL, NONE --> PARTIAL
			// PARTIAL --> PARTIAL
			// PARTIAL, NONE --> PARTIAL
			// NONE --> NONE
			// empty --> EMPTY
			if (childStatus.isEmpty()) {
				result = getCoverageStatus();
			} else if ((childStatus.size() == 1) && (childStatus.contains(CoverageStatus.FULL))) {
				result = CoverageStatus.FULL;
			} else if ((childStatus.size() == 1) && (childStatus.contains(CoverageStatus.NONE))) {
				result = CoverageStatus.NONE;
			} else {
				result = CoverageStatus.PARTIAL;
			}

		}
		return logger.traceExit(result);
	}

	/**
	 * @return the last line number
	 */
	public int getEndLine() {
		return this.directiveNode.getEndLocation().orElseThrow().getLineNumber();
	}

}