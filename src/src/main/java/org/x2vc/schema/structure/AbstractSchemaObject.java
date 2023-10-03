package org.x2vc.schema.structure;

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
