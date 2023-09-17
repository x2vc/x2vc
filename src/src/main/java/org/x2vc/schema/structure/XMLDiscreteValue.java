package org.x2vc.schema.structure;

import java.util.Objects;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard implementation of {@link IXMLDiscreteValue}
 */
public class XMLDiscreteValue extends AbstractSchemaObject implements IXMLDiscreteValue {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private XMLDatatype datatype;

	@XmlAttribute
	private String stringValue;

	@XmlAttribute
	private Boolean booleanValue;

	@XmlAttribute
	private Integer integerValue;

	/**
	 * Parameterless constructor for deserialization only.
	 */
	XMLDiscreteValue() {
	}

	private XMLDiscreteValue(Builder builder) {
		this.id = builder.id;
		this.comment = builder.comment;
		this.stringValue = builder.stringValue;
		this.booleanValue = builder.booleanValue;
		this.integerValue = builder.integerValue;
		if (this.stringValue != null) {
			this.datatype = XMLDatatype.STRING;
		} else if (this.booleanValue != null) {
			this.datatype = XMLDatatype.BOOLEAN;
		} else if (this.integerValue != null) {
			this.datatype = XMLDatatype.INTEGER;
		} else {
			this.datatype = XMLDatatype.OTHER;
		}
	}

	@Override
	public boolean isValue() {
		return true;
	}

	@Override
	public IXMLDiscreteValue asValue() {
		return this;
	}

	@Override
	public XMLDatatype getDatatype() {
		return this.datatype;
	}

	@Override
	public String asString() {
		if (this.datatype == XMLDatatype.STRING) {
			return this.stringValue;
		} else {
			throw logger.throwing(new IllegalStateException("Attempt to retrieve non-string value as string"));
		}
	}

	@Override
	public Boolean asBoolean() {
		if (this.datatype == XMLDatatype.BOOLEAN) {
			return this.booleanValue;
		} else {
			throw logger.throwing(new IllegalStateException("attempt to retrieve non-boolean value as boolean"));
		}
	}

	@Override
	public Integer asInteger() {
		if (this.datatype == XMLDatatype.INTEGER) {
			return this.integerValue;
		} else {
			throw logger.throwing(new IllegalStateException("attempt to retrieve non-integer value as integer"));
		}
	}

	/**
	 * Creates a builder to build {@link XMLDiscreteValue} and initialize it with
	 * the given object.
	 *
	 * @param xMLDiscreteValue to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IXMLDiscreteValue xMLDiscreteValue) {
		return new Builder(xMLDiscreteValue);
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
		public Builder() {
			this.id = UUID.randomUUID();
		}

		/**
		 * Create a new builder.
		 *
		 * @param id
		 */
		public Builder(UUID id) {
			this.id = id;
		}

		private Builder(IXMLDiscreteValue xMLDiscreteValue) {
			this.id = xMLDiscreteValue.getID();
			this.comment = xMLDiscreteValue.getComment().orElse(null);
			if (xMLDiscreteValue.getDatatype() == XMLDatatype.STRING) {
				this.stringValue = xMLDiscreteValue.asString();
			} else if (xMLDiscreteValue.getDatatype() == XMLDatatype.BOOLEAN) {
				this.booleanValue = xMLDiscreteValue.asBoolean();
			} else if (xMLDiscreteValue.getDatatype() == XMLDatatype.INTEGER) {
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
		 * Adds the resulting object to an {@link XMLElementType} builder and returns
		 * the object for further processing.
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
		result = prime * result + Objects.hash(this.booleanValue, this.integerValue, this.stringValue, this.datatype);
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
				&& Objects.equals(this.stringValue, other.stringValue) && this.datatype == other.datatype;
	}

}
