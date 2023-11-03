package org.x2vc.stylesheet.coverage;

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

/**
 * An indicator of the coverage of a line or a directive.
 */
public enum CoverageStatus {
	/**
	 * For a line: All directives covering the line were covered at least once. For a directive: All subordinate
	 * directives (if any) were covered at least once.
	 */
	FULL,
	/**
	 * For a line: Only some of the directives covering the line were covered at least once. For a directive: Only
	 * some of the subordinate directives were covered at least once.
	 */
	PARTIAL,
	/**
	 * For a line: None of the directives covering the line were covered. For a directive: The directive was not
	 * covered.
	 */
	NONE,
	/**
	 * For lines only: The line is not claimed by any directive (e.g. empty lines or comments between templates).
	 */
	EMPTY
}
