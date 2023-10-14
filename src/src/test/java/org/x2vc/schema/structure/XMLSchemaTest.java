package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.schema.structure.IElementType.ElementArrangement;
import org.x2vc.schema.structure.IFunctionSignatureType.SequenceItemType;
import org.x2vc.schema.structure.XMLSchema.Builder;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import net.sf.saxon.s9api.OccurrenceIndicator;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

@ExtendWith(MockitoExtension.class)
class XMLSchemaTest {

	@Mock
	private IStylesheetInformation stylesheet;
	private URI stylesheetURI;
	private URI schemaURI;
	private int schemaVersion;
	private XMLElementType elemChildStringType;
	private XMLElementReference elemChildString;
	private XMLElementType elemChildBoolType;
	private XMLElementReference elemChildBool;
	private XMLElementType elemChildIntType;
	private XMLElementReference elemChildInt;
	private XMLElementType elemRootAType;
	private XMLElementType elemDataContent;
	private XMLElementReference elemData;
	private XMLElementType elemMixedContent;
	private XMLElementReference elemMixed;
	private XMLElementType elemRootBType;
	private XMLAttribute attrStringChoice;
	private XMLElementType elemChildStringChoiceType;
	private XMLElementReference elemChildStringChoice;
	private XMLAttribute attrIntChoice;
	private XMLElementType elemChildIntChoiceType;
	private XMLElementReference elemChildIntChoice;
	private XMLAttribute attrBoolChoice;
	private XMLElementType elemChildBoolChoiceType;
	private XMLElementReference elemChildBoolChoice;
	private XMLElementType elemRootCType;
	private XMLElementReference elemRootA;
	private XMLElementReference elemRootB;
	private XMLElementReference elemRootC;
	private ExtensionFunction functionX;
	private StylesheetParameter parameterZ;
	private XMLSchema schema;

	@BeforeEach
	private void setUp() {
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		this.schemaVersion = 1;
		final Builder schemaBuilder = XMLSchema.builder(this.stylesheetURI, this.schemaURI, this.schemaVersion);

		final XMLElementType.Builder builderRootA = XMLElementType
			.builder(UUID.fromString("612a3074-b834-415e-b2fd-a77aa4269e7e"))
			.withComment("root sequence")
			.withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.SEQUENCE);

