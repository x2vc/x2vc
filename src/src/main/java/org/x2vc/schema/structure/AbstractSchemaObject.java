package org.x2vc.schema.structure;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class of {@link IXMLSchemaObject} subtypes.
 */
public abstract class AbstractSchemaObject implements IXMLSchemaObject {

	private static final Logger logger = LogManager.getLogger();

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
	public boolean isAttribute() {
		return false;
	}

	@Override
	public IXMLAttribute asAttribute() {
		throw logger.throwing(new IllegalStateException("This schema object can not be cast to IXMLAttribute"));
	}

	@Override
	public boolean isElement() {
		return false;
	}

	@Override
	public IXMLElementType asElement() {
		throw logger.throwing(new IllegalStateException("This schema object can not be cast to IXMLElementType"));
	}

	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public IXMLElementReference asReference() {
		throw logger.throwing(new IllegalStateException("This schema object can not be cast to IXMLElementReference"));
	}

	@Override
	public boolean isValue() {
		return false;
	}

	@Override
	public IXMLDiscreteValue asValue() {
		throw logger.throwing(new IllegalStateException("This schema object can not be cast to IXMLDiscreteValue"));
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
