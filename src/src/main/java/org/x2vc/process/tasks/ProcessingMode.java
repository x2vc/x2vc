package org.x2vc.process.tasks;

/**
 * Describes the actions to perform when processing an XSLT file.
 */
public enum ProcessingMode {

	/**
	 * Perform both the XSS check and the schema evolution.
	 */
	FULL,

	/**
	 * Omit the XSS check and only perform the schema evolution.
	 */
	SCHEMA_ONLY,

	/**
	 * Only perform the XSS check and omit the schema evolution.
	 */
	XSS_ONLY

}
