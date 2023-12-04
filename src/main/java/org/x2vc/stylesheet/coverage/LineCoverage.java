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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Standard implementation of {@link ILineCoverage}.
 */
public final class LineCoverage implements ILineCoverage {

	@XmlAttribute(name = "number")
	private final int lineNumber;

	@XmlValue
	private final String contents;

	@XmlAttribute
	private final CoverageStatus coverage;

	/**
	 * Default constructor required for JAXB operation.
	 */
	protected LineCoverage() {
		this.lineNumber = 0;
		this.contents = "";
		this.coverage = CoverageStatus.EMPTY;
	}

	/**
	 * Creates a new coverage entry.
	 *
	 * @param lineNumber
	 * @param contents
	 * @param coverage
	 */
	public LineCoverage(int lineNumber, String contents, CoverageStatus coverage) {
		super();
		this.lineNumber = lineNumber;
		this.contents = contents
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("'", "&apos;")
			.replace("\"", "&quot;");
		this.coverage = coverage;
	}

	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public String getContents() {
		return this.contents;
	}

	@Override
	public CoverageStatus getCoverage() {
		return this.coverage;
	}

	@Override
	public String toString() {
		return "coverage of line " + this.lineNumber + " (" + this.contents + "): " + this.coverage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.contents, this.coverage, this.lineNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LineCoverage)) {
			return false;
		}
		final LineCoverage other = (LineCoverage) obj;
		return Objects.equals(this.contents, other.contents) && this.coverage == other.coverage
				&& this.lineNumber == other.lineNumber;
	}

}
