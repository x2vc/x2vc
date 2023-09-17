package org.x2vc.xml.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.URIHandling;
import org.x2vc.utilities.URIHandling.ObjectType;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IValueDescriptor;
import org.x2vc.xml.value.IValueGenerator;
import org.x2vc.xml.value.IValueGeneratorFactory;
import org.x2vc.xml.value.ValueDescriptor;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratorTest {

	private static final String VALUE_PREFIX = "zuio";
	private static final Integer VALUE_LENGTH = 8;
	private JAXBContext context;
	private Unmarshaller unmarshaller;

	// schema
	@Mock
	private ISchemaManager schemaManager;
	private URI schemaURI;
	private int schemaVersion;
	private IXMLSchema schema;

	// stylesheet
	@Mock
	private IStylesheetManager stylesheetManager;
	private URI stylesheetURI;
	@Mock
	private IStylesheetInformation stylesheetInformation;
	private String traceNamespacePrefix = "trace0";

	// value generator
	@Mock
	private IValueGeneratorFactory valueGeneratorFactory;
	@Mock
	private IValueGenerator valueGenerator;
	private HashSet<Object> valueDescriptors;

	// request and associated objects
	@Mock
	private IDocumentRequest request;
	private IAddElementRule rootElementRule;
	@Mock
	private IRequestedValue requestedValue;

	// document generator under test
	private IDocumentGenerator documentGenerator;

	@BeforeEach
	void setUp() throws Exception {
		this.context = JAXBContext.newInstance(XMLSchema.class);
		this.unmarshaller = this.context.createUnmarshaller();

		// schema
		this.schemaURI = URIHandling.makeMemoryURI(ObjectType.SCHEMA, "bar");
		this.schemaVersion = 1;
		lenient().when(this.schemaManager.getSchema(this.schemaURI, this.schemaVersion)).thenAnswer(a -> this.schema);

		// stylesheet
		this.stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "baz");
		lenient().when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.stylesheetInformation.getTraceNamespacePrefix()).thenReturn(this.traceNamespacePrefix);

		// value generator
		lenient().when(this.valueGeneratorFactory.createValueGenerator(this.request)).thenReturn(this.valueGenerator);
		lenient().when(this.valueGenerator.getValuePrefix()).thenReturn(VALUE_PREFIX);
		lenient().when(this.valueGenerator.getValueLength()).thenReturn(VALUE_LENGTH);
		this.valueDescriptors = Sets.newHashSet();
		lenient().when(this.valueGenerator.getValueDescriptors())
			.thenAnswer(a -> ImmutableSet.copyOf(this.valueDescriptors));

		// request
		lenient().when(this.request.getSchemaURI()).thenReturn(this.schemaURI);
		lenient().when(this.request.getSchemaVersion()).thenReturn(this.schemaVersion);
		lenient().when(this.request.getStylesheeURI()).thenReturn(this.stylesheetURI);
		lenient().when(this.request.getRootElementRule()).thenAnswer(a -> this.rootElementRule);

		// document generator under test
		this.documentGenerator = new DocumentGenerator(this.schemaManager, this.stylesheetManager,
				this.valueGeneratorFactory);
	}

	@Test
	void testGenerateDocument_EmptyElementWithAttribute() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("EmptyElementWithAttribute.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IXMLAttribute rootAttribute = rootElementReference.getElement().getAttributes().iterator().next();

		// prepare generation rules and request
		final ISetAttributeRule rootAttributeRule = new SetAttributeRule(rootAttribute);
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addAttributeRule(rootAttributeRule)
			.build();

		// prepare value generator
		when(this.valueGenerator.generateValue(rootAttributeRule)).thenReturn("foobar");
		this.valueDescriptors
			.add(new ValueDescriptor(rootAttribute.getID(), rootAttributeRule.getID(), "foobar", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root someAttribute="foobar"/>
									""";
		compareXML(expectedXML, document.getDocument());

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foobar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootAttribute.getID(), valDesc.getSchemaElementID());
		assertEquals(rootAttributeRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithDataContent() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithDataContent.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddDataContentRule dataContentRule = new AddDataContentRule(rootElementReference.getID());
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addContentRule(dataContentRule).build();

		// prepare value generator
		when(this.valueGenerator.generateValue(dataContentRule)).thenReturn("foobar");
		this.valueDescriptors
			.add(new ValueDescriptor(rootElementReference.getID(), dataContentRule.getID(), "foobar", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root>foobar</root>
									""";
		compareXML(expectedXML, document.getDocument());

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foobar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaElementID());
		assertEquals(dataContentRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithDataContent_UnescapedEntities() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithDataContent.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddDataContentRule dataContentRule = new AddDataContentRule(rootElementReference.getID());
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addContentRule(dataContentRule).build();

		// prepare value generator
		when(this.valueGenerator.generateValue(dataContentRule)).thenReturn("foo&lt;br/&gt;bar");
		this.valueDescriptors
			.add(new ValueDescriptor(rootElementReference.getID(), dataContentRule.getID(), "foo&lt;br/&gt;bar",
					false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root>foo&lt;br/&gt;bar</root>
									""";
		compareXML(expectedXML, document.getDocument());

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foo&lt;br/&gt;bar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaElementID());
		assertEquals(dataContentRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithSubElement() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithSubElement.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IXMLElementReference subElementReference = rootElementReference.getElement().getElements().get(0);

		// prepare generation rules and request
		final AddElementRule subElementRule1 = new AddElementRule.Builder(subElementReference).build();
		final AddElementRule subElementRule2 = new AddElementRule.Builder(subElementReference).build();
		final AddElementRule subElementRule3 = new AddElementRule.Builder(subElementReference).build();
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addContentRule(subElementRule1)
			.addContentRule(subElementRule2).addContentRule(subElementRule3).build();

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root>
										<item/>
										<item/>
										<item/>
									</root>
									""";
		compareXML(expectedXML, document.getDocument());
	}

	@Test
	void testGenerateDocument_VerifyTraceIDs() throws FileNotFoundException, JAXBException {
		// same as testGenerateDocument_ElementWithSubElement
		this.schema = loadSchema("ElementWithSubElement.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IXMLElementReference subElementReference = rootElementReference.getElement().getElements().get(0);
		final AddElementRule subElementRule1 = new AddElementRule.Builder(subElementReference).build();
		final AddElementRule subElementRule2 = new AddElementRule.Builder(subElementReference).build();
		final AddElementRule subElementRule3 = new AddElementRule.Builder(subElementReference).build();
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addContentRule(subElementRule1)
			.addContentRule(subElementRule2).addContentRule(subElementRule3).build();
		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);

		// extract the trace IDs using regex
		final String[] matches = Pattern.compile("trace0:elementID=\"([0123456789abcdef-]+)\"")
			.matcher(document.getDocument())
			.results()
			.map(result -> result.group(1))
			.toArray(String[]::new);
		assertEquals(4, matches.length);

		final Map<UUID, UUID> idMap = document.getDocumentDescriptor().getTraceIDToRuleIDMap();

		assertEquals(this.rootElementRule.getID(), idMap.get(UUID.fromString(matches[0])));
		assertEquals(subElementRule1.getID(), idMap.get(UUID.fromString(matches[1])));
		assertEquals(subElementRule2.getID(), idMap.get(UUID.fromString(matches[2])));
		assertEquals(subElementRule3.getID(), idMap.get(UUID.fromString(matches[3])));
	}

	@Test
	void testGenerateDocument_ElementWithRawContent() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithRawContent.x2vc_schema");
		final IXMLElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddRawContentRule rawContentRule = new AddRawContentRule(rootElementReference.getID());
		this.rootElementRule = new AddElementRule.Builder(rootElementReference).addContentRule(rawContentRule).build();

		// prepare value generator
		when(this.valueGenerator.generateValue(rawContentRule)).thenReturn("<b>foo</b><i>bar</i>");
		this.valueDescriptors.add(new ValueDescriptor(rootElementReference.getID(), rawContentRule.getID(),
				"<b>foo</b><i>bar</i>", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root><b>foo</b><i>bar</i></root>
									""";
		compareXML(expectedXML, document.getDocument());

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor
			.getValueDescriptors("<b>foo</b><i>bar</i>");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaElementID());
		assertEquals(rawContentRule.getID(), valDesc.getGenerationRuleID());
	}

	private IXMLSchema loadSchema(String schemaFileName) throws FileNotFoundException, JAXBException {
		final File schemaFile = new File(
				"src/test/resources/data/org.x2vc.xml.document.DocumentGenerator/" + schemaFileName);
		final XMLSchema schema = (XMLSchema) this.unmarshaller
			.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
		schema.setURI(this.schemaURI);
		return schema;
	}

	private void compareXML(String expected, String actual) {
		assertNotNull(actual);
		final Diff d = DiffBuilder.compare(Input.fromString(expected))
			.ignoreWhitespace()
			.withTest(actual)
			.withNodeFilter(node -> node.getPrefix() != null && !node.getPrefix().equals(this.traceNamespacePrefix))
			.build();
		assertFalse(d.hasDifferences(), d.fullDescription());
	}

}
