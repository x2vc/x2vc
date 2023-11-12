package org.x2vc.xml.value;

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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.*;
import org.x2vc.xml.document.*;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IPrefixSelector.PrefixData;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.thedeanda.lorem.LoremIpsum;

import net.sf.saxon.s9api.OccurrenceIndicator;

/**
 * Standard implementation of {@link IValueGenerator}.
 */
public class ValueGenerator implements IValueGenerator {

	private record GeneratedValue(String value, boolean requested) {
	}

	/**
	 * When an element or attribute with non-fixed discrete values is encountered: What should be the ratio of the
	 * discrete values selected as opposed to randomly generated values?
	 */
	private double discreteValueSelectionRatio;

	/**
	 * When a string value is generated, how many words should be generated randomly?
	 */
	private int stringMinWordCount;
	private int stringMaxWordCount;

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private IDocumentRequest request;
	private IPrefixSelector prefixSelector;
	private IXMLSchema schema;

	private String valuePrefix;
	private Integer valueLength;
	private List<IValueDescriptor> valueDescriptors;
	private LoremIpsum textGenerator;
	int nextGeneratedValueCounter = 1;

	/**
	 * Creates a new value generator.
	 *
	 * @param schemaManager
	 * @param prefixSelector
	 * @param request
	 * @param discreteValueSelectionRatio
	 * @param stringMinWordCount
	 * @param stringMaxWordCount
	 */
	@Inject
	public ValueGenerator(ISchemaManager schemaManager, IPrefixSelector prefixSelector,
			@Assisted IDocumentRequest request,
			@TypesafeConfig("x2vc.xml.value.discrete_value_selection_ratio") Double discreteValueSelectionRatio,
			@TypesafeConfig("x2vc.xml.value.string_min_word_count") Integer stringMinWordCount,
			@TypesafeConfig("x2vc.xml.value.string_max_word_count") Integer stringMaxWordCount) {
		this.schemaManager = schemaManager;
		this.prefixSelector = prefixSelector;
		this.request = request;
		this.discreteValueSelectionRatio = discreteValueSelectionRatio;
		this.stringMinWordCount = stringMinWordCount;
		this.stringMaxWordCount = stringMaxWordCount;
		this.valueDescriptors = Lists.newArrayList();
		this.textGenerator = LoremIpsum.getInstance();
	}

	@Override
	public String generateValue(ISetAttributeRule rule) {
		logger.traceEntry("for attribute {}", rule.getAttributeID());
		loadSchema();
		final IAttribute attribute = this.schema.getObjectByID(rule.getAttributeID(), IAttribute.class);
		final Optional<IRequestedValue> requestedValue = rule.getRequestedValue();
		final GeneratedValue genValue = generateValueForDataObject(attribute, requestedValue);
		this.valueDescriptors
			.add(new ValueDescriptor(rule.getAttributeID(), rule.getID(), genValue.value, genValue.requested));
		return logger.traceExit("with generated value \"{}\"", genValue.value);
	}

	/**
	 * @throws IllegalStateException
	 */
	private void loadSchema() throws IllegalStateException {
		if (this.schema == null) {
			this.schema = this.schemaManager.getSchema(this.request.getStylesheeURI(), this.request.getSchemaVersion());
		}
	}

	@Override
	public String generateValue(IAddDataContentRule rule) {
		logger.traceEntry("for element {}", rule.getElementID());
		loadSchema();
		final IElementType element = this.schema.getObjectByID(rule.getElementID(), IElementType.class);
		final Optional<IRequestedValue> requestedValue = rule.getRequestedValue();
		final GeneratedValue genValue = generateValueForDataObject(element, requestedValue);
		this.valueDescriptors
			.add(new ValueDescriptor(rule.getElementID(), rule.getID(), genValue.value, genValue.requested));
		return logger.traceExit("with generated value \"{}\"", genValue.value);
	}

