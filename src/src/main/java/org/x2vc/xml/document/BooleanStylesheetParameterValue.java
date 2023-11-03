package org.x2vc.xml.document;

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

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import net.sf.saxon.s9api.XdmValue;

/**
 * Implementation of {@link IStylesheetParameterValue} that contains a boolean value.
 */
public final class BooleanStylesheetParameterValue extends AbstractStylesheetParameterValue {

	@XmlElement
	private final Boolean result;

	/**
	 * Creates a new parameter value.
	 *
	 * @param parameterID
	 * @param result
	 */
	public BooleanStylesheetParameterValue(UUID parameterID, Boolean result) {
		super(parameterID);
		this.result = result;
	}

	@Override
	public XdmValue getXDMValue() {
		return XdmValue.makeValue(this.result);
	}

}
