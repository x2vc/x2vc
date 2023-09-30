package org.x2vc.schema.structure;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for {@link IXMLSchemaObject}s that can contain typed data, like an attribute or an element with content
 * type DATA.
 */
public abstract class XMLDataObject extends AbstractSchemaObject implements IXMLDataObject {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	protected XMLDatatype datatype;

	@XmlAttribute
	protected Integer maxLength;

	@XmlAttribute
	protected Integer minValue;

	@XmlAttribute
	protected Integer maxValue;

	@XmlElement(type = XMLDiscreteValue.class, name = "discreteValue")
	protected Set<IXMLDiscreteValue> discreteValues;

	@XmlAttribute
	protected Boolean fixedValueset;

	@Override
	public XMLDatatype getDatatype() {
		return this.datatype;
	}

	@Override
	public Optional<Integer> getMaxLength() {
		if (this.datatype == XMLDatatype.STRING) {
			return Optional.ofNullable(this.maxLength);
		} else {
			throw (logger.throwing(new IllegalStateException("A maximum length is only supported for type STRING")));
		}
	}

	@Override
	public Optional<Integer> getMinValue() {
		if (this.datatype == XMLDatatype.INTEGER) {
			return Optional.ofNullable(this.minValue);
		} else {
			throw (logger.throwing(new IllegalStateException("A minimum value is only supported for type INTEGER")));
		}
	}

	@Override
	public Optional<Integer> getMaxValue() {
		if (this.datatype == XMLDatatype.INTEGER) {
			return Optional.ofNullable(this.maxValue);
		} else {
			throw (logger.throwing(new IllegalStateException("A maximum value is only supported for type INTEGER")));
		}
	}

	@Override
	public Set<IXMLDiscreteValue> getDiscreteValues() {
		return this.discreteValues;
	}

	@Override
	public Optional<Boolean> isFixedValueset() {
		if (this.discreteValues.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(this.fixedValueset);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.discreteValues, this.fixedValueset, this.maxLength, this.maxValue,
				this.minValue, this.datatype);
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
		final XMLDataObject other = (XMLDataObject) obj;
		return Objects.equals(this.discreteValues, other.discreteValues)
				&& Objects.equals(this.fixedValueset, other.fixedValueset)
				&& Objects.equals(this.maxLength, other.maxLength) && Objects.equals(this.maxValue, other.maxValue)
				&& Objects.equals(this.minValue, other.minValue) && this.datatype == other.datatype;
	}

}
