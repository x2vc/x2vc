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
package org.x2vc.schema.structure;


/**
 * Attribute of an XML Element.
 */
public interface IAttribute extends IDataObject {

	/**
	 * @return the name of the attribute
	 */
	String getName();

	/**
	 * @return <code>true</code> if the attribute is optional
	 */
	boolean isOptional();

	/**
	 * @return <code>true</code> if the value of the attribute can be influenced by
	 *         user input.
	 */
	boolean isUserModifiable();

}