		this.elemChildStringType = XMLElementType
			.builder(UUID.fromString("b5f02b8a-e715-41cc-8847-38d42dbeff58"))
			.withComment("sequence element with string type")
			.withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING)
			.withMaxLength(42)
			.addTo(schemaBuilder);
		this.elemChildString = XMLElementReference
			.builder(UUID.fromString("8542bb31-8ffe-49c9-9b93-c6a4fc1f842a"), "stringChild", this.elemChildStringType)
			.withComment("reference to sequence element with string type")
			.addTo(builderRootA);
		this.elemChildBoolType = XMLElementType
			.builder(UUID.fromString("58ee7ea9-9c2e-4f67-b2e1-f7d027b81fd4"))
			.withComment("sequence element with boolean type")
			.withContentType(ContentType.DATA)
			.withDataType(XMLDataType.BOOLEAN)
			.addTo(schemaBuilder);
		this.elemChildBool = XMLElementReference
			.builder(UUID.fromString("f08d14d3-5cb0-4601-9670-5de00bfcc6cc"), "boolChild", this.elemChildBoolType)
			.withComment("reference to sequence element with boolean type")
			.withMinOccurrence(1)
			.addTo(builderRootA);
		this.elemChildIntType = XMLElementType
			.builder(UUID.fromString("d152f6b3-de4e-4edf-a8f2-59c8ea663f01"))
			.withComment("sequence element with integer type")
			.withContentType(ContentType.DATA)
			.withDataType(XMLDataType.INTEGER)
			.withMinValue(1)
			.withMaxValue(42)
			.addTo(schemaBuilder);
		this.elemChildInt = XMLElementReference
			.builder(UUID.fromString("fd6ce3a7-a374-4737-bca0-565df6541d9c"), "intChild", this.elemChildIntType)
			.withComment("reference to sequence element with integer type")
			.withMaxOccurrence(42)
			.addTo(builderRootA);

		this.elemRootAType = builderRootA.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootB = XMLElementType
			.builder(UUID.fromString("93e0c827-fc97-4e94-a5f1-78766ac9211f"))
			.withComment("root all")
			.withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.ALL);

		this.elemDataContent = XMLElementType
			.builder(UUID.fromString("a68f77f0-794f-4d69-851b-7b0dd9bfd91e"))
			.withComment("all element with data (string) content")
			.withContentType(ContentType.DATA)
			.withDataType(XMLDataType.STRING)
			.withUserModifiable(true)
			.addTo(schemaBuilder);
		this.elemData = XMLElementReference
			.builder(UUID.fromString("8420cdf0-0039-4b26-9192-7f19f0559e1e"), "data", this.elemDataContent)
			.withComment("reference to all element with data (string) content")
			.addTo(builderRootB);

		this.elemMixedContent = XMLElementType
			.builder(UUID.fromString("b2d4e6c8-dcf4-4b77-975c-7ff92d219342"))
			.withComment("all element with mixed content")
			.withContentType(ContentType.MIXED)
			.withUserModifiable(true)
			.addTo(schemaBuilder);
		this.elemMixed = XMLElementReference
			.builder(UUID.fromString("eb77a740-f080-4490-ae18-e7eab989090d"), "mixed", this.elemMixedContent)
			.withComment("reference to all element with mixed content")
			.addTo(builderRootB);

		this.elemRootBType = builderRootB.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootC = XMLElementType
			.builder(UUID.fromString("31d0d851-1062-48d2-adc6-4bb4255202fb"))
			.withComment("root choice")
			.withContentType(ContentType.ELEMENT)
			.withElementArrangement(ElementArrangement.CHOICE);

		this.attrStringChoice = XMLAttribute
			.builder(UUID.fromString("60e4d4dd-19f9-4206-b510-4bdeb745e4ce"), "stringAttribute")
			.withComment("string attribute of choice element")
			.withType(XMLDataType.STRING)
			.withMaxLength(42)
			.withUserModifiable(true)
			.withFixedValueset(true)
			.addDiscreteValue(XMLDiscreteValue
				.builder(UUID.fromString("3f957473-c362-460d-a079-a92b6eb906e9"))
				.withComment("first choice")
				.withStringValue("foo")
				.build())
			.build();
		this.elemChildStringChoiceType = XMLElementType
			.builder(UUID.fromString("9ed6fdd6-e172-4fc8-946c-1e073299be97"))
			.withComment("choice element with string value")
			.withContentType(ContentType.ELEMENT)
			.addAttribute(this.attrStringChoice)
			.addTo(schemaBuilder);
		this.elemChildStringChoice = XMLElementReference
			.builder(UUID.fromString("d8f62f8e-e795-4bbf-a07f-ccecccc737c9"), "string", this.elemChildStringChoiceType)
			.withComment("reference to choice element with string value")
			.addTo(builderRootC);

		this.attrIntChoice = XMLAttribute
			.builder(UUID.fromString("2ee3c604-89c1-4d8a-a592-f7a90bcef577"), "intAttribute")
			.withComment("int attribute of choice element")
			.withType(XMLDataType.INTEGER)
			.withMinValue(1)
			.withMaxValue(42)
			.withUserModifiable(true)
			.build();
		this.elemChildIntChoiceType = XMLElementType
			.builder(UUID.fromString("c026a1ca-b616-4cf1-8fb6-14d9d0a08ddd"))
			.withComment("choice element with int value")
			.withContentType(ContentType.ELEMENT)
			.addAttribute(this.attrIntChoice)
			.addTo(schemaBuilder);
		this.elemChildIntChoice = XMLElementReference
			.builder(UUID.fromString("378229ba-2609-46f1-915a-52e7bf1d384e"), "int", this.elemChildIntChoiceType)
			.withComment("reference to choice element with int value")
			.addTo(builderRootC);

		this.attrBoolChoice = XMLAttribute
			.builder(UUID.fromString("d90323bb-1447-4cce-b7df-4269f4929a58"), "boolAttribute")
			.withComment("bool attribute of choice element")
			.withType(XMLDataType.BOOLEAN)
			.build();
		this.elemChildBoolChoiceType = XMLElementType
			.builder(UUID.fromString("e3dd89ed-3e45-4306-bdc1-348f5b618a82"))
			.withComment("choice element with bool value")
			.withContentType(ContentType.ELEMENT)
			.addAttribute(this.attrBoolChoice)
			.addTo(schemaBuilder);
		this.elemChildBoolChoice = XMLElementReference
			.builder(UUID.fromString("47482a97-fa1f-4681-8d25-020390c56b6f"), "bool", this.elemChildBoolChoiceType)
			.withComment("reference to choice element with bool value")
			.addTo(builderRootC);

		this.elemRootCType = builderRootC.addTo(schemaBuilder);

		this.elemRootA = XMLElementReference
			.builder(UUID.fromString("2779756d-67a8-499c-8b58-a5271cfb3fac"), "a", this.elemRootAType)
			.withComment("reference to root element A")
			.addTo(schemaBuilder);
		this.elemRootB = XMLElementReference
			.builder(UUID.fromString("d8135b9e-6595-4480-a156-9c4abf2f350c"), "b", this.elemRootBType)
			.withComment("reference to root element B")
			.addTo(schemaBuilder);
		this.elemRootC = XMLElementReference
			.builder(UUID.fromString("0b35a9dd-c88e-4cbe-b862-be664e1d5271"), "c", this.elemRootCType)
			.withComment("reference to root element C")
			.addTo(schemaBuilder);

		this.functionX = ExtensionFunction
			.builder(UUID.fromString("d371fefe-b19e-4f2e-975c-b2385bc94509"), "myFuncX")
			.withNamespaceURI("http://foo.bar")
			.withResultType(new FunctionSignatureType(SequenceItemType.STRING, OccurrenceIndicator.ONE))
			.withArgumentType(new FunctionSignatureType(SequenceItemType.INT, OccurrenceIndicator.ZERO_OR_MORE))
			.withArgumentType(new FunctionSignatureType(SequenceItemType.STRING, OccurrenceIndicator.ONE_OR_MORE))
			.build();
		schemaBuilder.addExtensionFunction(this.functionX);

		this.parameterZ = StylesheetParameter
			.builder(UUID.fromString("50584bd2-5260-43ca-9609-55090cf5fc3c"), "myParamZ")
			.withNamespaceURI("http://foo.bar")
			.withType(new FunctionSignatureType(SequenceItemType.STRING, OccurrenceIndicator.ONE))
			.build();
		schemaBuilder.addStylesheetParameter(this.parameterZ);

		this.schema = schemaBuilder.build();
	}

	@Test
	void testIndexByID() {
		assertEquals(this.elemChildStringType, this.schema.getObjectByID(this.elemChildStringType.getID()));
		assertEquals(this.elemChildBoolType, this.schema.getObjectByID(this.elemChildBoolType.getID()));
		assertEquals(this.elemChildIntType, this.schema.getObjectByID(this.elemChildIntType.getID()));
		assertEquals(this.elemRootAType, this.schema.getObjectByID(this.elemRootAType.getID()));
		assertEquals(this.elemDataContent, this.schema.getObjectByID(this.elemDataContent.getID()));
		assertEquals(this.elemMixedContent, this.schema.getObjectByID(this.elemMixedContent.getID()));
		assertEquals(this.elemRootBType, this.schema.getObjectByID(this.elemRootBType.getID()));
		assertEquals(this.attrStringChoice, this.schema.getObjectByID(this.attrStringChoice.getID()));
		assertEquals(this.elemChildStringChoice, this.schema.getObjectByID(this.elemChildStringChoice.getID()));
		assertEquals(this.attrIntChoice, this.schema.getObjectByID(this.attrIntChoice.getID()));
		assertEquals(this.elemChildIntChoice, this.schema.getObjectByID(this.elemChildIntChoice.getID()));
		assertEquals(this.attrBoolChoice, this.schema.getObjectByID(this.attrBoolChoice.getID()));
		assertEquals(this.elemChildBoolChoice, this.schema.getObjectByID(this.elemChildBoolChoice.getID()));
		assertEquals(this.elemRootCType, this.schema.getObjectByID(this.elemRootCType.getID()));
		assertEquals(this.elemRootA, this.schema.getObjectByID(this.elemRootA.getID()));
		assertEquals(this.elemRootB, this.schema.getObjectByID(this.elemRootB.getID()));
		assertEquals(this.elemRootC, this.schema.getObjectByID(this.elemRootC.getID()));
		assertEquals(this.functionX, this.schema.getObjectByID(this.functionX.getID()));
		assertEquals(this.parameterZ, this.schema.getObjectByID(this.parameterZ.getID()));
	}

	/**
	 * This test checks whether the @Xml... annotations are correct and sufficient to serialize and deserialize the
	 * schema.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerializeDeserialize() throws JAXBException {
		// serialize
		final JAXBContext context = JAXBContext.newInstance(XMLSchema.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		final StringWriter writer = new StringWriter();
		marshaller.marshal(this.schema, writer);
		final String serializedModel = writer.toString();

		final String expectedModel = """
										<schema stylesheetURI="memory:stylesheet/foo" schemaURI="memory:schema/bar" version="1">
										    <elementTypes>
										        <elementType contentType="ELEMENT" elementArrangement="ALL" id="93e0c827-fc97-4e94-a5f1-78766ac9211f">
										            <comment>root all</comment>
										            <subElement name="data" elementID="a68f77f0-794f-4d69-851b-7b0dd9bfd91e" minOccurrence="0" id="8420cdf0-0039-4b26-9192-7f19f0559e1e">
										                <comment>reference to all element with data (string) content</comment>
										            </subElement>
										            <subElement name="mixed" elementID="b2d4e6c8-dcf4-4b77-975c-7ff92d219342" minOccurrence="0" id="eb77a740-f080-4490-ae18-e7eab989090d">
										                <comment>reference to all element with mixed content</comment>
										            </subElement>
										        </elementType>
										        <elementType contentType="ELEMENT" id="9ed6fdd6-e172-4fc8-946c-1e073299be97">
										            <comment>choice element with string value</comment>
										            <attribute name="stringAttribute" optional="false" userModifiable="true" dataType="STRING" maxLength="42" fixedValueset="true" id="60e4d4dd-19f9-4206-b510-4bdeb745e4ce">
										                <comment>string attribute of choice element</comment>
										                <discreteValue dataType="STRING" stringValue="foo" id="3f957473-c362-460d-a079-a92b6eb906e9">
										                    <comment>first choice</comment>
										                </discreteValue>
										            </attribute>
										        </elementType>
										        <elementType contentType="DATA" userModifiable="true" dataType="STRING" id="a68f77f0-794f-4d69-851b-7b0dd9bfd91e">
										            <comment>all element with data (string) content</comment>
										        </elementType>
										        <elementType contentType="MIXED" userModifiable="true" id="b2d4e6c8-dcf4-4b77-975c-7ff92d219342">
										            <comment>all element with mixed content</comment>
										        </elementType>
										        <elementType contentType="DATA" dataType="STRING" maxLength="42" id="b5f02b8a-e715-41cc-8847-38d42dbeff58">
										            <comment>sequence element with string type</comment>
										        </elementType>
										        <elementType contentType="ELEMENT" id="c026a1ca-b616-4cf1-8fb6-14d9d0a08ddd">
										            <comment>choice element with int value</comment>
										            <attribute name="intAttribute" optional="false" userModifiable="true" dataType="INTEGER" minValue="1" maxValue="42" fixedValueset="false" id="2ee3c604-89c1-4d8a-a592-f7a90bcef577">
										                <comment>int attribute of choice element</comment>
										            </attribute>
										        </elementType>
										        <elementType contentType="DATA" dataType="INTEGER" minValue="1" maxValue="42" id="d152f6b3-de4e-4edf-a8f2-59c8ea663f01">
										            <comment>sequence element with integer type</comment>
										        </elementType>
										        <elementType contentType="ELEMENT" id="e3dd89ed-3e45-4306-bdc1-348f5b618a82">
										            <comment>choice element with bool value</comment>
										            <attribute name="boolAttribute" optional="false" userModifiable="false" dataType="BOOLEAN" fixedValueset="false" id="d90323bb-1447-4cce-b7df-4269f4929a58">
										                <comment>bool attribute of choice element</comment>
										            </attribute>
										        </elementType>
										        <elementType contentType="ELEMENT" elementArrangement="CHOICE" id="31d0d851-1062-48d2-adc6-4bb4255202fb">
										            <comment>root choice</comment>
										            <subElement name="string" elementID="9ed6fdd6-e172-4fc8-946c-1e073299be97" minOccurrence="0" id="d8f62f8e-e795-4bbf-a07f-ccecccc737c9">
										                <comment>reference to choice element with string value</comment>
										            </subElement>
										            <subElement name="int" elementID="c026a1ca-b616-4cf1-8fb6-14d9d0a08ddd" minOccurrence="0" id="378229ba-2609-46f1-915a-52e7bf1d384e">
										                <comment>reference to choice element with int value</comment>
										            </subElement>
										            <subElement name="bool" elementID="e3dd89ed-3e45-4306-bdc1-348f5b618a82" minOccurrence="0" id="47482a97-fa1f-4681-8d25-020390c56b6f">
										                <comment>reference to choice element with bool value</comment>
										            </subElement>
										        </elementType>
										        <elementType contentType="DATA" dataType="BOOLEAN" id="58ee7ea9-9c2e-4f67-b2e1-f7d027b81fd4">
										            <comment>sequence element with boolean type</comment>
										        </elementType>
										        <elementType contentType="ELEMENT" elementArrangement="SEQUENCE" id="612a3074-b834-415e-b2fd-a77aa4269e7e">
										            <comment>root sequence</comment>
										            <subElement name="stringChild" elementID="b5f02b8a-e715-41cc-8847-38d42dbeff58" minOccurrence="0" id="8542bb31-8ffe-49c9-9b93-c6a4fc1f842a">
										                <comment>reference to sequence element with string type</comment>
										            </subElement>
										            <subElement name="boolChild" elementID="58ee7ea9-9c2e-4f67-b2e1-f7d027b81fd4" minOccurrence="1" id="f08d14d3-5cb0-4601-9670-5de00bfcc6cc">
										                <comment>reference to sequence element with boolean type</comment>
										            </subElement>
										            <subElement name="intChild" elementID="d152f6b3-de4e-4edf-a8f2-59c8ea663f01" minOccurrence="0" maxOccurrence="42" id="fd6ce3a7-a374-4737-bca0-565df6541d9c">
										                <comment>reference to sequence element with integer type</comment>
										            </subElement>
										        </elementType>
										    </elementTypes>
										    <rootElements>
										        <rootElement name="b" elementID="93e0c827-fc97-4e94-a5f1-78766ac9211f" minOccurrence="0" id="d8135b9e-6595-4480-a156-9c4abf2f350c">
										            <comment>reference to root element B</comment>
										        </rootElement>
										        <rootElement name="c" elementID="31d0d851-1062-48d2-adc6-4bb4255202fb" minOccurrence="0" id="0b35a9dd-c88e-4cbe-b862-be664e1d5271">
										            <comment>reference to root element C</comment>
										        </rootElement>
										        <rootElement name="a" elementID="612a3074-b834-415e-b2fd-a77aa4269e7e" minOccurrence="0" id="2779756d-67a8-499c-8b58-a5271cfb3fac">
										            <comment>reference to root element A</comment>
										        </rootElement>
										    </rootElements>
										    <extensionFunctions>
										        <function id="d371fefe-b19e-4f2e-975c-b2385bc94509" namespaceURI="http://foo.bar" localName="myFuncX">
										            <result occurrence="ONE" type="STRING"/>
										            <arguments>
										                <argument occurrence="ZERO_OR_MORE" type="INT"/>
										                <argument occurrence="ONE_OR_MORE" type="STRING"/>
										            </arguments>
										        </function>
										    </extensionFunctions>
										    <stylesheetParameters>
										        <parameter id="50584bd2-5260-43ca-9609-55090cf5fc3c" namespaceURI="http://foo.bar" localName="myParamZ">
										            <type occurrence="ONE" type="STRING"/>
										        </parameter>
										    </stylesheetParameters>
										</schema>
										""";
		assertXMLEquals(expectedModel, serializedModel);

		// deserialize
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final XMLSchema schemaAfterRoundtrip = (XMLSchema) unmarshaller.unmarshal(new StringReader(serializedModel));

		// and check
		assertEquals(this.schema, schemaAfterRoundtrip);

	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLSchema#getObjectPaths(UUID)}.
	 */
	@Test
	void testGetObjectPaths() {
		assertEquals(Set.of("/a"), this.schema.getObjectPaths(this.elemRootAType.getID()));
		assertEquals(Set.of("/a/stringChild"), this.schema.getObjectPaths(this.elemChildString.getID()));
		assertEquals(Set.of("/a/boolChild"), this.schema.getObjectPaths(this.elemChildBool.getID()));
		assertEquals(Set.of("/a/intChild"), this.schema.getObjectPaths(this.elemChildInt.getID()));
		assertEquals(Set.of("/b"), this.schema.getObjectPaths(this.elemRootBType.getID()));
		assertEquals(Set.of("/b/data"), this.schema.getObjectPaths(this.elemData.getID()));
		assertEquals(Set.of("/b/mixed"), this.schema.getObjectPaths(this.elemMixed.getID()));
		assertEquals(Set.of("/c"), this.schema.getObjectPaths(this.elemRootCType.getID()));
		assertEquals(Set.of("/c/string"), this.schema.getObjectPaths(this.elemChildStringChoice.getID()));
		assertEquals(Set.of("/c/string/@stringAttribute"), this.schema.getObjectPaths(this.attrStringChoice.getID()));
		assertEquals(Set.of("/c/int"), this.schema.getObjectPaths(this.elemChildIntChoice.getID()));
		assertEquals(Set.of("/c/int/@intAttribute"), this.schema.getObjectPaths(this.attrIntChoice.getID()));
		assertEquals(Set.of("/c/bool"), this.schema.getObjectPaths(this.elemChildBoolChoice.getID()));
		assertEquals(Set.of("/c/bool/@boolAttribute"), this.schema.getObjectPaths(this.attrBoolChoice.getID()));
		assertEquals(Set.of("$myParamZ"), this.schema.getObjectPaths(this.parameterZ.getID()));

	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLSchema#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLSchema.class)
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

}
