package org.x2vc.schema.structure;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link IXMLElementType}.
 */
public class XMLElementType extends AbstractSchemaObject implements IXMLElementType {

	private static final long serialVersionUID = -1422448896072065357L;
	private static final Logger logger = LogManager.getLogger();

	private ImmutableSet<IXMLAttribute> attributes;
	private ContentType contentType;
	private XMLDatatype datatype;
	private Integer maxLength;
	private Integer minValue;
	private Integer maxValue;
	private ImmutableSet<IXMLDiscreteValue> discreteValues;
	private Boolean fixedValueset;
	private Boolean userModifiable;
	private ImmutableList<IXMLElementReference> elements;
	private ElementArrangement elementArrangement;

	private XMLElementType(Builder builder) {
		this.id = builder.id;
		this.comment = builder.comment;
		this.attributes = ImmutableSet.copyOf(builder.attributes);
		this.contentType = builder.contentType;
		this.datatype = builder.datatype;
		this.maxLength = builder.maxLength;
		this.minValue = builder.minValue;
		this.maxValue = builder.maxValue;
		this.discreteValues = ImmutableSet.copyOf(builder.discreteValues);
		this.fixedValueset = builder.fixedValueset;
		this.userModifiable = builder.userModifiable;
		this.elements = ImmutableList.copyOf(builder.elements);
		this.elementArrangement = builder.elementArrangement;
		// TODO XML Schema: Validate XMLElementType attribute combinations
	}

	@Override
	public boolean isElement() {
		return true;
	}

	@Override
	public IXMLElementType asElement() {
		return this;
	}

	@Override
	public ImmutableSet<IXMLAttribute> getAttributes() {
		return this.attributes;
	}

	@Override
	public ContentType getContentType() {
		return this.contentType;
	}

	@Override
	public boolean hasTextContent() {
		return this.contentType == ContentType.TEXT;
	}

	@Override
	public boolean hasDataContent() {
		return this.contentType == ContentType.DATA;
	}

	@Override
	public boolean hasElementContent() {
		return this.contentType == ContentType.ELEMENT;
	}

	@Override
	public boolean hasMixedContent() {
		return this.contentType == ContentType.MIXED;
	}

	@Override
	public XMLDatatype getDatatype() {
		if (this.contentType == ContentType.DATA) {
			return this.datatype;
		} else {
			throw logger.throwing(new IllegalStateException("Datatype is only supported for data elements"));
		}
	}

	@Override
	public Optional<Integer> getMaxLength() {
		if (this.contentType == ContentType.DATA) {
			if (this.datatype == XMLDatatype.STRING) {
				return Optional.ofNullable(this.maxLength);
			} else {
				throw (logger
					.throwing(new IllegalArgumentException("A maximum length is only supported for type STRING")));
			}
		} else {
			throw logger.throwing(new IllegalStateException("Datatype is only supported for data elements"));
		}
	}

	@Override
	public Optional<Integer> getMinValue() {
		if (this.contentType == ContentType.DATA) {
			if (this.datatype == XMLDatatype.INTEGER) {
				return Optional.ofNullable(this.minValue);
			} else {
				throw (logger
					.throwing(new IllegalArgumentException("A maximum length is only supported for type STRING")));
			}
		} else {
			throw logger.throwing(new IllegalStateException("Datatype is only supported for data elements"));
		}
	}

	@Override
	public Optional<Integer> getMaxValue() {
		if (this.contentType == ContentType.DATA) {
			if (this.datatype == XMLDatatype.INTEGER) {
				return Optional.ofNullable(this.maxValue);
			} else {
				throw (logger
					.throwing(new IllegalArgumentException("A maximum length is only supported for type STRING")));
			}
		} else {
			throw logger.throwing(new IllegalStateException("Datatype is only supported for data elements"));
		}
	}

	@Override
	public ImmutableSet<IXMLDiscreteValue> getDiscreteValues() {
		if (this.contentType == ContentType.DATA) {
			return this.discreteValues;
		} else {
			throw logger.throwing(new IllegalStateException("Discrete values are only supported for data elements"));
		}
	}

