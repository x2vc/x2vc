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

import org.x2vc.schema.structure.IAttribute;

/**
 * Standard implementation of {@link ISetAttributeRule}.
 */
public final class SetAttributeRule extends AbstractGenerationRule implements ISetAttributeRule {

	private final UUID attributeID;

	@XmlElement(type = RequestedValue.class)
	private final IRequestedValue requestedValue;

	/**
	 * Creates a new attribute rule with a specified ID and a requested value specified.
	 *
	 * @param ruleID
	 * @param attributeID
	 * @param requestedValue
	 */
	public SetAttributeRule(UUID ruleID, UUID attributeID, IRequestedValue requestedValue) {
		super(ruleID);
		this.attributeID = attributeID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a random ID and a requested value specified.
	 *
	 * @param attributeID
	 * @param requestedValue
	 */
	public SetAttributeRule(UUID attributeID, IRequestedValue requestedValue) {
		super();
		this.attributeID = attributeID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a specified ID and a requested value specified.
	 *
	 * @param ruleID
	 * @param attribute
	 * @param requestedValue
	 */
	public SetAttributeRule(UUID ruleID, IAttribute attribute, IRequestedValue requestedValue) {
		super(ruleID);
		this.attributeID = attribute.getID();
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a random ID and a requested value specified.
	 *
	 * @param attribute
	 * @param requestedValue
	 */
	public SetAttributeRule(IAttribute attribute, IRequestedValue requestedValue) {
		super();
		this.attributeID = attribute.getID();
		this.requestedValue = requestedValue;
	}

	/**
	 * Creates a new attribute rule with a specified ID and without a requested value specified.
	 *
	 * @param ruleID
	 * @param attributeID
	 */
	public SetAttributeRule(UUID ruleID, UUID attributeID) {
		super(ruleID);
		this.attributeID = attributeID;
		this.requestedValue = null;
	}

	/**
	 * Creates a new attribute rule with a random ID and without a requested value specified.
	 *
	 * @param attributeID
	 */
	public SetAttributeRule(UUID attributeID) {
		super();
		this.attributeID = attributeID;
		this.requestedValue = null;
	}

	/**
	 * Creates a new attribute rule with a specified ID and without a requested value specified.
	 *
	 * @param ruleID
	 * @param attribute
	 */
	public SetAttributeRule(UUID ruleID, IAttribute attribute) {
		super(ruleID);
		this.attributeID = attribute.getID();
		this.requestedValue = null;
	}

	/**
	 * Creates a new attribute rule with a random ID and without a requested value specified.
	 *
	 * @param attribute
	 */
	public SetAttributeRule(IAttribute attribute) {
		super();
		this.attributeID = attribute.getID();
		this.requestedValue = null;
	}

	@XmlAttribute
	@Override
	public UUID getAttributeID() {
		return this.attributeID;
	}

	@Override
	public Optional<UUID> getSchemaObjectID() {
		return Optional.of(this.attributeID);
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	@Override
	public IGenerationRule normalize() {
		if (this.requestedValue == null) {
			return new SetAttributeRule(UUID.fromString("0000-00-00-00-000000"), this.attributeID);
		} else {
			return new SetAttributeRule(UUID.fromString("0000-00-00-00-000000"), this.attributeID,
					this.requestedValue.normalize());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.attributeID, this.requestedValue);
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
		final SetAttributeRule other = (SetAttributeRule) obj;
		return Objects.equals(this.attributeID, other.attributeID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
