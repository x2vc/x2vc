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
