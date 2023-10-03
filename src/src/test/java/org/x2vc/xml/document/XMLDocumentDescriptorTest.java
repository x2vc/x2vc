package org.x2vc.xml.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.analysis.rules.AnalyzerRulePayload;
import org.x2vc.xml.value.IValueDescriptor;
import org.x2vc.xml.value.ValueDescriptor;

import com.google.common.collect.ImmutableSet;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class XMLDocumentDescriptorTest {

	@Mock
	private IDocumentModifier modifier;

	@Mock
	private IValueDescriptor valueDescriptor;

	@Test
	void testMinimal() {
		final XMLDocumentDescriptor descriptor = XMLDocumentDescriptor.builder("abcd", 8).build();
		assertEquals("abcd", descriptor.getValuePrefix());
		assertEquals(8, descriptor.getValueLength());
		assertFalse(descriptor.getModifier().isPresent());
	}

	@Test
	void testModifier() {
		final XMLDocumentDescriptor descriptor = XMLDocumentDescriptor.builder("abcd", 8)
			.withModifier(this.modifier).build();
		final Optional<IDocumentModifier> mod = descriptor.getModifier();
		assertTrue(mod.isPresent());
		assertSame(this.modifier, mod.get());
	}

	@ParameterizedTest
	@CsvSource({
		"qwer,qwer",
		"abcd0000,abcd0000",
		"abcd0000 Foo Bar,abcd0000",
		"Foo abcd0000 Bar,abcd0000",
		"abcd0000,abcd0000 Foo Bar",
		"abcd0000,Foo abcd0000 Bar",
		"abcd0000 Foo Bar,abcd0000 Foo Bar",
		"Foo abcd0000 Bar,abcd0000 Foo Bar",
		"abcd0000 Foo Bar,Foo abcd0000 Bar",
		"Foo abcd0000 Bar,Foo abcd0000 Bar",
		"abcd0000 qwer tzui ghjk abcd,abcd0000 qwer abcd"
	})
	void testValueDescriptor_Match(String testValue, String testQuery) {
		when(this.valueDescriptor.getValue()).thenReturn(testValue);
		final XMLDocumentDescriptor descriptor = XMLDocumentDescriptor.builder("abcd", 8)
				.addValueDescriptor(this.valueDescriptor).build();
		final Optional<ImmutableSet<IValueDescriptor>> vd = descriptor.getValueDescriptors(testQuery);
		assertTrue(vd.isPresent());
		assertEquals(1, vd.get().size());
		assertSame(this.valueDescriptor, vd.get().iterator().next());
	}

	@ParameterizedTest
	@CsvSource({
		"qwer,yxcv",
		"abcd0000,abcd1234",
		"abcd0000 Foo Bar,abcd1234",
		"Foo abcd0000 Bar,abcd1234",
		"abcd0000,abcd1234 Foo Bar",
		"abcd0000,Foo abcd1234 Bar",
		"abcd0000 Foo Bar,abcd1234 Foo Bar",
		"Foo abcd0000 Bar,abcd1234 Foo Bar",
		"abcd0000 Foo Bar,Foo abcd1234 Bar",
		"Foo abcd0000 Bar,Foo abcd1234 Bar"
	})
	void testValueDescriptor_NoMatch(String testValue, String testQuery) {
		when(this.valueDescriptor.getValue()).thenReturn(testValue);
		final XMLDocumentDescriptor descriptor = XMLDocumentDescriptor.builder("abcd", 8)
				.addValueDescriptor(this.valueDescriptor).build();
		final Optional<ImmutableSet<IValueDescriptor>> vd = descriptor.getValueDescriptors(testQuery);
		assertFalse(vd.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.xml.document.XMLDocumentDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLDocumentDescriptor.class).verify();
	}

	/**
	 * Test to determine whether a document request can be serialized. This test requires "real" objects instead of
	 * mocks.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerializer() throws JAXBException {
		final IValueDescriptor realValueDescriptor = new ValueDescriptor(
				UUID.fromString("d3243633-2dec-49e8-bf46-077004a50a01"), // schemaObjectID
				UUID.fromString("34aa5c43-9932-40f7-997c-c03354278320"), // generationRuleID,
				"valueDescriptorValue01", // value,
				true // requested
		);
		final IExtensionFunctionResult realStringFunctionResult = new StringExtensionFunctionResult(
				UUID.fromString("758ab706-d31a-4802-b5ef-0ad6d7441d9e"), // functionID
				"functionResult01" // result
		);
		final IExtensionFunctionResult realIntegerFunctionResult = new IntegerExtensionFunctionResult(
				UUID.fromString("dfa68645-20af-4d83-b226-249e486127c9"), // functionID
				42 // result
		);
		final IExtensionFunctionResult realBooleanFunctionResult = new BooleanExtensionFunctionResult(
				UUID.fromString("e1d72795-4bf9-409e-a9d6-80d7e68286fa"), // functionID
				false // result
		);
		final IModifierPayload realPayload = AnalyzerRulePayload.builder()
			.withAttributeName("attributeName01")
			.withElementName("elementName01")
			.withElementSelector("elementSelector01")
			.withInjectedValue("injectedValue01")
			.withSchemaElementID(UUID.fromString("b22ea52c-ca5c-4dce-a702-44d9ccaaf701"))
			.build();
		final IDocumentModifier realModifier = DocumentValueModifier.builder(
				UUID.fromString("29037fbb-5516-438c-9d0a-cf051ee856fa"), // schemaObjectID
				UUID.fromString("b22ea52c-ca5c-4dce-a702-44d9ccaaf701")) // generationRuleID
			.withAnalyzerRuleID("ruleID01")
			.withOriginalValue("modifierOriginalValue01")
			.withReplacementValue("modifierReplacementValue01")
			.withPayload(realPayload)
			.build();
		final Map<UUID, UUID> realTraceIDToRuleIDMap = new HashMap<UUID, UUID>();
		realTraceIDToRuleIDMap.put(
				UUID.fromString("54770902-51e9-4b74-b199-75e4016015d2"),
				UUID.fromString("94f122f9-c4b9-443a-b640-045f8d0f95c0"));
		realTraceIDToRuleIDMap.put(
				UUID.fromString("d8ddfc5f-6e6c-4fe8-9609-382dbb31cec7"),
				UUID.fromString("a8e09856-8598-427e-af33-a106494568b8"));
		final IXMLDocumentDescriptor descriptor = XMLDocumentDescriptor.builder("abcd", 8)
			.addValueDescriptor(realValueDescriptor)
			.addExtensionFunctionResult(realStringFunctionResult)
			.addExtensionFunctionResult(realIntegerFunctionResult)
			.addExtensionFunctionResult(realBooleanFunctionResult)
			.withModifier(realModifier)
			.withTraceIDToRuleIDMap(realTraceIDToRuleIDMap)
			.build();

		final JAXBContext context = JAXBContext.newInstance(XMLDocumentDescriptor.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		final StringWriter writer = new StringWriter();
		marshaller.marshal(descriptor, writer);

		final String expected = """
								<descriptor valuePrefix="abcd" valueLength="8">
								    <valueDescriptors>
								        <valueDescriptor schemaObjectID="d3243633-2dec-49e8-bf46-077004a50a01" generationRuleID="34aa5c43-9932-40f7-997c-c03354278320" requested="true">
								            <value>valueDescriptorValue01</value>
								        </valueDescriptor>
								    </valueDescriptors>
								    <documentValueModifier schemaObjectID="29037fbb-5516-438c-9d0a-cf051ee856fa" generationRuleID="b22ea52c-ca5c-4dce-a702-44d9ccaaf701" analyzerRuleID="ruleID01">
								        <analyzerRulePayload schemaElementID="b22ea52c-ca5c-4dce-a702-44d9ccaaf701" elementName="elementName01" attributeName="attributeName01">
								            <injectedValue>injectedValue01</injectedValue>
								            <elementSelector>elementSelector01</elementSelector>
								        </analyzerRulePayload>
								        <originalValue>modifierOriginalValue01</originalValue>
								        <replacementValue>modifierReplacementValue01</replacementValue>
								    </documentValueModifier>
								    <traceRuleMapping>
								        <item traceID="d8ddfc5f-6e6c-4fe8-9609-382dbb31cec7" ruleID="a8e09856-8598-427e-af33-a106494568b8"/>
								        <item traceID="54770902-51e9-4b74-b199-75e4016015d2" ruleID="94f122f9-c4b9-443a-b640-045f8d0f95c0"/>
								    </traceRuleMapping>
								    <functionResults>
								        <stringResult functionID="758ab706-d31a-4802-b5ef-0ad6d7441d9e">
								            <result>functionResult01</result>
								        </stringResult>
								        <integerResult functionID="dfa68645-20af-4d83-b226-249e486127c9">
								            <result>42</result>
								        </integerResult>
								        <booleanResult functionID="e1d72795-4bf9-409e-a9d6-80d7e68286fa">
								            <result>false</result>
								        </booleanResult>
								    </functionResults>
								</descriptor>
								""";
		assertXMLEquals(expected, writer.toString());
	}
}
