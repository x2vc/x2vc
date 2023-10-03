package org.x2vc.xml.value;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.*;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IPrefixSelector.PrefixData;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

@ExtendWith(MockitoExtension.class)
class ValueGeneratorTest {

	// stylesheet
	@Mock
	private URI stylesheetURI;
	@Mock
	private IStylesheetInformation stylesheet;

	// schema
	@Mock
	private ISchemaManager schemaManager;
	private URI schemaURI;
	private int schemaVersion;
	@Mock
	private IXMLSchema schema;

	// attribute in schema
	private UUID attributeID;
	@Mock
	private IAttribute attribute;

	// element in schema
	private UUID elementID;
	@Mock
	private IElementType element;

	// request
	@Mock
	private IDocumentRequest request;

	// prefix selector
	@Mock
	private IPrefixSelector prefixSelector;
	private static final int TEST_VAL_LENGTH = 8;
	private static final String TEST_PREFIX = "a42b";

	// rules
	private UUID ruleID;
	@Mock
	private ISetAttributeRule setAttributeRule;
	@Mock
	private IAddDataContentRule addDataContentRule;
	@Mock
	private IAddRawContentRule addRawContentRule;

	// requested value
	@Mock
	private IRequestedValue requestedValue;

	// value generator under test
	private ValueGenerator valueGenerator;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// stylesheet
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		lenient().when(this.stylesheet.getURI()).thenReturn(this.stylesheetURI);

		// schema
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		this.schemaVersion = 1;
		lenient().when(this.schemaManager.getSchema(this.stylesheetURI, this.schemaVersion)).thenReturn(this.schema);
		lenient().when(this.schema.getURI()).thenReturn(this.schemaURI);
		lenient().when(this.schema.getVersion()).thenReturn(this.schemaVersion);
		lenient().when(this.schema.getStylesheetURI()).thenReturn(this.stylesheetURI);

		// attribute in schema
		this.attributeID = UUID.randomUUID();
		lenient().when(this.attribute.getID()).thenReturn(this.attributeID);
		lenient().when(this.schema.getObjectByID(this.attributeID)).thenReturn(this.attribute);
		lenient().when(this.schema.getObjectByID(eq(this.attributeID), any())).thenReturn(this.attribute);

		// element in schema
		this.elementID = UUID.randomUUID();
		lenient().when(this.element.getID()).thenReturn(this.elementID);
		lenient().when(this.schema.getObjectByID(this.elementID)).thenReturn(this.element);
		lenient().when(this.schema.getObjectByID(eq(this.elementID), any())).thenReturn(this.element);

		// request
		lenient().when(this.request.getStylesheeURI()).thenReturn(this.stylesheetURI);
		lenient().when(this.request.getSchemaURI()).thenReturn(this.schemaURI);
		lenient().when(this.request.getSchemaVersion()).thenReturn(this.schemaVersion);

		// prefix selector
		lenient().when(this.prefixSelector.selectPrefix(this.stylesheetURI))
			.thenReturn(new PrefixData(TEST_PREFIX, TEST_VAL_LENGTH));

		// rules
		this.ruleID = UUID.randomUUID();

