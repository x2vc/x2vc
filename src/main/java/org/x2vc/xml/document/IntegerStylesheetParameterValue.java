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

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IStylesheetParameterValue} that contains an integer value.
 */
public final class IntegerStylesheetParameterValue extends AbstractStylesheetParameterValue {

	@XmlElement
	private final Integer result;

	/**
	 * Creates a new parameter value.
	 *
	 * @param parameterID
	 * @param result
	 */
	public IntegerStylesheetParameterValue(UUID parameterID, Integer result) {
		super(parameterID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
