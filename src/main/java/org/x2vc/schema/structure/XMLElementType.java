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
package org.x2vc.schema.structure;
import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

/**
 * Standard implementation of {@link IElementType}.
 */
public final class XMLElementType extends XMLDataObject implements IElementType {

	private static final Logger logger = LogManager.getLogger();

	@XmlElement(type = XMLAttribute.class, name = "attribute")
	private final List<IAttribute> attributes;

	@XmlAttribute
	private final ContentType contentType;

	@XmlAttribute
	private final Boolean userModifiable;

	@XmlElement(type = XMLElementReference.class, name = "subElement")
	private final List<IElementReference> elements;

	@XmlAttribute
	private final ElementArrangement elementArrangement;

	private XMLElementType() {
		// Parameterless constructor for deserialization only.
		super(UUID.randomUUID(), null, null, null, null, null, Lists.newArrayList(), null);
		this.attributes = Lists.newArrayList();
		this.contentType = null;
		this.userModifiable = null;
		this.elements = Lists.newArrayList();
		this.elementArrangement = null;
	}

	private XMLElementType(Builder builder) {
		super(builder.id, builder.comment, builder.dataType, builder.maxLength, builder.minValue, builder.maxValue,
				builder.discreteValues.stream()
					.sorted((v1, v2) -> v1.getID().compareTo(v2.getID()))
					.toList(),
				builder.fixedValueset);
		// sort the attributes by name - irrelevant for the actual function, but makes unit testing A LOT easier
		this.attributes = builder.attributes.stream().sorted((a1, a2) -> a1.getName().compareTo(a2.getName())).toList();
		this.contentType = builder.contentType;
		this.userModifiable = builder.userModifiable;
		this.elements = List.copyOf(builder.elements);
		this.elementArrangement = builder.elementArrangement;
		// TODO #37 XML Schema: Validate XMLElementType attribute combinations
	}

	@Override
	public Collection<IAttribute> getAttributes() {
		return this.attributes;
	}

	@Override
	public ContentType getContentType() {
		return this.contentType;
	}

	@Override
	public boolean hasDataContent() {
		return this.contentType == ContentType.DATA;
	}

	@Override
	public boolean hasElementContent() {
		return this.contentType == ContentType.ELEMENT || this.contentType == ContentType.MIXED;
	}

	@Override
	public boolean hasMixedContent() {
		return this.contentType == ContentType.MIXED;
	}

