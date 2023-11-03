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
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard implementation of {@link IDiscreteValue}
 */
public final class XMLDiscreteValue extends AbstractSchemaObject implements IDiscreteValue {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private final XMLDataType dataType;

	@XmlAttribute
	private final String stringValue;

	@XmlAttribute
	private final Boolean booleanValue;

	@XmlAttribute
	private final Integer integerValue;

	private XMLDiscreteValue(Builder builder) {
		super(builder.id, builder.comment);
		this.stringValue = builder.stringValue;
		this.booleanValue = builder.booleanValue;
		this.integerValue = builder.integerValue;
		if (this.stringValue != null) {
			this.dataType = XMLDataType.STRING;
		} else if (this.booleanValue != null) {
			this.dataType = XMLDataType.BOOLEAN;
		} else if (this.integerValue != null) {
			this.dataType = XMLDataType.INTEGER;
		} else {
			this.dataType = XMLDataType.OTHER;
		}
	}

	private XMLDiscreteValue() {
		super(UUID.randomUUID(), null);
		this.stringValue = null;
		this.booleanValue = null;
		this.integerValue = null;
		this.dataType = null;
	}

	@Override
	public XMLDataType getDataType() {
		return this.dataType;
	}

	@Override
	public String asString() {
		if (this.dataType == XMLDataType.STRING) {
			return this.stringValue;
		} else {
			throw logger.throwing(new IllegalStateException("Attempt to retrieve non-string value as string"));
		}
	}

	@Override
	public Boolean asBoolean() {
		if (this.dataType == XMLDataType.BOOLEAN) {
			return this.booleanValue;
		} else {
			throw logger.throwing(new IllegalStateException("attempt to retrieve non-boolean value as boolean"));
		}
	}

	@Override
	public Integer asInteger() {
		if (this.dataType == XMLDataType.INTEGER) {
			return this.integerValue;
		} else {
			throw logger.throwing(new IllegalStateException("attempt to retrieve non-integer value as integer"));
		}
	}

	/**
	 * Creates a builder to build {@link XMLDiscreteValue} and initialize it with the given object.
	 *
	 * @param xMLDiscreteValue to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IDiscreteValue xMLDiscreteValue) {
		return new Builder(xMLDiscreteValue);
	}

	/**
	 * Create a new builder.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new builder.
	 *
	 * @param id
	 * @return the builder
	 */
	public static Builder builder(UUID id) {
		return new Builder(id);
	}

	/**
	 * Builder to build {@link XMLDiscreteValue}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String stringValue;
		private Boolean booleanValue;
		private Integer integerValue;

		/**
		 * Create a new builder.
		 */
		private Builder() {
			this.id = UUID.randomUUID();
		}

		/**
		 * Create a new builder.
		 *
		 * @param id
		 */
		private Builder(UUID id) {
			this.id = id;
		}

		private Builder(IDiscreteValue xMLDiscreteValue) {
			this.id = xMLDiscreteValue.getID();
			this.comment = xMLDiscreteValue.getComment().orElse(null);
			if (xMLDiscreteValue.getDataType() == XMLDataType.STRING) {
				this.stringValue = xMLDiscreteValue.asString();
			} else if (xMLDiscreteValue.getDataType() == XMLDataType.BOOLEAN) {
				this.booleanValue = xMLDiscreteValue.asBoolean();
			} else if (xMLDiscreteValue.getDataType() == XMLDataType.INTEGER) {
				this.integerValue = xMLDiscreteValue.asInteger();
			}
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
		 * Builder method for stringValue parameter.
		 *
		 * @param stringValue field to set
		 * @return builder
		 */
		public Builder withStringValue(String stringValue) {
			this.stringValue = stringValue;
			this.booleanValue = null;
			this.integerValue = null;
			return this;
		}

		/**
		 * Builder method for booleanValue parameter.
		 *
		 * @param booleanValue field to set
		 * @return builder
		 */
		public Builder withBooleanValue(Boolean booleanValue) {
			this.booleanValue = booleanValue;
			this.stringValue = null;
			this.integerValue = null;
			return this;
		}

		/**
		 * Builder method for integerValue parameter.
		 *
		 * @param integerValue field to set
		 * @return builder
		 */
		public Builder withIntegerValue(Integer integerValue) {
			this.integerValue = integerValue;
			this.booleanValue = null;
			this.stringValue = null;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLDiscreteValue build() {
			return new XMLDiscreteValue(this);
		}

		/**
		 * Adds the resulting object to an {@link XMLAttribute} builder.
		 *
		 * @param attributeBuilder
		 */
		public void addTo(XMLAttribute.Builder attributeBuilder) {
			attributeBuilder.addDiscreteValue(build());
		}

		/**
		 * Adds the resulting object to an {@link XMLElementType} builder and returns the object for further processing.
		 *
		 * @param elementBuilder
		 * @return the built value
		 */
		public XMLDiscreteValue addTo(XMLElementType.Builder elementBuilder) {
			final XMLDiscreteValue value = build();
			elementBuilder.addDiscreteValue(value);
			return value;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.booleanValue, this.integerValue, this.stringValue, this.dataType);
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
		final XMLDiscreteValue other = (XMLDiscreteValue) obj;
		return Objects.equals(this.booleanValue, other.booleanValue)
				&& Objects.equals(this.integerValue, other.integerValue)
				&& Objects.equals(this.stringValue, other.stringValue) && this.dataType == other.dataType;
	}

}
