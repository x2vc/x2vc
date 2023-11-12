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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard 1mplementation of {@link IElementReference}.
 */
public final class XMLElementReference extends AbstractSchemaObject implements IElementReference {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private final String name;

	private transient IElementType element;

	@XmlAttribute
	private final UUID elementID;

	@XmlAttribute
	private final Integer minOccurrence;

	@XmlAttribute
	private final Integer maxOccurrence;

	private XMLElementReference(Builder builder) {
		super(builder.id, builder.comment);
		this.name = builder.name;
		this.element = builder.element;
		this.elementID = builder.elementID;
		this.minOccurrence = builder.minOccurrence;
		this.maxOccurrence = builder.maxOccurrence;
	}

	private XMLElementReference() {
		super(UUID.randomUUID(), null);
		this.name = null;
		this.element = null;
		this.elementID = null;
		this.minOccurrence = null;
		this.maxOccurrence = null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IElementType getElement() {
		if (this.element == null) {
			throw logger.throwing(new IllegalStateException(
					"Attempt to follow element reference that has not been completed after copy operation."));
		}
		return this.element;
	}

	@Override
	public UUID getElementID() {
		return this.elementID;
	}

	void fixElementReference(IElementType element) {
		if (this.element != null) {
			throw logger.throwing(new IllegalStateException(
					"Attempt to fix element reference that has already been completed after copy operation."));
		}
		if (!element.getID().equals(this.elementID)) {
			throw logger.throwing(new IllegalArgumentException(
					String.format("Attempt to fix element reference with ID %s with different element with ID %s.",
							this.elementID, element.getID())));
		}
		this.element = element;
	}

	@Override
	public Integer getMinOccurrence() {
		return (this.minOccurrence == null) ? 0 : this.minOccurrence;
	}

	@Override
	public Optional<Integer> getMaxOccurrence() {
		return Optional.ofNullable(this.maxOccurrence);
	}

	/**
	 * Creates a builder to build {@link XMLElementReference} and initialize it with the given object.
	 *
	 * @param xMLElementReference to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IElementReference xMLElementReference) {
		return new Builder(xMLElementReference);
	}

	/**
	 * Create a new builder instance.
	 *
	 * @param name
	 * @param element
	 * @return the builder
	 */
	public static Builder builder(String name, IElementType element) {
		return new Builder(name, element);
	}

	/**
	 * Create a new builder instance.
	 *
	 * @param id
	 * @param name
	 * @param element
	 * @return the builder
	 */
	public static Builder builder(UUID id, String name, IElementType element) {
		return new Builder(id, name, element);
	}

	/**
	 * Builder to build {@link XMLElementReference}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String name;
		private IElementType element;
		private UUID elementID;
		private Integer minOccurrence = 0;
		private Integer maxOccurrence = null;

		/**
		 * Create a new builder instance.
		 *
		 * @param name
		 * @param element
		 */
		private Builder(String name, IElementType element) {
			this.id = UUID.randomUUID();
			this.name = name;
			this.element = element;
			this.elementID = element.getID();
		}

		/**
		 * Create a new builder instance.
		 *
		 * @param id
		 * @param name
		 * @param element
		 */
		private Builder(UUID id, String name, IElementType element) {
			this.id = id;
			this.name = name;
			this.element = element;
			this.elementID = element.getID();
		}

		private Builder(IElementReference xMLElementReference) {
			this.id = xMLElementReference.getID();
			this.comment = xMLElementReference.getComment().orElse(null);
			this.elementID = xMLElementReference.getElement().getID();
			this.minOccurrence = xMLElementReference.getMinOccurrence();
			this.maxOccurrence = xMLElementReference.getMaxOccurrence().orElse(null);

			// element reference is broken by copy operation and needs to be reconstructed
			// after the entire schema has been copied
			this.name = xMLElementReference.getName();
		}

		/**
		 * Builder method for comment parameter.
		 *
		 * @param comment field to set
		 * @return builder
		 */
		public Builder withComment(String comment) {
			this.comment = comment;
			return this;
		}

		/**
		 * Builder method for comment parameter.
		 *
		 * @param comment field to set
		 * @return builder
		 */
		public Builder withComment(Optional<String> comment) {
			this.comment = comment.orElse(null);
			return this;
		}

		/**
		 * Builder method for minOccurrence parameter.
		 *
		 * @param minOccurrence field to set
		 * @return builder
		 */
		public Builder withMinOccurrence(Integer minOccurrence) {
			this.minOccurrence = minOccurrence;
			return this;
		}

		/**
		 * Builder method for maxOccurrence parameter.
		 *
		 * @param maxOccurrence field to set
		 * @return builder
		 */
		public Builder withMaxOccurrence(Integer maxOccurrence) {
			this.maxOccurrence = maxOccurrence;
			return this;
		}

		/**
		 * Builder method for maxOccurrence parameter.
		 *
		 * @param maxOccurrence field to set
		 * @return builder
		 */
		public Builder withMaxOccurrence(Optional<Integer> maxOccurrence) {
			this.maxOccurrence = maxOccurrence.orElse(null);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLElementReference build() {
			return new XMLElementReference(this);
		}

		/**
		 * Adds the resulting object to an {@link XMLElementType} builder and returns the object for further processing.
		 *
		 * @param elementBuilder
		 * @return the built reference
		 */
		public XMLElementReference addTo(XMLElementType.Builder elementBuilder) {
			final XMLElementReference ref = build();
			elementBuilder.addElement(ref);
			return ref;
		}

		/**
		 * Adds the resulting object as a root element reference to an {@link XMLSchema} builder and returns the object
		 * for further processing.
		 *
		 * @param schemaBuilder
		 * @return the built element
		 */
		public XMLElementReference addTo(XMLSchema.Builder schemaBuilder) {
			final XMLElementReference ref = build();
			schemaBuilder.addRootElement(ref);
			return ref;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.elementID, this.maxOccurrence, this.minOccurrence, this.name);
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
		if (!(obj instanceof XMLElementReference)) {
			return false;
		}
		final XMLElementReference other = (XMLElementReference) obj;
		return Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.maxOccurrence, other.maxOccurrence)
				&& Objects.equals(this.minOccurrence, other.minOccurrence) && Objects.equals(this.name, other.name);
	}

}
