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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base implementation of {@link IStylesheetParameterValue}.
 */
public abstract class AbstractStylesheetParameterValue implements IStylesheetParameterValue {

	@XmlAttribute
	private final UUID parameterID;

	protected AbstractStylesheetParameterValue(UUID parameterID) {
		super();
		this.parameterID = parameterID;
	}

	@Override
	public UUID getParameterID() {
		return this.parameterID;
	}

}