	/**
	 * Generate a value for a schema object (an attribute or an element), taking into account a requested value.
	 *
	 * @param schemaObject
	 * @param requestedValue
	 * @return a generated value for the schema object
	 */
	private GeneratedValue generateValueForDataObject(IDataObject schemaObject,
			Optional<IRequestedValue> requestedValue) {
		logger.traceEntry("for schema object {}", schemaObject.getID());
		GeneratedValue value = null;

		// see if a value was requested and if so, if it matches the
		if (requestedValue.isPresent()) {
			final String rV = requestedValue.get().getValue();
			if (requestedValueIsValidForDataObject(rV, schemaObject)) {
				value = new GeneratedValue(rV, true);
			}
		}

		// if no value was requested or the requested value was invalid, generate a new
		// value
		if (value == null) {
			value = new GeneratedValue(generateValueForDataObject(schemaObject), false);
		}

		return logger.traceExit("with generated value \"{}\"", value);
	}

	/**
	 * Determines whether a requested value is valid for a schema object.
	 *
	 * @param value
	 * @param schemaObject
	 * @return whether the requested value is valid for the schema object
	 */
	private boolean requestedValueIsValidForDataObject(String value, IDataObject schemaObject) {
		switch (schemaObject.getDataType()) {
		case BOOLEAN:
			return (value.equals("true") || value.equals("false"));
		case INTEGER:
			return requestedValueIsValidForIntegerObject(value, schemaObject);
		case STRING:
			return requestedValueIsValidForStringObject(value, schemaObject);
		default:
			// no validation for other types at the moment
			return true;
		}
	}

	/**
	 * Determines whether a requested value is valid for an integer schema object.
	 *
	 * @param value
	 * @param schemaObject
	 * @return
	 */
	private boolean requestedValueIsValidForIntegerObject(String value, IDataObject schemaObject) {
		logger.traceEntry();
		boolean result = true;
		try {
			final int intValue = Integer.parseInt(value);
			final Optional<Integer> minValue = schemaObject.getMinValue();
			if (minValue.isPresent() && (intValue < minValue.get())) {
				logger.warn("requested value {} is lower than the minimum value {} and will be disregarded", value,
						minValue.get());
				result = false;
			}
			final Optional<Integer> maxValue = schemaObject.getMaxValue();
			if (maxValue.isPresent() && (intValue > maxValue.get())) {
				logger.warn("requested value {} is greater than the maximum value {} and will be disregarded", value,
						maxValue.get());
				result = false;
			}
			final Collection<IDiscreteValue> discreteValues = schemaObject.getDiscreteValues();
			if (!discreteValues.isEmpty() && schemaObject.isFixedValueset().orElse(false) && (discreteValues.stream()
				.noneMatch(dv -> (dv.getDataType() == XMLDataType.INTEGER) && (dv.asInteger() == intValue)))) {
				logger.warn("requested value {} is not part of the fixed value set", value);
				result = false;

			}
		} catch (final NumberFormatException e) {
			logger.warn("requested value {} is not a valid integer and will be disregarded", value);
			result = false;
		}
		return logger.traceExit(result);
	}

	/**
	 * Determines whether a requested value is valid for a string schema object.
	 *
	 * @param value
	 * @param schemaObject
	 * @return
	 */
	private boolean requestedValueIsValidForStringObject(String value, IDataObject schemaObject) {
		logger.traceEntry();
		boolean result = true;
		final Optional<Integer> maxLength = schemaObject.getMaxLength();
		if (maxLength.isPresent() && (value.length() > maxLength.get())) {
			logger.warn("requested value \"{}\" is longer than the maximum length {} and will be disregarded", value,
					maxLength.get());
			result = false;
		}
		final Collection<IDiscreteValue> discreteValues = schemaObject.getDiscreteValues();
		if (!discreteValues.isEmpty() && schemaObject.isFixedValueset().orElse(false) && (discreteValues.stream()
			.noneMatch(dv -> (dv.getDataType() == XMLDataType.STRING) && (dv.asString().equals(value))))) {
			logger.warn("requested value {} is not part of the fixed value set", value);
			result = false;

		}
		return logger.traceExit(result);
	}

