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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base implementation of {@link IExtensionFunctionResult}.
 */
public abstract class AbstractExtensionFunctionResult implements IExtensionFunctionResult {

	@XmlAttribute
	private final UUID functionID;

	protected AbstractExtensionFunctionResult(UUID functionID) {
		super();
		this.functionID = functionID;
	}

	@Override
	public UUID getFunctionID() {
		return this.functionID;
	}

}
