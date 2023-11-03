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
 * A discrete value used to represent either "interesting" values or hard
 * restrictions.
 */
public interface IDiscreteValue extends ISchemaObject {

	/**
	 * @return the data type of the value, represented as {@link XMLDataType}.
	 */
	XMLDataType getDataType();

	/**
	 * @return the value represented as String, if supported
	 */
	String asString();

	/**
	 * @return the value represented as Boolean, if supported
	 */
	Boolean asBoolean();

	/**
	 * @return the value represented as Integer, if supported
	 */
	Integer asInteger();

}