	/**
	 * Generate a value for a schema object (an attribute or an element).
	 *
	 * @param schemaObject
	 * @return
	 */
	private String generateValueForDataObject(IDataObject schemaObject) {
		final XMLDataType dataType = schemaObject.getDataType();
		if (dataType == null) {
			throw logger.throwing(new IllegalStateException(
					String.format("schema object %s has no valid data type set", schemaObject.getID())));
		}
		switch (dataType) {
		case BOOLEAN:
			return (ThreadLocalRandom.current().nextBoolean()) ? "true" : "false";
		case INTEGER:
			return generateValueForIntegerObject(schemaObject);
		case STRING:
			return generateValueForStringObject(schemaObject);
		default:
			// no generation for other types at the moment
			throw new IllegalStateException(
					String.format("no value generation for data type %s implemented", schemaObject.getDataType()));
		}
	}

	/**
	 * @param schemaObject
	 * @return
	 */
	private String generateValueForIntegerObject(IDataObject schemaObject) {
		logger.traceEntry();
		String result = null;

		// handle discrete values first
		if (!schemaObject.getDiscreteValues().isEmpty()) {
			final IDiscreteValue[] discreteValues = schemaObject.getDiscreteValues()
				.toArray(new IDiscreteValue[0]);
			// check whether we have to select from a fixed value set
			boolean selectDiscreteValue = false;
			if (schemaObject.isFixedValueset().orElse(false)) {
				// yes - select one of the values all of the time
				selectDiscreteValue = true;
			} else {
				// no, the values are just "interesting" values - select one of these, but not
				// all of the time
				selectDiscreteValue = (ThreadLocalRandom.current().nextDouble() < this.discreteValueSelectionRatio);
			}
			if (selectDiscreteValue) {
				final int index = ThreadLocalRandom.current().nextInt(discreteValues.length);
				logger.debug("selecting discrete value {} of {} values available", index + 1, discreteValues.length);
				result = Integer.toString(discreteValues[index].asInteger());
			}
		}

		// if no discrete value was used, generate a new one within the limits specified
		if (result == null) {
			final Integer minValue = schemaObject.getMinValue().orElse(Integer.MIN_VALUE);
			final Integer maxValue = schemaObject.getMaxValue().orElse(Integer.MAX_VALUE - 1);
			result = Integer.toString(ThreadLocalRandom.current().nextInt(minValue, maxValue + 1));
		}
		return logger.traceExit(result);
	}

	/**
	 * @param schemaObject
	 * @return
	 */
	private String generateValueForStringObject(IDataObject schemaObject) {
		logger.traceEntry();
		String result = null;

		// handle discrete values first
		if (!schemaObject.getDiscreteValues().isEmpty()) {
			final IDiscreteValue[] discreteValues = schemaObject.getDiscreteValues()
				.toArray(new IDiscreteValue[0]);
			// check whether we have to select from a fixed value set
			boolean selectDiscreteValue = false;
			if (schemaObject.isFixedValueset().orElse(false)) {
				// yes - select one of the values all of the time
				selectDiscreteValue = true;
			} else {
				// no, the values are just "interesting" values - select one of these, but not
				// all of the time
				selectDiscreteValue = (ThreadLocalRandom.current().nextDouble() < this.discreteValueSelectionRatio);
			}
			if (selectDiscreteValue) {
				final int index = ThreadLocalRandom.current().nextInt(discreteValues.length);
				logger.debug("selecting discrete value {} of {} values available", index + 1, discreteValues.length);
				result = discreteValues[index].asString();
			}
		}

		// if no discrete value was used, generate a new one within the limits specified
		if (result == null) {
			final Optional<Integer> maxLength = schemaObject.getMaxLength();
			result = generateRandomStringWithPrefix(maxLength);
		}
		return logger.traceExit(result);
	}

