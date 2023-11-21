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
package org.x2vc.process.commands;

/**
 * The state of processing of a single stylesheet.
 */
public enum ProcessState {
	/**
	 * The process has just been created and not started yet.
	 */
	NEW,

	/**
	 * The processing is being initialized. Among other things, the schema for the stylesheet is being loaded or
	 * generated.
	 */
	INITIALIZE,

	/**
	 * The structure of the stylesheet is being examined statically to determine whether the schema needs to be
	 * adjusted.
	 */
	STATIC_CHECK,

	/**
	 * The results of the static analysis phase are being consolidated and the schema is being adjusted.
	 */
	STATIC_RESULT_PROCESSING,

	/**
	 * Sample documents are being processed in order to determine whether the stylesheet attempts to access document
	 * elements that are not yet represented in the schema.
	 */
	EXPLORE_SCHEMA,

	/**
	 * The results of the schema exploration phase are being consolidated and the schema is being adjusted.
	 */
	EVOLVE_SCHEMA,

	/**
	 * The XSS vulnerability check is being performed (both initial and follow-up pass).
	 */
	CHECK_XSS,

	/**
	 * The report for the stylesheet is being compiled,
	 */
	COMPILE_REPORT,

	/**
	 * The processing has been completed.
	 */
	DONE
}
