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
package org.x2vc.xml.request;


import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base class for {@link IGenerationRule} derivations.
 */
public abstract class AbstractGenerationRule implements IGenerationRule {

	@XmlAttribute
	private final UUID ruleID;

	/**
	 * Creates a new rule with a random ID.
	 */
	AbstractGenerationRule() {
		super();
		this.ruleID = UUID.randomUUID();
	}

	/**
	 * Creates a new rule with a specific ID.
	 *
	 * @param ruleID
	 */
	AbstractGenerationRule(UUID ruleID) {
		super();
		this.ruleID = ruleID;
	}

	@Override
	public UUID getID() {
		return this.ruleID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.ruleID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractGenerationRule other = (AbstractGenerationRule) obj;
		return Objects.equals(this.ruleID, other.ruleID);
	}

}
