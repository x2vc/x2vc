package org.x2vc.process.tasks;

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
