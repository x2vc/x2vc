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
package org.x2vc.xml.value;


import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Standard implementation of {@link IValueDescriptor}.
 */
public final class ValueDescriptor implements IValueDescriptor {

	@XmlAttribute
	private final UUID schemaObjectID;

	@XmlAttribute
	private final UUID generationRuleID;

	@XmlElement
	private final String value;

	@XmlAttribute
	private final boolean requested;

	/**
	 * @param schemaObjectID
	 * @param generationRuleID
	 * @param value
	 * @param requested
	 */
	public ValueDescriptor(UUID schemaObjectID, UUID generationRuleID, String value, boolean requested) {
		super();
		this.schemaObjectID = schemaObjectID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.requested = requested;
	}

	/**
	 * @param schemaObjectID
	 * @param generationRuleID
	 * @param value
	 */
	public ValueDescriptor(UUID schemaObjectID, UUID generationRuleID, String value) {
		super();
		this.schemaObjectID = schemaObjectID;
		this.generationRuleID = generationRuleID;
		this.value = value;
		this.requested = false;
	}

	@Override
	public UUID getSchemaObjectID() {
		return this.schemaObjectID;
	}

	@Override
	public UUID getGenerationRuleID() {
		return this.generationRuleID;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public boolean isRequested() {
		return this.requested;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.generationRuleID, this.requested, this.schemaObjectID, this.value);
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
		final ValueDescriptor other = (ValueDescriptor) obj;
		return Objects.equals(this.generationRuleID, other.generationRuleID) && this.requested == other.requested
				&& Objects.equals(this.schemaObjectID, other.schemaObjectID)
				&& Objects.equals(this.value, other.value);
	}

}