		// value generator under test
		this.valueGenerator = new ValueGenerator(this.schemaManager, this.prefixSelector, this.request, 0.75, 10, 50);

	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#getValuePrefix()} and
	 * {@link org.x2vc.xml.value.ValueGenerator#getValueLength()}.
	 */
	@Test
	void testGenerateValue_PrefixSelectorAccess() {
		assertEquals(TEST_PREFIX, this.valueGenerator.getValuePrefix());
		assertEquals(TEST_VAL_LENGTH, this.valueGenerator.getValueLength());
	}

	/**
	 * Prepares the {@link ISetAttributeRule} mockup for use
	 *
	 * @param withRequestedValue
	 */
	void prepareSetAttributeRule(boolean withRequestedValue) {
		lenient().when(this.setAttributeRule.getID()).thenReturn(this.ruleID);
		lenient().when(this.setAttributeRule.getAttributeID()).thenReturn(this.attributeID);
		if (withRequestedValue) {
			lenient().when(this.setAttributeRule.getRequestedValue()).thenReturn(Optional.of(this.requestedValue));
		} else {
			lenient().when(this.setAttributeRule.getRequestedValue()).thenReturn(Optional.empty());
		}
	}

	/**
	 * Prepares the {@link IAttribute} mockup as string for use without discrete values
	 *
	 * @param maxLength
	 */
	void prepareAttributeForString(@Nullable Integer maxLength) {
		lenient().when(this.attribute.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(this.attribute.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.attribute.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.attribute.getMaxLength()).thenReturn(Optional.ofNullable(maxLength));
		lenient().when(this.attribute.getMinValue()).thenThrow(new IllegalStateException()); // n/a for string
		lenient().when(this.attribute.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for string
	}

	/**
	 * Prepares the {@link IAttribute} mockup as string for use with discrete values
	 */
	void prepareAttributeForString(@Nullable Integer maxLength, boolean fixedValueset, String... discreteValues) {
		final List<XMLDiscreteValue> valueList = Arrays.stream(discreteValues)
			.map(val -> XMLDiscreteValue.builder().withStringValue(val).build()).toList();
		lenient().when(this.attribute.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(this.attribute.getDiscreteValues()).thenReturn(Set.copyOf(valueList));
		lenient().when(this.attribute.isFixedValueset()).thenReturn(Optional.of(fixedValueset));
		lenient().when(this.attribute.getMaxLength()).thenReturn(Optional.ofNullable(maxLength));
		lenient().when(this.attribute.getMinValue()).thenThrow(new IllegalStateException()); // n/a for string
		lenient().when(this.attribute.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for string
	}

	/**
	 * Prepares the {@link IAttribute} mockup as boolean
	 */
	void prepareAttributeForBoolean() {
		lenient().when(this.attribute.getDataType()).thenReturn(XMLDataType.BOOLEAN);
		lenient().when(this.attribute.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.attribute.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.attribute.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for boolean
		lenient().when(this.attribute.getMinValue()).thenThrow(new IllegalStateException()); // n/a for boolean
		lenient().when(this.attribute.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for boolean
	}

	/**
	 * Prepares the {@link IAttribute} mockup as integer for use without discrete values
	 */
	void prepareAttributeForInteger(@Nullable Integer minValue, @Nullable Integer maxValue) {
		lenient().when(this.attribute.getDataType()).thenReturn(XMLDataType.INTEGER);
		lenient().when(this.attribute.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.attribute.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.attribute.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for integer
		lenient().when(this.attribute.getMinValue()).thenReturn(Optional.ofNullable(minValue));
		lenient().when(this.attribute.getMaxValue()).thenReturn(Optional.ofNullable(maxValue));
	}

	/**
	 * Prepares the {@link IAttribute} mockup as integer for use with discrete values
	 */
	void prepareAttributeForInteger(boolean fixedValueset, Integer... discreteValues) {
		final List<XMLDiscreteValue> valueList = Arrays.stream(discreteValues)
			.map(val -> XMLDiscreteValue.builder().withIntegerValue(val).build()).toList();
		lenient().when(this.attribute.getDataType()).thenReturn(XMLDataType.INTEGER);
		lenient().when(this.attribute.getDiscreteValues()).thenReturn(Set.copyOf(valueList));
		lenient().when(this.attribute.isFixedValueset()).thenReturn(Optional.of(fixedValueset));
		lenient().when(this.attribute.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for integer
		lenient().when(this.attribute.getMinValue()).thenReturn(Optional.empty());
		lenient().when(this.attribute.getMaxValue()).thenReturn(Optional.empty());
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_String() {
		prepareSetAttributeRule(false);
		prepareAttributeForString(null);

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertValueDescriptorPresent(this.attributeID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringMaxLength() {
		final int maxLength = 42;
		prepareSetAttributeRule(false);
		prepareAttributeForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.attributeID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringMaxLength_ShorterThanGeneratorLength() {
		final int maxLength = TEST_VAL_LENGTH - 1;
		prepareSetAttributeRule(false);
		prepareAttributeForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		// generated value should still start with prefix
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.attributeID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringMaxLength_ShorterThanPrefixLength() {
		final int maxLength = TEST_PREFIX.length() - 1;
		prepareSetAttributeRule(false);
		prepareAttributeForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		// can't do anything about the prefix, at least check the overall length
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.attributeID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringDiscreteValues() {
		prepareSetAttributeRule(false);
		prepareAttributeForString(null, false, "xxxx1", "xxxx2", "xxxx3");

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that at least some if the discrete values are present.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("xxxx1") + values.count("xxxx2") + values.count("xxxx3");
		assertTrue(totalDiscreteValueCount > 0, "discrete values should have appeared in here somewhere");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringFixedValues() {
		prepareSetAttributeRule(false);
		prepareAttributeForString(null, true, "xxxx1", "xxxx2", "xxxx3");

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that the sum of all fixed values matches the number of
		// iterations.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("xxxx1") + values.count("xxxx2") + values.count("xxxx3");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringRequestedValue() {
		final String value = "foobar42";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForString(null);

		assertEquals(value, this.valueGenerator.generateValue(this.setAttributeRule));
		assertValueDescriptorPresent(this.attributeID, value, true);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringRequestedValueTooLong() {
		final int maxLength = TEST_PREFIX.length() - 1;
		final String value = "foobar42foobar";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForString(maxLength);

		// if the requested value is too long, it can either be shorted or a new value
		// can be generated, but the maximum length may not be exceeded
		assertTrue(this.valueGenerator.generateValue(this.setAttributeRule).length() <= maxLength,
				"generated value too long");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_StringRequestedNotInFixedList() {
		final String value = "foobar42foobar";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForString(null, true, "xxxx1", "xxxx2", "xxxx3");

		// the generated value may not be the requested value
		assertNotEquals(value, this.valueGenerator.generateValue(this.setAttributeRule));
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_Boolean() {
		prepareSetAttributeRule(false);
		prepareAttributeForBoolean();

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		assertTrue(generatedValue.equals("true") || generatedValue.equals("false"));
		assertValueDescriptorPresent(this.attributeID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_BooleanRequestedValue() {
		final String value = "false";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForBoolean();

		// to rule out random influence, generate a number of values and check they ALL
		// match the requested value
		final int NUM_TESTS = 50;
		int requestedCount = 0;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			if (generatedValue.equals(value)) {
				requestedCount++;
			}
		}

		assertEquals(NUM_TESTS, requestedCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_BooleanRequestedValueInvalidType() {
		final String value = "rhubarb"; // not a valid boolean value
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForBoolean();

		final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
		assertTrue(generatedValue.equals("true") || generatedValue.equals("false"));
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_Integer() {
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(null, null);

		final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
		final Integer generatedInteger = Integer.parseInt(generatedString);
		assertNotNull(generatedInteger);
		assertValueDescriptorPresent(this.attributeID, generatedString, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerMin() {
		final int minValue = 352416;
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(minValue, null);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerMax() {
		final int maxValue = 625143;
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(null, maxValue);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerMinMax() {
		final int minValue = 352416;
		final int maxValue = 625143;
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(minValue, maxValue);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerDiscreteValues() {
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(false, 42, 21, 84);

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that at least some of the discrete values are present.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertTrue(totalDiscreteValueCount > 0, "discrete values should have appeared in here somewhere");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerFixedValues() {
		prepareSetAttributeRule(false);
		prepareAttributeForInteger(true, 42, 21, 84);

		// Again generate a larger number of values and check that the occurrence of all
		// values sums up to the total number of values generated.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerRequestedValue() {
		final String value = "123789";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForInteger(null, null);

		// Generate a larger number of values and check only the requested value occurs
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			assertEquals(value, generatedValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerRequestedValueInvalidType() {
		final String value = "foobar"; // hey, look, not an integer
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForInteger(null, null);

		// requested value must be ignored at this point
		final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
		final Integer generatedInteger = Integer.parseInt(generatedString);
		assertNotNull(generatedInteger);
		assertValueDescriptorPresent(this.attributeID, generatedString, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerRequestedValueTooLow() {
		final int minValue = 100;
		final String value = "50";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForInteger(minValue, null);

		// requested value must be ignored at this point - all generated must be above
		// the minValue
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerRequestedValueTooHigh() {
		final int maxValue = 10;
		final String value = "50";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareSetAttributeRule(true);
		prepareAttributeForInteger(null, maxValue);

		// requested value must be ignored at this point - all generated must be above
		// the minValue
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.setAttributeRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for
	 * {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.ISetAttributeRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_SetAttributeRule_IntegerRequestedValueNotInFixedValues() {
		when(this.requestedValue.getValue()).thenReturn("7");
		prepareSetAttributeRule(true);
		prepareAttributeForInteger(true, 42, 21, 84);

		// requested value must be ignored - only fixed values may occur
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.setAttributeRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Prepares the {@link IAddDataContentRule} mockup for use
	 *
	 * @param withRequestedValue
	 */
	void prepareAddDataContentRule(boolean withRequestedValue) {
		lenient().when(this.addDataContentRule.getID()).thenReturn(this.ruleID);
		lenient().when(this.addDataContentRule.getElementID()).thenReturn(this.elementID);
		if (withRequestedValue) {
			lenient().when(this.addDataContentRule.getRequestedValue()).thenReturn(Optional.of(this.requestedValue));
		} else {
			lenient().when(this.addDataContentRule.getRequestedValue()).thenReturn(Optional.empty());
		}
	}

	/**
	 * Prepares the {@link IElementType} mockup as string for use without discrete values
	 *
	 * @param maxLength
	 */
	void prepareElementForString(@Nullable Integer maxLength) {
		lenient().when(this.element.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(this.element.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.element.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.element.getMaxLength()).thenReturn(Optional.ofNullable(maxLength));
		lenient().when(this.element.getMinValue()).thenThrow(new IllegalStateException()); // n/a for string
		lenient().when(this.element.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for string
	}

	/**
	 * Prepares the {@link IElementType} mockup as string for use with discrete values
	 */
	void prepareElementForString(@Nullable Integer maxLength, boolean fixedValueset, String... discreteValues) {
		final List<XMLDiscreteValue> valueList = Arrays.stream(discreteValues)
			.map(val -> XMLDiscreteValue.builder().withStringValue(val).build()).toList();
		lenient().when(this.element.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(this.element.getDiscreteValues()).thenReturn(Set.copyOf(valueList));
		lenient().when(this.element.isFixedValueset()).thenReturn(Optional.of(fixedValueset));
		lenient().when(this.element.getMaxLength()).thenReturn(Optional.ofNullable(maxLength));
		lenient().when(this.element.getMinValue()).thenThrow(new IllegalStateException()); // n/a for string
		lenient().when(this.element.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for string
	}

	/**
	 * Prepares the {@link IElementType} mockup as boolean
	 */
	void prepareElementForBoolean() {
		lenient().when(this.element.getDataType()).thenReturn(XMLDataType.BOOLEAN);
		lenient().when(this.element.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.element.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.element.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for boolean
		lenient().when(this.element.getMinValue()).thenThrow(new IllegalStateException()); // n/a for boolean
		lenient().when(this.element.getMaxValue()).thenThrow(new IllegalStateException()); // n/a for boolean
	}

	/**
	 * Prepares the {@link IElementType} mockup as integer for use without discrete values
	 */
	void prepareElementForInteger(@Nullable Integer minValue, @Nullable Integer maxValue) {
		lenient().when(this.element.getDataType()).thenReturn(XMLDataType.INTEGER);
		lenient().when(this.element.getDiscreteValues()).thenReturn(Set.of());
		lenient().when(this.element.isFixedValueset()).thenReturn(Optional.empty());
		lenient().when(this.element.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for integer
		lenient().when(this.element.getMinValue()).thenReturn(Optional.ofNullable(minValue));
		lenient().when(this.element.getMaxValue()).thenReturn(Optional.ofNullable(maxValue));
	}

	/**
	 * Prepares the {@link IElementType} mockup as integer for use with discrete values
	 */
	void prepareElementForInteger(boolean fixedValueset, Integer... discreteValues) {
		final List<XMLDiscreteValue> valueList = Arrays.stream(discreteValues)
			.map(val -> XMLDiscreteValue.builder().withIntegerValue(val).build()).toList();
		lenient().when(this.element.getDataType()).thenReturn(XMLDataType.INTEGER);
		lenient().when(this.element.getDiscreteValues()).thenReturn(Set.copyOf(valueList));
		lenient().when(this.element.isFixedValueset()).thenReturn(Optional.of(fixedValueset));
		lenient().when(this.element.getMaxLength()).thenThrow(new IllegalStateException()); // n/a for integer
		lenient().when(this.element.getMinValue()).thenReturn(Optional.empty());
		lenient().when(this.element.getMaxValue()).thenReturn(Optional.empty());
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_String() {
		prepareAddDataContentRule(false);
		prepareElementForString(null);

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringMaxLength() {
		final int maxLength = 42;
		prepareAddDataContentRule(false);
		prepareElementForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringMaxLength_ShorterThanGeneratorLength() {
		final int maxLength = TEST_VAL_LENGTH - 1;
		prepareAddDataContentRule(false);
		prepareElementForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		// generated value should still start with prefix
		assertTrue(generatedValue.startsWith(TEST_PREFIX), "generated value does not start with prefix");
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringMaxLength_ShorterThanPrefixLength() {
		final int maxLength = TEST_PREFIX.length() - 1;
		prepareAddDataContentRule(false);
		prepareElementForString(maxLength);

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		// can't do anything about the prefix, at least check the overall length
		assertTrue(generatedValue.length() <= maxLength, "generated value too long");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringDiscreteValues() {
		prepareAddDataContentRule(false);
		prepareElementForString(null, false, "xxxx1", "xxxx2", "xxxx3");

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that at least some if the discrete values are present.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("xxxx1") + values.count("xxxx2") + values.count("xxxx3");
		assertTrue(totalDiscreteValueCount > 0, "discrete values should have appeared in here somewhere");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringFixedValues() {
		prepareAddDataContentRule(false);
		prepareElementForString(null, true, "xxxx1", "xxxx2", "xxxx3");

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that the sum of all fixed values matches the number of
		// iterations.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("xxxx1") + values.count("xxxx2") + values.count("xxxx3");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringRequestedValue() {
		final String value = "foobar42";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForString(null);

		assertEquals(value, this.valueGenerator.generateValue(this.addDataContentRule));
		assertValueDescriptorPresent(this.elementID, value, true);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringRequestedValueTooLong() {
		final int maxLength = TEST_PREFIX.length() - 1;
		final String value = "foobar42foobar";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForString(maxLength);

		// if the requested value is too long, it can either be shorted or a new value
		// can be generated, but the maximum length may not be exceeded
		assertTrue(this.valueGenerator.generateValue(this.addDataContentRule).length() <= maxLength,
				"generated value too long");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_StringRequestedNotInFixedList() {
		final String value = "foobar42foobar";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForString(null, true, "xxxx1", "xxxx2", "xxxx3");

		// the generated value may not be the requested value
		assertNotEquals(value, this.valueGenerator.generateValue(this.addDataContentRule));
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_Boolean() {
		prepareAddDataContentRule(false);
		prepareElementForBoolean();

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		assertTrue(generatedValue.equals("true") || generatedValue.equals("false"));
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_BooleanRequestedValue() {
		final String value = "false";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForBoolean();

		// to rule out random influence, generate a number of values and check they ALL
		// match the requested value
		final int NUM_TESTS = 50;
		int requestedCount = 0;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			if (generatedValue.equals(value)) {
				requestedCount++;
			}
		}

		assertEquals(NUM_TESTS, requestedCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_BooleanRequestedValueInvalidType() {
		final String value = "rhubarb"; // not a valid boolean value
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForBoolean();

		final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
		assertTrue(generatedValue.equals("true") || generatedValue.equals("false"));
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_Integer() {
		prepareAddDataContentRule(false);
		prepareElementForInteger(null, null);

		final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
		final Integer generatedInteger = Integer.parseInt(generatedString);
		assertNotNull(generatedInteger);
		assertValueDescriptorPresent(this.elementID, generatedString, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerMin() {
		final int minValue = 352416;
		prepareAddDataContentRule(false);
		prepareElementForInteger(minValue, null);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerMax() {
		final int maxValue = 625143;
		prepareAddDataContentRule(false);
		prepareElementForInteger(null, maxValue);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerMinMax() {
		final int minValue = 352416;
		final int maxValue = 625143;
		prepareAddDataContentRule(false);
		prepareElementForInteger(minValue, maxValue);

		// The randomness makes it impossible to check this decisively. All we can do is
		// generate a number of values and check they all comply.
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerDiscreteValues() {
		prepareAddDataContentRule(false);
		prepareElementForInteger(false, 42, 21, 84);

		// There's a bit of randomness at play here - so just generate a larger number
		// of values and check that at least some of the discrete values are present.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertTrue(totalDiscreteValueCount > 0, "discrete values should have appeared in here somewhere");
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerFixedValues() {
		prepareAddDataContentRule(false);
		prepareElementForInteger(true, 42, 21, 84);

		// Again generate a larger number of values and check that the occurrence of all
		// values sums up to the total number of values generated.
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerRequestedValue() {
		final String value = "123789";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForInteger(null, null);

		// Generate a larger number of values and check only the requested value occurs
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			assertEquals(value, generatedValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerRequestedValueInvalidType() {
		final String value = "foobar"; // hey, look, not an integer
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForInteger(null, null);

		// requested value must be ignored at this point
		final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
		final Integer generatedInteger = Integer.parseInt(generatedString);
		assertNotNull(generatedInteger);
		assertValueDescriptorPresent(this.elementID, generatedString, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerRequestedValueTooLow() {
		final int minValue = 100;
		final String value = "50";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForInteger(minValue, null);

		// requested value must be ignored at this point - all generated must be above
		// the minValue
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger >= minValue);
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerRequestedValueTooHigh() {
		final int maxValue = 10;
		final String value = "50";
		when(this.requestedValue.getValue()).thenReturn(value);

		prepareAddDataContentRule(true);
		prepareElementForInteger(null, maxValue);

		// requested value must be ignored at this point - all generated must be above
		// the minValue
		final int NUM_TESTS = 50;
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedString = this.valueGenerator.generateValue(this.addDataContentRule);
			final Integer generatedInteger = Integer.parseInt(generatedString);
			assertTrue(generatedInteger <= maxValue);
		}
	}

	/**
	 * Test method for
	 * {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddDataContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddDataContentRule_IntegerRequestedValueNotInFixedValues() {
		when(this.requestedValue.getValue()).thenReturn("7");
		prepareAddDataContentRule(true);
		prepareElementForInteger(true, 42, 21, 84);

		// requested value must be ignored - only fixed values may occur
		final int NUM_TESTS = 50;
		final Multiset<String> values = HashMultiset.create();
		for (int i = 0; i < NUM_TESTS; i++) {
			final String generatedValue = this.valueGenerator.generateValue(this.addDataContentRule);
			values.add(generatedValue);
		}

		final int totalDiscreteValueCount = values.count("42") + values.count("21") + values.count("84");
		assertEquals(NUM_TESTS, totalDiscreteValueCount);
	}

	/**
	 * Prepares the {@link IAddRawContentRule} mockup for use
	 * @param withRequestedValue
	 */
	void prepareAddRawContentRule(boolean withRequestedValue) {
		when(this.addRawContentRule.getID()).thenReturn(this.ruleID);
		when(this.addRawContentRule.getElementID()).thenReturn(this.elementID);
		if (withRequestedValue) {
			when(this.addRawContentRule.getRequestedValue()).thenReturn(Optional.of(this.requestedValue));
		} else {
			when(this.addRawContentRule.getRequestedValue()).thenReturn(Optional.empty());
		}
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddRawContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddRawContentRule_MixedContentGenerationModeFull() {
		lenient().when(this.request.getMixedContentGenerationMode()).thenReturn(MixedContentGenerationMode.FULL);
		prepareAddRawContentRule(false);
		final String generatedValue = this.valueGenerator.generateValue(this.addRawContentRule);
		assertTrue(Pattern.compile("<[^<>]+>").matcher(generatedValue).find(),
				String.format("generated value \"%s\" should contain a bit of HTML markup", generatedValue));
		assertTrue(generatedValue.contains(TEST_PREFIX), "generated value does not contain the prefix");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddRawContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddRawContentRule_MixedContentGenerationModeFull_RequestedValue() {
		lenient().when(this.request.getMixedContentGenerationMode()).thenReturn(MixedContentGenerationMode.FULL);
		final String value = "<script>alert(\"haX0red!\")</script>";
		when(this.requestedValue.getValue()).thenReturn(value);
		prepareAddRawContentRule(true);
		final String generatedValue = this.valueGenerator.generateValue(this.addRawContentRule);
		assertEquals(value, generatedValue);
		assertValueDescriptorPresent(this.elementID, generatedValue, true);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddRawContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddRawContentRule_MixedContentGenerationModeRestricted() {
		lenient().when(this.request.getMixedContentGenerationMode()).thenReturn(MixedContentGenerationMode.RESTRICTED);
		prepareAddRawContentRule(false);
		final String generatedValue = this.valueGenerator.generateValue(this.addRawContentRule);
		assertFalse(Pattern.compile("<[^<>]+>").matcher(generatedValue).find(),
				String.format("generated value \"%s\" should NOT contain HTML markup", generatedValue));
		assertTrue(generatedValue.contains(TEST_PREFIX), "generated value does not contain the prefix");
		assertValueDescriptorPresent(this.elementID, generatedValue, false);
	}

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueGenerator#generateValue(org.x2vc.xml.request.IAddRawContentRule)}
	 * and {@link org.x2vc.xml.value.ValueGenerator#getValueDescriptors()}.
	 */
	@Test
	void testGenerateValue_AddRawContentRule_MixedContentGenerationModeRestricted_RequestedValue() {
		lenient().when(this.request.getMixedContentGenerationMode()).thenReturn(MixedContentGenerationMode.RESTRICTED);
		final String value = "<script>alert(\"haX0red!\")</script>";
		when(this.requestedValue.getValue()).thenReturn(value);
		prepareAddRawContentRule(true);
		final String generatedValue = this.valueGenerator.generateValue(this.addRawContentRule);
		assertEquals(value, generatedValue);
		assertValueDescriptorPresent(this.elementID, generatedValue, true);
	}

	/**
	 * Checks that a value descriptor is present.
	 *
	 * @param schemaElementID
	 * @param generatedValue
	 * @param isRequested
	 */
	void assertValueDescriptorPresent(UUID schemaElementID, String generatedValue, Boolean isRequested) {
		assertTrue(this.valueGenerator.getValueDescriptors().stream()
			.anyMatch(vd -> vd.getSchemaElementID().equals(schemaElementID)
					&& vd.getGenerationRuleID().equals(this.ruleID) && vd.getValue().equals(generatedValue)
					&& vd.isRequested() == isRequested),
				"generated value missing from value descriptors");
	}

}
