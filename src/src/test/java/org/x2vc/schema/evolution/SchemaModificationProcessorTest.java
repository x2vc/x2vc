package org.x2vc.schema.evolution;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLDataType;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import com.google.common.io.Files;

@ExtendWith(MockitoExtension.class)
class SchemaModificationProcessorTest {

	private JAXBContext context;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	private SchemaModificationProcessor processor;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.context = JAXBContext.newInstance(XMLSchema.class);
		this.marshaller = this.context.createMarshaller();
		this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		this.unmarshaller = this.context.createUnmarshaller();
		this.processor = new SchemaModificationProcessor();
	}

	@ParameterizedTest
	@CsvSource({
			"MixedContent_WithoutSubElements.x2vc_schema",
			"MixedContent_WithSubElements.x2vc_schema",
			"MultipleRootElements.x2vc_schema",
			"SingleDataElement.x2vc_schema",
			"SingleEmptyElement_WithMultipleAttributes.x2vc_schema",
			"SingleEmptyElement_WithOptionalAttribute.x2vc_schema",
			"SingleEmptyElement_WithRequiredAttribute.x2vc_schema",
			"SingleEmptyElement.x2vc_schema",
			"SubElement_WithArrangementAll.x2vc_schema",
			"SubElement_WithArrangementChoice.x2vc_schema",
			"SubElement_WithArrangementSequence.x2vc_schema",
			"TypeSample.x2vc_schema",
	})
	void test_complexSchema_noChanges(String schemaFileName) throws JAXBException, IOException {
		final File schemaFile = new File(
				"src/test/resources/data/org.x2vc.schema.evolution.SchemaModificationProcessorTest/" + schemaFileName);
		final String originalSchemaSource = Files.asCharSource(schemaFile, StandardCharsets.UTF_8).read();
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of());
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();
		final String expectedSchemaSource = originalSchemaSource.replace("version=\"1\">", "version=\"2\">");
		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addAttribute_toEmptyRootElement() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="285a04e8-41cb-475a-be6c-d556155ff0b2">
				            <comment>root element</comment>
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="285a04e8-41cb-475a-be6c-d556155ff0b2" minOccurrence="1" maxOccurrence="1" id="3f711fb0-249d-458e-9477-fbad012299b8">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an attribute
		final IAddAttributeModifier modifier = AddAttributeModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("285a04e8-41cb-475a-be6c-d556155ff0b2")) // root element
			.withAttributeID(UUID.fromString("12e67549-6484-4d33-b765-0bdfa45545de"))
			.withName("attrib1")
			.withXMLDataType(XMLDataType.STRING)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="285a04e8-41cb-475a-be6c-d556155ff0b2">
				            <comment>root element</comment>
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="STRING" fixedValueset="false" id="12e67549-6484-4d33-b765-0bdfa45545de" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="285a04e8-41cb-475a-be6c-d556155ff0b2" minOccurrence="1" maxOccurrence="1" id="3f711fb0-249d-458e-9477-fbad012299b8">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addAttribute_toPopulatedRootElement() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="fffdb058-e3b8-401b-a389-bf7dc03815e0">
				            <comment>root element</comment>
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="INTEGER" fixedValueset="false" id="12e67549-6484-4d33-b765-0bdfa45545de" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="fffdb058-e3b8-401b-a389-bf7dc03815e0" minOccurrence="1" maxOccurrence="1" id="d64a171d-0b97-410f-9d59-0bb1d6a24a79">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an attribute
		final IAddAttributeModifier modifier = AddAttributeModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("fffdb058-e3b8-401b-a389-bf7dc03815e0")) // root element
			.withAttributeID(UUID.fromString("6ba05464-8bf9-48f2-999b-fad10bae4de4"))
			.withName("attrib2")
			.withXMLDataType(XMLDataType.STRING)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="fffdb058-e3b8-401b-a389-bf7dc03815e0">
				            <comment>root element</comment>
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="INTEGER" fixedValueset="false" id="12e67549-6484-4d33-b765-0bdfa45545de" />
							<attribute name="attrib2" optional="false" userModifiable="true" dataType="STRING" fixedValueset="false" id="6ba05464-8bf9-48f2-999b-fad10bae4de4" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="fffdb058-e3b8-401b-a389-bf7dc03815e0" minOccurrence="1" maxOccurrence="1" id="d64a171d-0b97-410f-9d59-0bb1d6a24a79">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addAttribute_toRootElement_withCollision() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="41fb7e95-c418-4a57-bd11-4ebb5acdd9ac">
				            <comment>root element</comment>
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="STRING" fixedValueset="false" id="3e5073d8-b1f0-4d94-9d2f-3df74c296699" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="41fb7e95-c418-4a57-bd11-4ebb5acdd9ac" minOccurrence="1" maxOccurrence="1" id="8e21e2a9-8a2c-4dfc-a2a9-f157853c6f8f">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an attribute that conflicts with the existing attribute
		final IAddAttributeModifier modifier = AddAttributeModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("41fb7e95-c418-4a57-bd11-4ebb5acdd9ac")) // root element
			.withAttributeID(UUID.fromString("ed2ef657-ce2c-4c34-9a63-6e109576cfcc"))
			.withName("attrib1")
			.withXMLDataType(XMLDataType.INTEGER)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="41fb7e95-c418-4a57-bd11-4ebb5acdd9ac">
				            <comment>root element</comment>
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="STRING" fixedValueset="false" id="3e5073d8-b1f0-4d94-9d2f-3df74c296699" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="41fb7e95-c418-4a57-bd11-4ebb5acdd9ac" minOccurrence="1" maxOccurrence="1" id="8e21e2a9-8a2c-4dfc-a2a9-f157853c6f8f">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addElement_toEmptyRootElement() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="ca3d5550-8141-4138-a72c-34a4466e9427">
				            <comment>root element</comment>
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="ca3d5550-8141-4138-a72c-34a4466e9427" minOccurrence="1" maxOccurrence="1" id="bd2e38bd-a523-4c6a-acc6-f12ff65488f0">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an element
		final IAddElementModifier modifier = AddElementModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("ca3d5550-8141-4138-a72c-34a4466e9427")) // root element
			.withName("elem1")
			.withTypeID(UUID.fromString("cbe2ec0e-ffb7-4ac3-b7f6-b6d4078667ba"))
			.withReferenceID(UUID.fromString("56073cb8-30de-4657-8f12-0eaa8b8c1a46"))
			.withContentType(ContentType.MIXED)
			.withMinOccurrence(1)
			.withMaxOccurrence(42)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="ca3d5550-8141-4138-a72c-34a4466e9427">
				            <comment>root element</comment>
					        <subElement name="elem1" elementID="cbe2ec0e-ffb7-4ac3-b7f6-b6d4078667ba" minOccurrence="1" maxOccurrence="42" id="56073cb8-30de-4657-8f12-0eaa8b8c1a46" />
				        </elementType>
				        <elementType contentType="MIXED" id="cbe2ec0e-ffb7-4ac3-b7f6-b6d4078667ba" />
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="ca3d5550-8141-4138-a72c-34a4466e9427" minOccurrence="1" maxOccurrence="1" id="bd2e38bd-a523-4c6a-acc6-f12ff65488f0">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addElement_toPopulatedRootElement() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="980e8a16-b2e6-4fe4-86fe-33cef78737c7">
				            <comment>root element</comment>
					        <subElement name="elem1" elementID="a3c010ac-9ce6-43dc-971f-e3e225041ae5" minOccurrence="1" maxOccurrence="42" id="b1aa61fb-563d-448c-8605-1b872f411c7e">
					        	<comment>existing sub-element</comment>
					        </subElement>
				        </elementType>
				        <elementType contentType="EMPTY" id="a3c010ac-9ce6-43dc-971f-e3e225041ae5" />
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="980e8a16-b2e6-4fe4-86fe-33cef78737c7" minOccurrence="1" maxOccurrence="1" id="0bd9c5e5-7d18-4037-8778-b6c649708f3b">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an element
		final IAddElementModifier modifier = AddElementModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("980e8a16-b2e6-4fe4-86fe-33cef78737c7")) // root element
			.withName("elem2")
			.withTypeID(UUID.fromString("adad5e24-e7c9-4c43-98c9-cd8d37a75c8e"))
			.withReferenceID(UUID.fromString("420d68c0-298e-42ee-b471-ed019a8d7ee9"))
			.withContentType(ContentType.MIXED)
			.withMinOccurrence(2)
			.withMaxOccurrence(4)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="980e8a16-b2e6-4fe4-86fe-33cef78737c7">
				            <comment>root element</comment>
					        <subElement name="elem2" elementID="adad5e24-e7c9-4c43-98c9-cd8d37a75c8e" minOccurrence="2" maxOccurrence="4" id="420d68c0-298e-42ee-b471-ed019a8d7ee9" />
					        <subElement name="elem1" elementID="a3c010ac-9ce6-43dc-971f-e3e225041ae5" minOccurrence="1" maxOccurrence="42" id="b1aa61fb-563d-448c-8605-1b872f411c7e">
					        	<comment>existing sub-element</comment>
					        </subElement>
				        </elementType>
				        <elementType contentType="EMPTY" id="a3c010ac-9ce6-43dc-971f-e3e225041ae5" />
				        <elementType contentType="MIXED" id="adad5e24-e7c9-4c43-98c9-cd8d37a75c8e" />
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="980e8a16-b2e6-4fe4-86fe-33cef78737c7" minOccurrence="1" maxOccurrence="1" id="0bd9c5e5-7d18-4037-8778-b6c649708f3b">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addElement_toRootElement_withCollision() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="980e8a16-b2e6-4fe4-86fe-33cef78737c7">
				            <comment>root element</comment>
					        <subElement name="elem1" elementID="a3c010ac-9ce6-43dc-971f-e3e225041ae5" minOccurrence="1" maxOccurrence="42" id="b1aa61fb-563d-448c-8605-1b872f411c7e">
					        	<comment>existing sub-element</comment>
					        </subElement>
				        </elementType>
				        <elementType contentType="EMPTY" elementArrangement="ALL" fixedValueset="false" id="a3c010ac-9ce6-43dc-971f-e3e225041ae5" />
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="980e8a16-b2e6-4fe4-86fe-33cef78737c7" minOccurrence="1" maxOccurrence="1" id="0bd9c5e5-7d18-4037-8778-b6c649708f3b">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an element
		final IAddElementModifier modifier = AddElementModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("980e8a16-b2e6-4fe4-86fe-33cef78737c7")) // root element
			.withName("elem1") // this collides with the existing element
			.withTypeID(UUID.fromString("adad5e24-e7c9-4c43-98c9-cd8d37a75c8e"))
			.withReferenceID(UUID.fromString("420d68c0-298e-42ee-b471-ed019a8d7ee9"))
			.withContentType(ContentType.MIXED)
			.withMinOccurrence(2)
			.withMaxOccurrence(4)
			.build();
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(modifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="980e8a16-b2e6-4fe4-86fe-33cef78737c7">
				            <comment>root element</comment>
					        <subElement name="elem1" elementID="a3c010ac-9ce6-43dc-971f-e3e225041ae5" minOccurrence="1" maxOccurrence="42" id="b1aa61fb-563d-448c-8605-1b872f411c7e">
					        	<comment>existing sub-element</comment>
					        </subElement>
				        </elementType>
				        <elementType contentType="EMPTY" id="a3c010ac-9ce6-43dc-971f-e3e225041ae5" />
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="980e8a16-b2e6-4fe4-86fe-33cef78737c7" minOccurrence="1" maxOccurrence="1" id="0bd9c5e5-7d18-4037-8778-b6c649708f3b">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	@Test
	void test_addElementWithAttribute_toEmptyRootElement() throws JAXBException {
		// prepare original schema
		//@formatter:off
		final String originalSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="1">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="ca3d5550-8141-4138-a72c-34a4466e9427">
				            <comment>root element</comment>
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="ca3d5550-8141-4138-a72c-34a4466e9427" minOccurrence="1" maxOccurrence="1" id="bd2e38bd-a523-4c6a-acc6-f12ff65488f0">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
				""";
		//@formatter:on
		final XMLSchema originalSchema = (XMLSchema) this.unmarshaller
			.unmarshal(new StringReader(originalSchemaSource));

		// use a modifier to add an element with a second modifier to add an attribute
		final IAddElementModifier addElementModifier = AddElementModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("ca3d5550-8141-4138-a72c-34a4466e9427"))
			.withName("elem1")
			.withTypeID(UUID.fromString("5f36c162-767c-4d51-a78a-713c447ca659"))
			.withReferenceID(UUID.fromString("a7d00111-ddb5-4f29-b6a3-383285d181cb"))
			.withContentType(ContentType.MIXED)
			.withMinOccurrence(1)
			.withMaxOccurrence(42)
			.build();
		final IAddAttributeModifier addAttributeModifier = AddAttributeModifier
			.builder(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "someSchema"), 1)
			.withElementID(UUID.fromString("5f36c162-767c-4d51-a78a-713c447ca659"))
			.withAttributeID(UUID.fromString("72d50aba-f554-40a4-9411-48e8709c8002"))
			.withName("attrib1")
			.withXMLDataType(XMLDataType.STRING)
			.build();
		addElementModifier.addAttribute(addAttributeModifier);
		final IXMLSchema modifiedSchema = this.processor.modifySchema(originalSchema, Set.of(addElementModifier));

		// serialize the schema and compare with the expected version
		//@formatter:off
		final String expectedSchemaSource
			= """
				<schema stylesheetURI="file://somewhere/SampleStylesheet.xslt" schemaURI="memory:schema/someSchema" version="2">
				    <elementTypes>
				        <elementType contentType="ELEMENT" elementArrangement="ALL" id="ca3d5550-8141-4138-a72c-34a4466e9427">
				            <comment>root element</comment>
					        <subElement name="elem1" elementID="5f36c162-767c-4d51-a78a-713c447ca659" minOccurrence="1" maxOccurrence="42" id="a7d00111-ddb5-4f29-b6a3-383285d181cb" />
				        </elementType>
				        <elementType contentType="MIXED" id="5f36c162-767c-4d51-a78a-713c447ca659">
							<attribute name="attrib1" optional="false" userModifiable="true" dataType="STRING" fixedValueset="false" id="72d50aba-f554-40a4-9411-48e8709c8002" />
				        </elementType>
				    </elementTypes>
				    <rootElements>
				        <rootElement name="root" elementID="ca3d5550-8141-4138-a72c-34a4466e9427" minOccurrence="1" maxOccurrence="1" id="bd2e38bd-a523-4c6a-acc6-f12ff65488f0">
				            <comment>reference to root element</comment>
				        </rootElement>
				    </rootElements>
				</schema>
			""";
		// @formatter:on
		final StringWriter actualSchemaSourceWriter = new StringWriter();
		this.marshaller.marshal(modifiedSchema, actualSchemaSourceWriter);
		final String actualSchemaSource = actualSchemaSourceWriter.toString();

		compareXML(expectedSchemaSource, actualSchemaSource);
	}

	private void compareXML(String expected, String actual) {
		assertNotNull(actual);
		final Diff d = DiffBuilder.compare(Input.fromString(expected))
			.ignoreWhitespace()
			.withTest(actual)
			.build();
		if (d.hasDifferences()) {
			assertionFailure()
				.message(d.fullDescription())
				.expected(expected)
				.actual(actual)
				.buildAndThrow();
		}
	}

}