	@Override
	public XMLDataType getDataType() {
		if (this.contentType == ContentType.DATA) {
			return super.getDataType();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"DataType is only supported for data elements (current element %s has content type %s)", getID(),
					this.contentType)));
		}
	}

	@Override
	public Optional<Integer> getMaxLength() {
		if (this.contentType == ContentType.DATA) {
			return super.getMaxLength();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"DataType is only supported for data elements (current element %s has content type %s)", getID(),
					this.contentType)));
		}
	}

	@Override
	public Optional<Integer> getMinValue() {
		if (this.contentType == ContentType.DATA) {
			return super.getMinValue();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"DataType is only supported for data elements (current element %s has content type %s)", getID(),
					this.contentType)));
		}
	}

	@Override
	public Optional<Integer> getMaxValue() {
		if (this.contentType == ContentType.DATA) {
			return super.getMaxValue();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"DataType is only supported for data elements (current element %s has content type %s)", getID(),
					this.contentType)));
		}
	}

	@Override
	public Collection<IDiscreteValue> getDiscreteValues() {
		if (this.contentType == ContentType.DATA) {
			return super.getDiscreteValues();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"Discrete values are only supported for data elements (current element %s has content type %s)",
					getID(), this.contentType)));
		}
	}

	@Override
	public Optional<Boolean> isFixedValueset() {
		if (this.contentType == ContentType.DATA) {
			return super.isFixedValueset();
		} else {
			throw logger.throwing(new IllegalStateException(String.format(
					"Discrete values are only supported for data elements (current element %s has content type %s)",
					getID(), this.contentType)));
		}
	}

	@Override
	public List<IElementReference> getElements() {
		if ((this.contentType == ContentType.ELEMENT) || (this.contentType == ContentType.MIXED)) {
			return this.elements;
		} else {
			throw logger
				.throwing(new IllegalStateException(String.format(
						"Sub-Elements are only supported for element and mixed content (current element %s has content type %s)",
						getID(), this.contentType)));
		}
	}

	@Override
	public ElementArrangement getElementArrangement() {
		if (this.contentType == ContentType.ELEMENT) {
			return this.elementArrangement;
		} else {
			throw logger
				.throwing(new IllegalStateException(String.format(
						"Element arrangement is only supported for element-only elements (current element %s has content type %s)",
						getID(), this.contentType)));
		}
	}

	@Override
	public Optional<Boolean> isUserModifiable() {
		if (this.contentType == ContentType.ELEMENT) {
			throw logger.throwing(
					new IllegalStateException(String.format(
							"User modification tagging is not supported for element-only elements (current element %s has content type %s)",
							getID(), this.contentType)));
		} else {
			return Optional.ofNullable(this.userModifiable);
		}
	}

	/**
	 * Creates a builder to build {@link XMLElementType} and initialize it with the given object.
	 *
	 * @param xMLElementType to initialize the builder with
	 * @param copyAttributes whether to copy all the attributes
	 * @param copyElements   whether to copy all the element references
	 * @return created builder
	 */
	public static Builder builderFrom(IElementType xMLElementType, boolean copyAttributes, boolean copyElements) {
		return new Builder(xMLElementType, copyAttributes, copyElements);
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
	 * Creates a new builder.
	 *
	 * @param id
	 * @return the builder
	 */
	public static Builder builder(UUID id) {
		return new Builder(id);
	}

	/**
	 * Builder to build {@link XMLElementType}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private Set<IAttribute> attributes = new HashSet<>();
		private ContentType contentType;
		private XMLDataType dataType;
		private Integer maxLength;
		private Integer minValue;
		private Integer maxValue;
		private Set<IDiscreteValue> discreteValues = new HashSet<>();
		private Boolean fixedValueset;
		private Boolean userModifiable;
		private List<IElementReference> elements = new ArrayList<>();
		private ElementArrangement elementArrangement;

		/**
		 * Create a new builder.
		 */
		private Builder() {
			this.id = UUID.randomUUID();
		}

		/**
		 * Creates a new builder.
		 *
		 * @param id
		 */
		private Builder(UUID id) {
			this.id = id;
		}

		private Builder(IElementType xMLElementType, boolean copyAttributes, boolean copyElements) {
			this.id = xMLElementType.getID();
			this.comment = xMLElementType.getComment().orElse(null);
			this.contentType = xMLElementType.getContentType();
			if (this.contentType == ContentType.DATA) {
				this.dataType = xMLElementType.getDataType();
				if (this.dataType == XMLDataType.STRING) {
					this.maxLength = xMLElementType.getMaxLength().orElse(null);
				}
				if (this.dataType == XMLDataType.INTEGER) {
					this.minValue = xMLElementType.getMinValue().orElse(null);
					this.maxValue = xMLElementType.getMaxValue().orElse(null);
				}
				this.fixedValueset = xMLElementType.isFixedValueset().orElse(false);
			}
			if ((this.contentType == ContentType.DATA) || (this.contentType == ContentType.MIXED)) {
				this.userModifiable = xMLElementType.isUserModifiable().orElse(null);
			} else if (this.contentType == ContentType.ELEMENT) {
				this.elementArrangement = xMLElementType.getElementArrangement();
			}

			// create deep copy of discrete values
			if (this.contentType == ContentType.DATA) {
				xMLElementType.getDiscreteValues()
					.forEach(val -> this.discreteValues.add(XMLDiscreteValue.builderFrom(val).build()));
			}

			if (copyAttributes) {
				// create deep copy of attributes
				xMLElementType.getAttributes()
					.forEach(attr -> this.attributes.add(XMLAttribute.builderFrom(attr).build()));
			}

			if (copyElements
					&& ((this.contentType == ContentType.ELEMENT) || (this.contentType == ContentType.MIXED))) {
				// create deep copy of elements
				xMLElementType.getElements()
					.forEach(elem -> this.elements.add(XMLElementReference.builderFrom(elem).build()));

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
		 * Builder method for attributes parameter.
		 *
		 * @param attribute field to set
		 * @return builder
		 */
		public Builder addAttribute(IAttribute attribute) {
			this.attributes.add(attribute);
			return this;
		}

		/**
		 * Builder method for contentType parameter.
		 *
		 * @param contentType field to set
		 * @return builder
		 */
		public Builder withContentType(ContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		/**
		 * Builder method for dataType parameter.
		 *
		 * @param dataType field to set
		 * @return builder
		 */
		public Builder withDataType(XMLDataType dataType) {
			this.dataType = dataType;
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
		 * @param discreteValue field to set
		 * @return builder
		 */
		public Builder addDiscreteValue(IDiscreteValue discreteValue) {
			this.discreteValues.add(discreteValue);
			return this;
		}

		/**
		 * Builder method for fixedValueset parameter.
		 *
		 * @param fixedValueset field to set
		 * @return builder
		 */
		public Builder withFixedValueset(Boolean fixedValueset) {
			this.fixedValueset = fixedValueset;
			return this;
		}

		/**
		 * Builder method for userModifiable parameter.
		 *
		 * @param userModifiable field to set
		 * @return builder
		 */
		public Builder withUserModifiable(Boolean userModifiable) {
			this.userModifiable = userModifiable;
			return this;
		}

		/**
		 * Builder method for elements parameter.
		 *
		 * @param element field to set
		 * @return builder
		 */
		public Builder addElement(IElementReference element) {
			this.elements.add(element);
			return this;
		}

		/**
		 * Builder method for elementArrangement parameter.
		 *
		 * @param elementArrangement field to set
		 * @return builder
		 */
		public Builder withElementArrangement(ElementArrangement elementArrangement) {
			this.elementArrangement = elementArrangement;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLElementType build() {
			return new XMLElementType(this);
		}

		/**
		 * Adds the resulting object to an {@link XMLSchema} builder and returns the object for further processing.
		 *
		 * @param schemaBuilder
		 * @return the built element
		 */
		public XMLElementType addTo(XMLSchema.Builder schemaBuilder) {
			final XMLElementType element = build();
			schemaBuilder.addElementType(element);
			return element;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.attributes, this.contentType, this.elementArrangement,
				this.elements, this.userModifiable);
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
		if (!(obj instanceof XMLElementType)) {
			return false;
		}
		final XMLElementType other = (XMLElementType) obj;
		return Objects.equals(this.attributes, other.attributes) && this.contentType == other.contentType
				&& this.elementArrangement == other.elementArrangement && Objects.equals(this.elements, other.elements)
				&& Objects.equals(this.userModifiable, other.userModifiable);
	}

}
