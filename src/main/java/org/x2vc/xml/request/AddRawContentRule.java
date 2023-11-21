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
 * Standard implementation of {@link IAddRawContentRule}.
 */
public final class AddRawContentRule extends AbstractGenerationRule implements IAddRawContentRule {

	@XmlAttribute
	private final UUID elementID;

	@XmlElement(type = RequestedValue.class)
	private final IRequestedValue requestedValue;

	/**
	 * Create a new rule with a specified ID and a requested value specified.
	 *
	 * @param ruleID
	 * @param elementID
	 * @param requestedValue
	 */
	public AddRawContentRule(UUID ruleID, UUID elementID, IRequestedValue requestedValue) {
		super(ruleID);
		this.elementID = elementID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule with a random ID and a requested value specified.
	 *
	 * @param elementID
	 * @param requestedValue
	 */
	public AddRawContentRule(UUID elementID, IRequestedValue requestedValue) {
		super();
		this.elementID = elementID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule with a specified ID and without a requested value specified.
	 *
	 * @param ruleID
	 * @param elementID
	 */
	public AddRawContentRule(UUID ruleID, UUID elementID) {
		super(ruleID);
		this.elementID = elementID;
		this.requestedValue = null;
	}

	/**
	 * Create a new rule with a random ID and without a requested value specified.
	 *
	 * @param elementID
	 */
	public AddRawContentRule(UUID elementID) {
		super();
		this.elementID = elementID;
		this.requestedValue = null;
	}

	@Override
	public UUID getElementID() {
		return this.elementID;
	}

	@Override
	public Optional<UUID> getSchemaObjectID() {
		return Optional.of(this.elementID);
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	@Override
	public IGenerationRule normalize() {
		if (this.requestedValue == null) {
			return new AddRawContentRule(UUID.fromString("0000-00-00-00-000000"), this.elementID);
		} else {
			return new AddRawContentRule(UUID.fromString("0000-00-00-00-000000"), this.elementID,
					this.requestedValue.normalize());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.elementID, this.requestedValue);
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AddRawContentRule other = (AddRawContentRule) obj;
		return Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