	/**
	 * Generates a random string starting with a generated prefix.
	 *
	 * @param maxLength
	 * @return the random string
	 */
	protected String generateRandomStringWithPrefix(final Optional<Integer> maxLength) {
		String result;
		// start with a prefixed value to provide a means of identification
		final int counterLength = this.getValueLength() - this.getValuePrefix().length();
		final String format = "%s%0" + counterLength + "d";
		final String prefixValue = String.format(format, this.valuePrefix, this.nextGeneratedValueCounter++);
		final String text = this.textGenerator.getWords(this.stringMinWordCount, this.stringMaxWordCount);
		result = prefixValue + " " + text;

		// ensure the length restriction is met, if any is specified
		if (maxLength.isPresent()) {
			final Integer maxLengthValue = maxLength.get();
			if (result.length() > maxLengthValue) {
				result = result.substring(0, maxLengthValue - 1);
			}
		}
		return result;
	}

	@Override
	public String generateValue(IAddRawContentRule rule) {
		logger.traceEntry("for element {}", rule.getElementID());
		loadSchema();
		String value = null;
		final Optional<IRequestedValue> requestedValue = rule.getRequestedValue();
		if (requestedValue.isPresent()) {
			value = requestedValue.get().getValue();
			this.valueDescriptors.add(new ValueDescriptor(rule.getElementID(), rule.getID(), value, true));
		} else {
			if (this.request.getMixedContentGenerationMode() == MixedContentGenerationMode.FULL) {
				// generate some raw content: some text with a few tags inside
				value = switch (ThreadLocalRandom.current().nextInt(6)) {
				case 1 -> "#PREFIX# <b>#TEXT#</b> #TEXT#";
				case 2 -> "#PREFIX# <i>#TEXT#</i> #TEXT#";
				case 3 -> "#PREFIX# <a href=\"foobar\">#TEXT#</a> #TEXT#";
				case 4 -> "#PREFIX# <p>#TEXT#</p> #TEXT#";
				case 5 -> "#PREFIX# <div id=\"foobar\">#TEXT#</div> #TEXT#";
				default -> "#PREFIX# <br/> #TEXT#";
				};
			} else {
				// The raw content should NOT contain tags (used for schema evolution).
				value = "#PREFIX# #TEXT#";
			}
			while (value.contains("#PREFIX#")) {
				final int counterLength = this.getValueLength() - this.getValuePrefix().length();
				final String format = "%s%0" + counterLength + "d";
				final String prefixValue = String.format(format, this.valuePrefix, this.nextGeneratedValueCounter++);
				value = value.replaceFirst("#PREFIX#", prefixValue);
			}
			while (value.contains("#TEXT#")) {
				value = value.replaceFirst("#TEXT#", this.textGenerator.getWords(5, 50));
			}
			this.valueDescriptors.add(new ValueDescriptor(rule.getElementID(), rule.getID(), value));
		}
		return logger.traceExit("with generated value \"{}\"", value);
	}

	@Override
	public IExtensionFunctionResult generateValue(IExtensionFunctionRule rule) {
		logger.traceEntry("for function {}", rule.getFunctionID());
		loadSchema();
		final IExtensionFunction function = this.schema.getObjectByID(rule.getFunctionID(), IExtensionFunction.class);
		final Optional<IRequestedValue> oRequestedValue = rule.getRequestedValue();
		IExtensionFunctionResult result = null;
		if (oRequestedValue.isPresent()) {
			result = generateRequestedFunctionResult(function, oRequestedValue.get());
		} else {
			result = generateFunctionResult(function);
		}
		this.valueDescriptors
			.add(new ValueDescriptor(rule.getFunctionID(), rule.getID(), result.getXDMValue().toString(),
					oRequestedValue.isPresent()));
		return logger.traceExit("with generated result \"{}\"", result);
	}

