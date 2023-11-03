package org.x2vc.schema.structure;

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
 * The base data types supported by the XML schema.
 */
public enum XMLDataType {

	/**
	 * A character string value. Additional specifications: maxLength.
	 */
	STRING,

	/**
	 * A boolean value (<code>true</code> or <code>false</code>).
	 */
	BOOLEAN,

	/**
	 * An integer value, optionally signed. Additional specifications: minValue, maxValue).
	 */
	INTEGER,

	// TODO XML Schema: Support other numeric data types.
	// TODO XML Schema: Support date/time/duration data types.
	// TODO XML Schema: Support binary data types.

	/**
	 * Another type not explicitly supported.
	 */
	OTHER

}
