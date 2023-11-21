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
package org.x2vc.xml.document;


import java.util.UUID;

import org.x2vc.schema.structure.IExtensionFunction;

import net.sf.saxon.s9api.XdmValue;

/**
 * A representation of a call to an extension function (represented by the function ID as provided by the schema) and
 * the generated return value.
 *
 * In a subsequent version, matchers to restrict the return value to various input parameters might be added here.
 */
public interface IExtensionFunctionResult {

	/**
	 * @return the function ID as provided by the schema
	 * @see IExtensionFunction#getID()
	 */
	UUID getFunctionID();

	/**
	 * @return the result of the function call as {@link XdmValue}
	 */
	XdmValue getXDMValue();

}