	/**
	 * @param function
	 * @return
	 */
	private IExtensionFunctionResult generateFunctionResult(IExtensionFunction function) {
		logger.traceEntry();
		IExtensionFunctionResult result = null;

		final IFunctionSignatureType resultType = function.getResultType();
		if (resultType.getOccurrenceIndicator() != OccurrenceIndicator.ONE) {
			// TODO Extension Functions: support other return type occurrence indicators
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an occurrence of %s is not yet supported",
							resultType.getOccurrenceIndicator())));
		}

		switch (resultType.getSequenceItemType()) {
		case BOOLEAN:
			result = new BooleanExtensionFunctionResult(function.getID(),
					ThreadLocalRandom.current().nextBoolean());
			break;
		case INT:
			result = new IntegerExtensionFunctionResult(function.getID(),
					ThreadLocalRandom.current().nextInt());
			break;
		case INTEGER:
			result = new IntegerExtensionFunctionResult(function.getID(),
					ThreadLocalRandom.current().nextInt());
			break;
		case STRING:
			result = new StringExtensionFunctionResult(function.getID(),
					generateRandomStringWithPrefix(Optional.empty()));
			break;
		default:
			// TODO Extension Functions: support other return type data types
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an item type of %s is not yet supported",
							resultType.getSequenceItemType())));
		}
		return logger.traceExit(result);
	}

	/**
	 * @param function
	 * @param requestedValue
	 * @return
	 */
	private IExtensionFunctionResult generateRequestedFunctionResult(IExtensionFunction function,
			IRequestedValue requestedValue) {
		logger.traceEntry();
		IExtensionFunctionResult result = null;

		final IFunctionSignatureType resultType = function.getResultType();
		if (resultType.getOccurrenceIndicator() != OccurrenceIndicator.ONE) {
			// TODO Extension Functions: support other return type occurrence indicators
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an occurrence of %s is not yet supported",
							resultType.getOccurrenceIndicator())));
		}

		switch (resultType.getSequenceItemType()) {
		case BOOLEAN:
			result = new BooleanExtensionFunctionResult(function.getID(),
					Boolean.parseBoolean(requestedValue.getValue()));
			break;
		case INT:
			result = new IntegerExtensionFunctionResult(function.getID(),
					Integer.parseInt(requestedValue.getValue()));
			break;
		case INTEGER:
			result = new IntegerExtensionFunctionResult(function.getID(),
					Integer.parseInt(requestedValue.getValue()));
			break;
		case STRING:
			result = new StringExtensionFunctionResult(function.getID(),
					requestedValue.getValue());
			break;
		default:
			// TODO Extension Functions: support other return type data types
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an item tyoe of %s is not yet supported",
							resultType.getSequenceItemType())));
		}
		return logger.traceExit(result);
	}

	@Override
	public IStylesheetParameterValue generateValue(IStylesheetParameterRule rule) {
		logger.traceEntry("for parameter {}", rule.getParameterID());
		loadSchema();
		final IStylesheetParameter parameter = this.schema.getObjectByID(rule.getParameterID(),
				IStylesheetParameter.class);
		final Optional<IRequestedValue> oRequestedValue = rule.getRequestedValue();
		IStylesheetParameterValue result = null;
		if (oRequestedValue.isPresent()) {
			result = generateRequestedParameterResult(parameter, oRequestedValue.get());
		} else {
			result = generateParameterResult(parameter);
		}
		this.valueDescriptors
			.add(new ValueDescriptor(rule.getParameterID(), rule.getID(), result.getXDMValue().toString(),
					oRequestedValue.isPresent()));
		return logger.traceExit("with generated result \"{}\"", result);
	}

	/**
	 * @param parameter
	 * @return
	 */
	private IStylesheetParameterValue generateParameterResult(IStylesheetParameter parameter) {
		logger.traceEntry();
		IStylesheetParameterValue result = null;

		final IFunctionSignatureType resultType = parameter.getType();
		if (resultType.getOccurrenceIndicator() != OccurrenceIndicator.ONE) {
			// TODO Extension Parameters: support other return type occurrence indicators
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an occurrence of %s is not yet supported",
							resultType.getOccurrenceIndicator())));
		}

		switch (resultType.getSequenceItemType()) {
		case BOOLEAN:
			result = new BooleanStylesheetParameterValue(parameter.getID(),
					ThreadLocalRandom.current().nextBoolean());
			break;
		case INT:
			result = new IntegerStylesheetParameterValue(parameter.getID(),
					ThreadLocalRandom.current().nextInt());
			break;
		case INTEGER:
			result = new IntegerStylesheetParameterValue(parameter.getID(),
					ThreadLocalRandom.current().nextInt());
			break;
		case STRING:
			result = new StringStylesheetParameterValue(parameter.getID(),
					generateRandomStringWithPrefix(Optional.empty()));
			break;
		default:
			// TODO Extension Parameters: support other return type data types
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an item tyoe of %s is not yet supported",
							resultType.getSequenceItemType())));
		}
		return logger.traceExit(result);
	}

	/**
	 * @param parameter
	 * @param requestedValue
	 * @return
	 */
	private IStylesheetParameterValue generateRequestedParameterResult(IStylesheetParameter parameter,
			IRequestedValue requestedValue) {
		logger.traceEntry();
		IStylesheetParameterValue result = null;

		final IFunctionSignatureType resultType = parameter.getType();
		if (resultType.getOccurrenceIndicator() != OccurrenceIndicator.ONE) {
			// TODO Extension Parameters: support other return type occurrence indicators
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an occurrence of %s is not yet supported",
							resultType.getOccurrenceIndicator())));
		}

		switch (resultType.getSequenceItemType()) {
		case BOOLEAN:
			result = new BooleanStylesheetParameterValue(parameter.getID(),
					Boolean.parseBoolean(requestedValue.getValue()));
			break;
		case INT:
			result = new IntegerStylesheetParameterValue(parameter.getID(),
					Integer.parseInt(requestedValue.getValue()));
			break;
		case INTEGER:
			result = new IntegerStylesheetParameterValue(parameter.getID(),
					Integer.parseInt(requestedValue.getValue()));
			break;
		case STRING:
			result = new StringStylesheetParameterValue(parameter.getID(),
					requestedValue.getValue());
			break;
		default:
			// TODO Extension Parameters: support other return type data types
			throw logger.throwing(new UnsupportedOperationException(
					String.format("Generating a value with an item tyoe of %s is not yet supported",
							resultType.getSequenceItemType())));
		}
		return logger.traceExit(result);
	}

	@Override
	public String getValuePrefix() {
		if (this.valuePrefix == null) {
			selectPrefixAndLength();
		}
		return this.valuePrefix;
	}

	@Override
	public int getValueLength() {
		if (this.valueLength == null) {
			selectPrefixAndLength();
		}
		return this.valueLength;
	}

	@Override
	public ImmutableSet<IValueDescriptor> getValueDescriptors() {
		return ImmutableSet.copyOf(this.valueDescriptors);
	}

	/**
	 * Uses the {@link IPrefixSelector} to determine a unique prefix and a default length for the generated values.
	 */
	private void selectPrefixAndLength() {
		logger.traceEntry();
		final PrefixData data = this.prefixSelector.selectPrefix(this.request.getStylesheeURI());
		logger.debug("will use prefix {} and a length {} to generate values", data.prefix(), data.valueLength());
		this.valuePrefix = data.prefix();
		this.valueLength = data.valueLength();
		logger.traceExit();
	}

}
