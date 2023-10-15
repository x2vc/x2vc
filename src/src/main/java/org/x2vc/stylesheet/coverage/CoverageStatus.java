package org.x2vc.stylesheet.coverage;

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