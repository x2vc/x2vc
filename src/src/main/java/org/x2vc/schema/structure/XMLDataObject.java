package org.x2vc.schema.structure;

import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for {@link ISchemaObject}s that can contain typed data, like an attribute or an element with content
 * type DATA.
 */
public abstract class XMLDataObject extends AbstractSchemaObject implements IDataObject {

	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private final XMLDataType dataType;

	@XmlAttribute
	private final Integer maxLength;

	@XmlAttribute
	private final Integer minValue;

	@XmlAttribute
	private final Integer maxValue;

	@XmlElement(type = XMLDiscreteValue.class, name = "discreteValue")
	private final List<IDiscreteValue> discreteValues;

	@XmlAttribute
	private final Boolean fixedValueset;

	protected XMLDataObject(UUID id, String comment, XMLDataType dataType, Integer maxLength, Integer minValue,
			Integer maxValue, List<IDiscreteValue> discreteValues, Boolean fixedValueset) {
		super(id, comment);
		this.dataType = dataType;
		this.maxLength = maxLength;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.discreteValues = discreteValues;
		this.fixedValueset = fixedValueset;
	}

	@Override
	public XMLDataType getDataType() {
		return this.dataType;
	}

	@Override
	public Optional<Integer> getMaxLength() {
		if (this.dataType == XMLDataType.STRING) {
			return Optional.ofNullable(this.maxLength);
		} else {
			throw (logger.throwing(new IllegalStateException("A maximum length is only supported for type STRING")));
		}
	}

	@Override
	public Optional<Integer> getMinValue() {
		if (this.dataType == XMLDataType.INTEGER) {
			return Optional.ofNullable(this.minValue);
		} else {
			throw (logger.throwing(new IllegalStateException("A minimum value is only supported for type INTEGER")));
		}
	}

	@Override
	public Optional<Integer> getMaxValue() {
		if (this.dataType == XMLDataType.INTEGER) {
			return Optional.ofNullable(this.maxValue);
		} else {
			throw (logger.throwing(new IllegalStateException("A maximum value is only supported for type INTEGER")));
		}
	}

	@Override
	public Collection<IDiscreteValue> getDiscreteValues() {
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
		result = prime * result + Objects.hash(this.dataType, this.discreteValues, this.fixedValueset, this.maxLength,
				this.maxValue, this.minValue);
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
		if (!(obj instanceof XMLDataObject)) {
			return false;
		}
		final XMLDataObject other = (XMLDataObject) obj;
		return this.dataType == other.dataType && Objects.equals(this.discreteValues, other.discreteValues)
				&& Objects.equals(this.fixedValueset, other.fixedValueset)
				&& Objects.equals(this.maxLength, other.maxLength)
				&& Objects.equals(this.maxValue, other.maxValue) && Objects.equals(this.minValue, other.minValue)
				&& this.getClass().equals(other.getClass()); // added manually!
	}

}
