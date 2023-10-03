package org.x2vc.schema.structure;

import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.collect.Lists;

/**
 * Standard implementation of {@link IXMLAttribute}.
 */
public final class XMLAttribute extends XMLDataObject implements IXMLAttribute {

	@XmlAttribute
	private final String name;

	@XmlAttribute
	private final Boolean optional;

	@XmlAttribute
	private final Boolean userModifiable;

	private XMLAttribute(Builder builder) {
		super(builder.id, builder.comment, builder.dataType, builder.maxLength, builder.minValue, builder.maxValue,
				builder.discreteValues.stream()
					.sorted((v1, v2) -> v1.getID().compareTo(v2.getID()))
					.toList(),
				builder.fixedValueset);
		this.name = builder.name;
		this.optional = builder.optional;
		this.userModifiable = builder.userModifiable;
		// TODO XML Schema: Validate XMLAttribute attribute combinations
	}

	private XMLAttribute() {
		// Parameterless constructor for deserialization only.
		super(UUID.randomUUID(), null, null, null, null, null, Lists.newArrayList(), null);
		this.name = null;
		this.optional = null;
		this.userModifiable = null;
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
	 * Creates a builder to build {@link XMLAttribute} and initialize it with the given object.
	 *
	 * @param xmlAttribute to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IXMLAttribute xmlAttribute) {
		return new Builder(xmlAttribute);
	}

	/**
	 * Provides a builder to create a {@link XMLAttribute} instance.
	 *
	 * @param name
	 * @return a new builder
	 */
	public static Builder builder(String name) {
		return new Builder(name);
	}

	/**
	 * Provides a builder to create a {@link XMLAttribute} instance.
	 *
	 * @param id
	 * @param name
	 * @return a new builder
	 */
	public static Builder builder(UUID id, String name) {
		return new Builder(id, name);
	}

	/**
	 * Builder to build {@link XMLAttribute}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String name;
		private XMLDataType dataType;
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
		private Builder(String name) {
			this.id = UUID.randomUUID();
			this.name = name;
		}

		/**
		 * Creates a new builder.
		 *
		 * @param id
		 * @param name
		 */
		private Builder(UUID id, String name) {
			this.id = id;
			this.name = name;
		}

		private Builder(IXMLAttribute xMLAttribute) {
			this.id = xMLAttribute.getID();
			this.comment = xMLAttribute.getComment().orElse(null);
			this.name = xMLAttribute.getName();
			this.dataType = xMLAttribute.getDataType();
			this.optional = xMLAttribute.isOptional();
			if (this.dataType == XMLDataType.STRING) {
				this.maxLength = xMLAttribute.getMaxLength().orElse(null);
			}
			if (this.dataType == XMLDataType.INTEGER) {
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
		 * Builder method for type parameter.
		 *
		 * @param type field to set
		 * @return builder
		 */
		public Builder withType(XMLDataType type) {
			this.dataType = type;
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
		 * Adds the resulting object to an {@link XMLElementType} builder and returns the object for further processing.
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
		if (!(obj instanceof XMLAttribute)) {
			return false;
		}
		final XMLAttribute other = (XMLAttribute) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.optional, other.optional)
				&& Objects.equals(this.userModifiable, other.userModifiable);
	}

}
