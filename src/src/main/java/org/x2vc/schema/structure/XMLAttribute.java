package org.x2vc.schema.structure;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link IXMLAttribute}.
 */
public class XMLAttribute extends XMLDataObject implements IXMLAttribute {

	@XmlAttribute
	private String name;

	@XmlAttribute
	private Boolean optional;

	@XmlAttribute
	private Boolean userModifiable;

	/**
	 * Parameterless constructor for deserialization only.
	 */
	XMLAttribute() {
		this.discreteValues = Sets.newHashSet();
	}

	private XMLAttribute(Builder builder) {
		this.id = builder.id;
		this.comment = builder.comment;
		this.name = builder.name;
		this.datatype = builder.datatype;
		this.optional = builder.optional;
		this.maxLength = builder.maxLength;
		this.minValue = builder.minValue;
		this.maxValue = builder.maxValue;
		this.discreteValues = Set.copyOf(builder.discreteValues);
		this.fixedValueset = builder.fixedValueset;
		this.userModifiable = builder.userModifiable;
		// TODO XML Schema: Validate XMLAttribute attribute combinations
	}

	@Override
	public boolean isAttribute() {
		return true;
	}

	@Override
	public IXMLAttribute asAttribute() {
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}

	@Override
	public boolean isUserModifiable() {
		return this.userModifiable;
	}

	/**
	 * Creates a builder to build {@link XMLAttribute} and initialize it with the
	 * given object.
	 *
	 * @param xMLAttribute to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IXMLAttribute xMLAttribute) {
		return new Builder(xMLAttribute);
	}

	/**
	 * Builder to build {@link XMLAttribute}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String name;
		private XMLDatatype datatype;
		private Boolean optional = false;
		private Integer maxLength;
		private Integer minValue;
		private Integer maxValue;
		private Set<IXMLDiscreteValue> discreteValues = new HashSet<>();
		private boolean fixedValueset = false;
		private boolean userModifiable;

		/**
		 * Create a new builder.
		 *
		 * @param name
		 */
		public Builder(String name) {
			this.id = UUID.randomUUID();
			this.name = name;
		}

		/**
		 * Creates a new builder.
		 *
		 * @param id
		 * @param name
		 */
		public Builder(UUID id, String name) {
			this.id = id;
			this.name = name;
		}

		private Builder(IXMLAttribute xMLAttribute) {
			this.id = xMLAttribute.getID();
			this.comment = xMLAttribute.getComment().orElse(null);
			this.name = xMLAttribute.getName();
			this.datatype = xMLAttribute.getDatatype();
			this.optional = xMLAttribute.isOptional();
			if (this.datatype == XMLDatatype.STRING) {
				this.maxLength = xMLAttribute.getMaxLength().orElse(null);
			}
			if (this.datatype == XMLDatatype.INTEGER) {
				this.minValue = xMLAttribute.getMinValue().orElse(null);
				this.maxValue = xMLAttribute.getMaxValue().orElse(null);
			}
			this.fixedValueset = xMLAttribute.isFixedValueset().orElse(false);
			this.userModifiable = xMLAttribute.isUserModifiable();

			// create deep copy of discrete values
			xMLAttribute.getDiscreteValues()
				.forEach(val -> this.discreteValues.add(XMLDiscreteValue.builderFrom(val).build()));
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
		 * Builder method for type parameter.
		 *
		 * @param type field to set
		 * @return builder
		 */
		public Builder withType(XMLDatatype type) {
			this.datatype = type;
			return this;
		}

		/**
		 * Builder method for optional parameter.
		 *
		 * @param optional field to set
		 * @return builder
		 */
		public Builder withOptional(Boolean optional) {
			this.optional = optional;
			return this;
		}

		/**
		 * Builder method for maxLength parameter.
		 *
		 * @param maxLength field to set
		 * @return builder
		 */
		public Builder withMaxLength(Integer maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		/**
		 * Builder method for minValue parameter.
		 *
		 * @param minValue field to set
		 * @return builder
		 */
		public Builder withMinValue(Integer minValue) {
			this.minValue = minValue;
			return this;
		}

		/**
		 * Builder method for maxValue parameter.
		 *
		 * @param maxValue field to set
		 * @return builder
		 */
		public Builder withMaxValue(Integer maxValue) {
			this.maxValue = maxValue;
			return this;
		}

		/**
		 * Builder method for discreteValues parameter.
		 *
		 * @param discreteValue value to add
		 * @return builder
		 */
		public Builder addDiscreteValue(IXMLDiscreteValue discreteValue) {
			this.discreteValues.add(discreteValue);
			return this;
		}

		/**
		 * Builder method for fixedValueset parameter.
		 *
		 * @param fixedValueset field to set
		 * @return builder
		 */
		public Builder withFixedValueset(boolean fixedValueset) {
			this.fixedValueset = fixedValueset;
			return this;
		}

		/**
		 * Builder method for userModifiable parameter.
		 *
		 * @param userModifiable field to set
		 * @return builder
		 */
		public Builder withUserModifiable(boolean userModifiable) {
			this.userModifiable = userModifiable;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLAttribute build() {
			return new XMLAttribute(this);
		}

		/**
		 * Adds the resulting object to an {@link XMLElementType} builder and returns
		 * the object for further processing.
		 *
		 * @param elementBuilder
		 * @return the built attribute
		 */
		public XMLAttribute addTo(XMLElementType.Builder elementBuilder) {
			final XMLAttribute attribute = build();
			elementBuilder.addAttribute(attribute);
			return attribute;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.name, this.optional, this.userModifiable);
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
		final XMLAttribute other = (XMLAttribute) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.optional, other.optional)
				&& Objects.equals(this.userModifiable, other.userModifiable);
	}

}
