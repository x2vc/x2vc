package org.x2vc.schema.structure;

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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Base class of {@link ISchemaObject} subtypes.
 */
public abstract class AbstractSchemaObject implements ISchemaObject {

	@XmlAttribute
	private final UUID id;

	@XmlElement
	private final String comment;

	protected AbstractSchemaObject(UUID id, String comment) {
		this.id = id;
		this.comment = comment;
	}

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public Optional<String> getComment() {
		return Optional.ofNullable(this.comment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.comment, this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractSchemaObject)) {
			return false;
		}
		final AbstractSchemaObject other = (AbstractSchemaObject) obj;
		return Objects.equals(this.comment, other.comment) && Objects.equals(this.id, other.id)
				&& this.getClass().equals(other.getClass()); // added manually!
	}

}
