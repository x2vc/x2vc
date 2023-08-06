package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class of {@link IXMLSchemaObject} subtypes.
 */
public abstract class AbstractSchemaObject implements IXMLSchemaObject {

	private static final long serialVersionUID = 2406827816150443227L;
	private static final Logger logger = LogManager.getLogger();
	protected UUID id;
	protected String comment;

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

}
