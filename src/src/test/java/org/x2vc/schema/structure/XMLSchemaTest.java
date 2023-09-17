package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IXMLElementType.ContentType;
import org.x2vc.schema.structure.IXMLElementType.ElementArrangement;
import org.x2vc.schema.structure.XMLSchema.Builder;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

@ExtendWith(MockitoExtension.class)
class XMLSchemaTest {

	@Mock
	private IStylesheetInformation stylesheet;

	@Test
	void testIndexByID() {
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		final URI schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		final Builder schemaBuilder = new XMLSchema.Builder(stylesheetURI, schemaURI, 1);

		final XMLElementType.Builder builderRootA = new XMLElementType.Builder().withComment("root sequence")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.SEQUENCE);

		final XMLElementType elemChildStringType = new XMLElementType.Builder()
			.withComment("sequence element with string type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).withMaxLength(42).addTo(schemaBuilder);
		new XMLElementReference.Builder("stringChild", elemChildStringType)
			.withComment("reference to sequence element with string type").addTo(builderRootA);
		final XMLElementType elemChildBoolType = new XMLElementType.Builder()
			.withComment("sequence element with boolean type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.BOOLEAN).addTo(schemaBuilder);
		new XMLElementReference.Builder("boolChild", elemChildBoolType)
			.withComment("reference to sequence element with boolean type").withMinOccurrence(1).addTo(builderRootA);
		final XMLElementType elemChildIntType = new XMLElementType.Builder()
			.withComment("sequence element with integer type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.INTEGER).withMinValue(1).withMaxValue(42).addTo(schemaBuilder);
		new XMLElementReference.Builder("intChild", elemChildIntType)
			.withComment("reference to sequence element with integer type").withMaxOccurrence(42).addTo(builderRootA);

		final XMLElementType elemRootA = builderRootA.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootB = new XMLElementType.Builder().withComment("root all")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.ALL);

		final XMLElementType elemTextContent = new XMLElementType.Builder().withComment("all element with data content")
			.withContentType(ContentType.DATA).withDatatype(XMLDatatype.STRING).withUserModifiable(true)
			.addTo(schemaBuilder);
		new XMLElementReference.Builder("text", elemTextContent)
			.withComment("reference to all element with text content").addTo(builderRootB);
		final XMLElementType elemMixedContent = new XMLElementType.Builder()
			.withComment("all element with mixed content").withContentType(ContentType.MIXED).withUserModifiable(true)
			.addTo(schemaBuilder);
		new XMLElementReference.Builder("mixed", elemMixedContent)
			.withComment("reference to all element with mixed content").addTo(builderRootB);

		final XMLElementType elemRootB = builderRootB.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootC = new XMLElementType.Builder().withComment("root choice")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.CHOICE);

		final XMLAttribute attrStringChoice = new XMLAttribute.Builder("stringAttribute")
			.withComment("string attribute of choice element").withType(XMLDatatype.STRING).withMaxLength(42)
			.withUserModifiable(true).withFixedValueset(true)
			.addDiscreteValue(new XMLDiscreteValue.Builder().withComment("first choice").withStringValue("foo").build())
			.build();
		final XMLElementType elemChildStringChoice = new XMLElementType.Builder()
			.withComment("choice element with string value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrStringChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("string", elemChildStringChoice)
			.withComment("reference to choice element with string value").addTo(builderRootC);

		final XMLAttribute attrIntChoice = new XMLAttribute.Builder("intAttribute")
			.withComment("int attribute of choice element").withType(XMLDatatype.INTEGER).withMinValue(1)
			.withMaxValue(42).withUserModifiable(true).build();
		final XMLElementType elemChildIntChoice = new XMLElementType.Builder()
			.withComment("choice element with int value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrIntChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("int", elemChildIntChoice)
			.withComment("reference to choice element with int value").addTo(builderRootC);

		final XMLAttribute attrBoolChoice = new XMLAttribute.Builder("boolAttribute")
			.withComment("bool attribute of choice element").withType(XMLDatatype.BOOLEAN).build();
		final XMLElementType elemChildBoolChoice = new XMLElementType.Builder()
			.withComment("choice element with bool value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrBoolChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("bool", elemChildBoolChoice)
			.withComment("reference to choice element with bool value").addTo(builderRootC);

		final XMLElementType elemRootC = builderRootC.addTo(schemaBuilder);

		new XMLElementReference.Builder("a", elemRootA).withComment("reference to root element A").addTo(schemaBuilder);
		new XMLElementReference.Builder("b", elemRootB).withComment("reference to root element B").addTo(schemaBuilder);
		new XMLElementReference.Builder("c", elemRootC).withComment("reference to root element C").addTo(schemaBuilder);

		final XMLSchema schema = schemaBuilder.build();

		assertEquals(elemChildStringType, schema.getObjectByID(elemChildStringType.getID()));
		assertEquals(elemChildBoolType, schema.getObjectByID(elemChildBoolType.getID()));
		assertEquals(elemChildIntType, schema.getObjectByID(elemChildIntType.getID()));
		assertEquals(elemRootA, schema.getObjectByID(elemRootA.getID()));
		assertEquals(elemTextContent, schema.getObjectByID(elemTextContent.getID()));
		assertEquals(elemMixedContent, schema.getObjectByID(elemMixedContent.getID()));
		assertEquals(elemRootB, schema.getObjectByID(elemRootB.getID()));
		assertEquals(attrStringChoice, schema.getObjectByID(attrStringChoice.getID()));
		assertEquals(elemChildStringChoice, schema.getObjectByID(elemChildStringChoice.getID()));
		assertEquals(attrIntChoice, schema.getObjectByID(attrIntChoice.getID()));
		assertEquals(elemChildIntChoice, schema.getObjectByID(elemChildIntChoice.getID()));
		assertEquals(attrBoolChoice, schema.getObjectByID(attrBoolChoice.getID()));
		assertEquals(elemChildBoolChoice, schema.getObjectByID(elemChildBoolChoice.getID()));
		assertEquals(elemRootC, schema.getObjectByID(elemRootC.getID()));

	}

	/**
	 * This test checks whether the @Xml... annotations are correct and sufficient
	 * to serialize and deserialize the schema.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerializeDeserialize() throws JAXBException {
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		final URI schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		final Builder schemaBuilder = new XMLSchema.Builder(stylesheetURI, schemaURI, 1);

		final XMLElementType.Builder builderRootA = new XMLElementType.Builder().withComment("root sequence")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.SEQUENCE);

		final XMLElementType elemChildStringType = new XMLElementType.Builder()
			.withComment("sequence element with string type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).withMaxLength(42).addTo(schemaBuilder);
		new XMLElementReference.Builder("stringChild", elemChildStringType)
			.withComment("reference to sequence element with string type").addTo(builderRootA);
		final XMLElementType elemChildBoolType = new XMLElementType.Builder()
			.withComment("sequence element with boolean type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.BOOLEAN).addTo(schemaBuilder);
		new XMLElementReference.Builder("boolChild", elemChildBoolType)
			.withComment("reference to sequence element with boolean type").withMinOccurrence(1).addTo(builderRootA);
		final XMLElementType elemChildIntType = new XMLElementType.Builder()
			.withComment("sequence element with integer type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.INTEGER).withMinValue(1).withMaxValue(42).addTo(schemaBuilder);
		new XMLElementReference.Builder("intChild", elemChildIntType)
			.withComment("reference to sequence element with integer type").withMaxOccurrence(42).addTo(builderRootA);

		final XMLElementType elemRootA = builderRootA.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootB = new XMLElementType.Builder().withComment("root all")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.ALL);

		final XMLElementType elemTextContent = new XMLElementType.Builder().withComment("all element with text content")
			.withContentType(ContentType.DATA).withDatatype(XMLDatatype.STRING).withUserModifiable(true)
			.addTo(schemaBuilder);
		new XMLElementReference.Builder("text", elemTextContent)
			.withComment("reference to all element with text content").addTo(builderRootB);
		final XMLElementType elemMixedContent = new XMLElementType.Builder()
			.withComment("all element with mixed content").withContentType(ContentType.MIXED).withUserModifiable(true)
			.addTo(schemaBuilder);
		new XMLElementReference.Builder("mixed", elemMixedContent)
			.withComment("reference to all element with mixed content").addTo(builderRootB);

		final XMLElementType elemRootB = builderRootB.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootC = new XMLElementType.Builder().withComment("root choice")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.CHOICE);

		final XMLAttribute attrStringChoice = new XMLAttribute.Builder("stringAttribute")
			.withComment("string attribute of choice element").withType(XMLDatatype.STRING).withMaxLength(42)
			.withUserModifiable(true).withFixedValueset(true)
			.addDiscreteValue(new XMLDiscreteValue.Builder().withComment("first choice").withStringValue("foo").build())
			.build();
		final XMLElementType elemChildStringChoice = new XMLElementType.Builder()
			.withComment("choice element with string value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrStringChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("string", elemChildStringChoice)
			.withComment("reference to choice element with string value").addTo(builderRootC);

		final XMLAttribute attrIntChoice = new XMLAttribute.Builder("intAttribute")
			.withComment("int attribute of choice element").withType(XMLDatatype.INTEGER).withMinValue(1)
			.withMaxValue(42).withUserModifiable(true).build();
		final XMLElementType elemChildIntChoice = new XMLElementType.Builder()
			.withComment("choice element with int value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrIntChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("int", elemChildIntChoice)
			.withComment("reference to choice element with int value").addTo(builderRootC);

		final XMLAttribute attrBoolChoice = new XMLAttribute.Builder("boolAttribute")
			.withComment("bool attribute of choice element").withType(XMLDatatype.BOOLEAN).build();
		final XMLElementType elemChildBoolChoice = new XMLElementType.Builder()
			.withComment("choice element with bool value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrBoolChoice).addTo(schemaBuilder);
		new XMLElementReference.Builder("bool", elemChildBoolChoice)
			.withComment("reference to choice element with bool value").addTo(builderRootC);

		final XMLElementType elemRootC = builderRootC.addTo(schemaBuilder);

		new XMLElementReference.Builder("a", elemRootA).withComment("reference to root element A").addTo(schemaBuilder);
		new XMLElementReference.Builder("b", elemRootB).withComment("reference to root element B").addTo(schemaBuilder);
		new XMLElementReference.Builder("c", elemRootC).withComment("reference to root element C").addTo(schemaBuilder);

		final XMLSchema schema = schemaBuilder.build();

		// serialize
		final JAXBContext context = JAXBContext.newInstance(XMLSchema.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		final StringWriter writer = new StringWriter();
		marshaller.marshal(schema, writer);
		final String serializedModel = writer.toString();

		// deserialize
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final XMLSchema schemaAfterRoundtrip = (XMLSchema) unmarshaller.unmarshal(new StringReader(serializedModel));

		// and check
		assertEquals(schema, schemaAfterRoundtrip);

	}

	@Test
	void testGetObjectPaths() {

		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foo");
		final URI schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		final Builder schemaBuilder = new XMLSchema.Builder(stylesheetURI, schemaURI, 1);

		final XMLElementType.Builder builderRootA = new XMLElementType.Builder().withComment("root sequence")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.SEQUENCE);

		final XMLElementType elemChildStringType = new XMLElementType.Builder()
			.withComment("sequence element with string type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.STRING).withMaxLength(42).addTo(schemaBuilder);
		final XMLElementReference elemChildString = new XMLElementReference.Builder("stringChild",
				elemChildStringType)
			.withComment("reference to sequence element with string type").addTo(builderRootA);
		final XMLElementType elemChildBoolType = new XMLElementType.Builder()
			.withComment("sequence element with boolean type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.BOOLEAN).addTo(schemaBuilder);
		final XMLElementReference elemChildBool = new XMLElementReference.Builder("boolChild", elemChildBoolType)
			.withComment("reference to sequence element with boolean type").withMinOccurrence(1).addTo(builderRootA);
		final XMLElementType elemChildIntType = new XMLElementType.Builder()
			.withComment("sequence element with integer type").withContentType(ContentType.DATA)
			.withDatatype(XMLDatatype.INTEGER).withMinValue(1).withMaxValue(42).addTo(schemaBuilder);
		final XMLElementReference elemChildInt = new XMLElementReference.Builder("intChild", elemChildIntType)
			.withComment("reference to sequence element with integer type").withMaxOccurrence(42).addTo(builderRootA);

		final XMLElementType elemRootA = builderRootA.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootB = new XMLElementType.Builder().withComment("root all")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.ALL);

		final XMLElementType elemTextContent = new XMLElementType.Builder().withComment("all element with data content")
			.withContentType(ContentType.DATA).withDatatype(XMLDatatype.STRING).withUserModifiable(true)
			.addTo(schemaBuilder);
		final XMLElementReference elemText = new XMLElementReference.Builder("text", elemTextContent)
			.withComment("reference to all element with text content").addTo(builderRootB);
		final XMLElementType elemMixedContent = new XMLElementType.Builder()
			.withComment("all element with mixed content").withContentType(ContentType.MIXED).withUserModifiable(true)
			.addTo(schemaBuilder);
		final XMLElementReference elemMixed = new XMLElementReference.Builder("mixed", elemMixedContent)
			.withComment("reference to all element with mixed content").addTo(builderRootB);

		final XMLElementType elemRootB = builderRootB.addTo(schemaBuilder);

		final XMLElementType.Builder builderRootC = new XMLElementType.Builder().withComment("root choice")
			.withContentType(ContentType.ELEMENT).withElementArrangement(ElementArrangement.CHOICE);

		final XMLAttribute attrStringChoice = new XMLAttribute.Builder("stringAttribute")
			.withComment("string attribute of choice element").withType(XMLDatatype.STRING).withMaxLength(42)
			.withUserModifiable(true).withFixedValueset(true)
			.addDiscreteValue(new XMLDiscreteValue.Builder().withComment("first choice").withStringValue("foo").build())
			.build();
		final XMLElementType elemChildStringChoiceType = new XMLElementType.Builder()
			.withComment("choice element with string value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrStringChoice).addTo(schemaBuilder);
		final XMLElementReference elemChildStringChoice = new XMLElementReference.Builder("string",
				elemChildStringChoiceType)
			.withComment("reference to choice element with string value").addTo(builderRootC);

		final XMLAttribute attrIntChoice = new XMLAttribute.Builder("intAttribute")
			.withComment("int attribute of choice element").withType(XMLDatatype.INTEGER).withMinValue(1)
			.withMaxValue(42).withUserModifiable(true).build();
		final XMLElementType elemChildIntChoiceType = new XMLElementType.Builder()
			.withComment("choice element with int value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrIntChoice).addTo(schemaBuilder);
		final XMLElementReference elemChildIntChoice = new XMLElementReference.Builder("int", elemChildIntChoiceType)
			.withComment("reference to choice element with int value").addTo(builderRootC);

		final XMLAttribute attrBoolChoice = new XMLAttribute.Builder("boolAttribute")
			.withComment("bool attribute of choice element").withType(XMLDatatype.BOOLEAN).build();
		final XMLElementType elemChildBoolChoiceType = new XMLElementType.Builder()
			.withComment("choice element with bool value").withContentType(ContentType.ELEMENT)
			.addAttribute(attrBoolChoice).addTo(schemaBuilder);
		final XMLElementReference elemChildBoolChoice = new XMLElementReference.Builder("bool", elemChildBoolChoiceType)
			.withComment("reference to choice element with bool value").addTo(builderRootC);

		final XMLElementType elemRootC = builderRootC.addTo(schemaBuilder);

		new XMLElementReference.Builder("a", elemRootA).withComment("reference to root element A").addTo(schemaBuilder);
		new XMLElementReference.Builder("b", elemRootB).withComment("reference to root element B").addTo(schemaBuilder);
		new XMLElementReference.Builder("c", elemRootC).withComment("reference to root element C").addTo(schemaBuilder);

		final XMLSchema schema = schemaBuilder.build();

		assertEquals(Set.of("/a"), schema.getObjectPaths(elemRootA.getID()));
		assertEquals(Set.of("/a/stringChild"), schema.getObjectPaths(elemChildString.getID()));
		assertEquals(Set.of("/a/boolChild"), schema.getObjectPaths(elemChildBool.getID()));
		assertEquals(Set.of("/a/intChild"), schema.getObjectPaths(elemChildInt.getID()));
		assertEquals(Set.of("/b"), schema.getObjectPaths(elemRootB.getID()));
		assertEquals(Set.of("/b/text"), schema.getObjectPaths(elemText.getID()));
		assertEquals(Set.of("/b/mixed"), schema.getObjectPaths(elemMixed.getID()));
		assertEquals(Set.of("/c"), schema.getObjectPaths(elemRootC.getID()));
		assertEquals(Set.of("/c/string"), schema.getObjectPaths(elemChildStringChoice.getID()));
		assertEquals(Set.of("/c/string/@stringAttribute"), schema.getObjectPaths(attrStringChoice.getID()));
		assertEquals(Set.of("/c/int"), schema.getObjectPaths(elemChildIntChoice.getID()));
		assertEquals(Set.of("/c/int/@intAttribute"), schema.getObjectPaths(attrIntChoice.getID()));
		assertEquals(Set.of("/c/bool"), schema.getObjectPaths(elemChildBoolChoice.getID()));
		assertEquals(Set.of("/c/bool/@boolAttribute"), schema.getObjectPaths(attrBoolChoice.getID()));

	}

}
