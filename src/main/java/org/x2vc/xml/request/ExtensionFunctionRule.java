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
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Standard implementation of {@link IExtensionFunctionRule}.
 */
public final class ExtensionFunctionRule extends AbstractGenerationRule implements IExtensionFunctionRule {

	@XmlAttribute
	private final UUID functionID;

	@XmlElement(type = RequestedValue.class)
	private final IRequestedValue requestedValue;

	/**
	 * Creates a new {@link ExtensionFunctionRule} with a requested value.
	 *
	 * @param ruleID
	 *
	 * @param functionID
	 * @param requestedValue
	 */
	public ExtensionFunctionRule(UUID ruleID, UUID functionID, IRequestedValue requestedValue) {
		super(ruleID);
		this.functionID = functionID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new {@link ExtensionFunctionRule} without a requested value.
	 *
	 * @param ruleID
	 *
	 * @param functionID
	 */
	public ExtensionFunctionRule(UUID ruleID, UUID functionID) {
		super(ruleID);
		this.functionID = functionID;
		this.requestedValue = null;
	}

	/**
	 * Creates a new {@link ExtensionFunctionRule} with a requested value.
	 *
	 * @param functionID
	 * @param requestedValue
	 */
	public ExtensionFunctionRule(UUID functionID, IRequestedValue requestedValue) {
		super();
		this.functionID = functionID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new {@link ExtensionFunctionRule} without a requested value.
	 *
	 * @param functionID
	 */
	public ExtensionFunctionRule(UUID functionID) {
		super();
		this.functionID = functionID;
		this.requestedValue = null;
	}

	@Override
	public Optional<UUID> getSchemaObjectID() {
		return Optional.of(this.functionID);
	}

	@Override
	public UUID getFunctionID() {
		return this.functionID;
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	@Override
	public IGenerationRule normalize() {
		return new ExtensionFunctionRule(this.functionID, this.requestedValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.functionID, this.requestedValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ExtensionFunctionRule)) {
			return false;
		}
		final ExtensionFunctionRule other = (ExtensionFunctionRule) obj;
		return Objects.equals(this.functionID, other.functionID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