	@Override
	public Boolean isFixedValueset() {
		if (this.contentType == ContentType.DATA) {
			return this.fixedValueset;
		} else {
			throw logger.throwing(new IllegalStateException("Discrete values are only supported for data elements"));
		}
	}

	@Override
	public ImmutableList<IXMLElementReference> getElements() {
		if ((this.contentType == ContentType.ELEMENT) || (this.contentType == ContentType.MIXED)) {
			return this.elements;
		} else {
			throw logger
				.throwing(new IllegalStateException("Sub-Elements are only supported for element and mixed content"));
		}
	}

	@Override
	public ElementArrangement getElementArrangement() {
		if (this.contentType == ContentType.ELEMENT) {
			return this.elementArrangement;
		} else {
			throw logger
				.throwing(new IllegalStateException("Element arrangement is only supported for element-only elements"));
		}
	}

	@Override
	public Optional<Boolean> isUserModifiable() {
		if (this.contentType == ContentType.ELEMENT) {
			throw logger.throwing(
					new IllegalStateException("User modification tagging is not supported for element-only elements"));
		} else {
			return Optional.ofNullable(this.userModifiable);
		}
	}

	/**
	 * Creates a builder to build {@link XMLElementType} and initialize it with the
	 * given object.
	 *
	 * @param xMLElementType to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IXMLElementType xMLElementType) {
		return new Builder(xMLElementType);
	}

	/**
	 * Builder to build {@link XMLElementType}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private Set<IXMLAttribute> attributes = new HashSet<>();
		private ContentType contentType;
		private XMLDatatype datatype;
		private Integer maxLength;
		private Integer minValue;
		private Integer maxValue;
		private Set<IXMLDiscreteValue> discreteValues = new HashSet<>();
		private Boolean fixedValueset;
		private Boolean userModifiable;
		private List<IXMLElementReference> elements = new ArrayList<>();
		private ElementArrangement elementArrangement = ElementArrangement.ALL;

		/**
		 * Create a new builder.
		 */
		public Builder() {
			this.id = UUID.randomUUID();
		}

		/**
		 * Creates a new builder.
		 *
		 * @param id
		 */
		public Builder(UUID id) {
			this.id = id;
		}

		private Builder(IXMLElementType xMLElementType) {
			this.id = xMLElementType.getID();
			this.comment = xMLElementType.getComment().orElse(null);
			this.contentType = xMLElementType.getContentType();
			if (this.contentType == ContentType.DATA) {
				this.datatype = xMLElementType.getDatatype();
				if (this.datatype == XMLDatatype.STRING) {
					this.maxLength = xMLElementType.getMaxLength().orElse(null);
				}
				if (this.datatype == XMLDatatype.INTEGER) {
					this.minValue = xMLElementType.getMinValue().orElse(null);
					this.maxValue = xMLElementType.getMaxValue().orElse(null);
				}
				this.fixedValueset = xMLElementType.isFixedValueset();
			}
			if (this.contentType != ContentType.ELEMENT) {
				this.userModifiable = xMLElementType.isUserModifiable().orElse(null);
			} else {
				this.elementArrangement = xMLElementType.getElementArrangement();
			}
			// create deep copy of attributes
			xMLElementType.getAttributes().forEach(attr -> this.attributes.add(XMLAttribute.builderFrom(attr).build()));

			// create deep copy of discrete values
			if (this.contentType == ContentType.DATA) {
				xMLElementType.getDiscreteValues()
					.forEach(val -> this.discreteValues.add(XMLDiscreteValue.builderFrom(val).build()));
			}

			// create deep copy of element references
			if ((this.contentType == ContentType.ELEMENT) || (this.contentType == ContentType.MIXED)) {
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
		 * Builder method for attributes parameter.
		 *
		 * @param attribute field to set
		 * @return builder
		 */
		public Builder addAttribute(IXMLAttribute attribute) {
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
		 * Builder method for datatype parameter.
		 *
		 * @param datatype field to set
		 * @return builder
		 */
		public Builder withDatatype(XMLDatatype datatype) {
			this.datatype = datatype;
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
		public Builder addElement(IXMLElementReference element) {
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
		 * Adds the resulting object to an {@link XMLSchema} builder and returns the
		 * object for further processing.
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

}
