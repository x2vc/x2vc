package org.x2vc.xml.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Node;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.*;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.request.*;
import org.x2vc.xml.value.IValueDescriptor;
import org.x2vc.xml.value.IValueGenerator;
import org.x2vc.xml.value.IValueGeneratorFactory;
import org.x2vc.xml.value.ValueDescriptor;

import com.google.common.collect.*;
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
	private List<IExtensionFunctionRule> extensionFunctionRules;
	@Mock
	private IRequestedValue requestedValue;

	// document generator under test
	private IDocumentGenerator documentGenerator;

	@BeforeEach
	void setUp() throws Exception {
		this.context = JAXBContext.newInstance(XMLSchema.class);
		this.unmarshaller = this.context.createUnmarshaller();

		// stylesheet
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "baz");
		lenient().when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.stylesheetInformation.getTraceNamespacePrefix()).thenReturn(this.traceNamespacePrefix);

		// schema
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar");
		this.schemaVersion = 1;
		lenient().when(this.schemaManager.getSchema(this.stylesheetURI, this.schemaVersion))
			.thenAnswer(a -> this.schema);

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
		this.extensionFunctionRules = Lists.newArrayList();
		lenient().when(this.request.getExtensionFunctionRules())
			.thenAnswer(a -> ImmutableList.copyOf(this.extensionFunctionRules));

		// document generator under test
		this.documentGenerator = new DocumentGenerator(this.schemaManager, this.stylesheetManager,
				this.valueGeneratorFactory);
	}

	@Test
	void testGenerateDocument_EmptyElementWithAttribute() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("EmptyElementWithAttribute.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IAttribute rootAttribute = rootElementReference.getElement().getAttributes().iterator().next();

		// prepare generation rules and request
		final ISetAttributeRule rootAttributeRule = new SetAttributeRule(rootAttribute);
		this.rootElementRule = AddElementRule.builder(rootElementReference).addAttributeRule(rootAttributeRule)
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
		assertXMLEquals(expectedXML, document.getDocument(), node -> traceNodeFilter(node));

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foobar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootAttribute.getID(), valDesc.getSchemaObjectID());
		assertEquals(rootAttributeRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithDataContent() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithDataContent.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddDataContentRule dataContentRule = new AddDataContentRule(rootElementReference.getID());
		this.rootElementRule = AddElementRule.builder(rootElementReference).addContentRule(dataContentRule).build();

		// prepare value generator
		when(this.valueGenerator.generateValue(dataContentRule)).thenReturn("foobar");
		this.valueDescriptors
			.add(new ValueDescriptor(rootElementReference.getID(), dataContentRule.getID(), "foobar", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root>foobar</root>
									""";
		assertXMLEquals(expectedXML, document.getDocument(), node -> traceNodeFilter(node));

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foobar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaObjectID());
		assertEquals(dataContentRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithDataContent_UnescapedEntities() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithDataContent.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddDataContentRule dataContentRule = new AddDataContentRule(rootElementReference.getID());
		this.rootElementRule = AddElementRule.builder(rootElementReference).addContentRule(dataContentRule).build();

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
		assertXMLEquals(expectedXML, document.getDocument(), node -> traceNodeFilter(node));

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foo&lt;br/&gt;bar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaObjectID());
		assertEquals(dataContentRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_ElementWithSubElement() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("ElementWithSubElement.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IElementReference subElementReference = rootElementReference.getElement().getElements().get(0);

		// prepare generation rules and request
		final AddElementRule subElementRule1 = AddElementRule.builder(subElementReference).build();
		final AddElementRule subElementRule2 = AddElementRule.builder(subElementReference).build();
		final AddElementRule subElementRule3 = AddElementRule.builder(subElementReference).build();
		this.rootElementRule = AddElementRule.builder(rootElementReference).addContentRule(subElementRule1)
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
		assertXMLEquals(expectedXML, document.getDocument(), node -> traceNodeFilter(node));
	}

	@Test
	void testGenerateDocument_VerifyTraceIDs() throws FileNotFoundException, JAXBException {
		// same as testGenerateDocument_ElementWithSubElement
		this.schema = loadSchema("ElementWithSubElement.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IElementReference subElementReference = rootElementReference.getElement().getElements().get(0);
		final AddElementRule subElementRule1 = AddElementRule.builder(subElementReference).build();
		final AddElementRule subElementRule2 = AddElementRule.builder(subElementReference).build();
		final AddElementRule subElementRule3 = AddElementRule.builder(subElementReference).build();
		this.rootElementRule = AddElementRule.builder(rootElementReference).addContentRule(subElementRule1)
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
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();

		// prepare generation rules and request
		final AddRawContentRule rawContentRule = new AddRawContentRule(rootElementReference.getID());
		this.rootElementRule = AddElementRule.builder(rootElementReference).addContentRule(rawContentRule).build();

		// prepare value generator
		when(this.valueGenerator.generateValue(rawContentRule)).thenReturn("<b>foo</b><i>bar</i>");
		this.valueDescriptors.add(new ValueDescriptor(rootElementReference.getID(), rawContentRule.getID(),
				"<b>foo</b><i>bar</i>", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		assertNotNull(document);

		final String expectedXML = """
									<root><b>foo</b><i>bar</i></root>
									""";
		assertXMLEquals(expectedXML, document.getDocument(), node -> traceNodeFilter(node));

		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor
			.getValueDescriptors("<b>foo</b><i>bar</i>");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(rootElementReference.getID(), valDesc.getSchemaObjectID());
		assertEquals(rawContentRule.getID(), valDesc.getGenerationRuleID());
	}

	@Test
	void testGenerateDocument_EmptyElementWithFunction() throws FileNotFoundException, JAXBException {
		// load schema and extract relevant objects
		this.schema = loadSchema("EmptyElementWithFunction.x2vc_schema");
		final IElementReference rootElementReference = this.schema.getRootElements().iterator().next();
		final IExtensionFunction extensionFunction = this.schema.getExtensionFunctions().iterator().next();

		// prepare generation rules and request
		this.rootElementRule = AddElementRule.builder(rootElementReference).build();
		final ExtensionFunctionRule extensionFunctionRule = new ExtensionFunctionRule(extensionFunction.getID());
		this.extensionFunctionRules.add(extensionFunctionRule);

		// prepare value generator
		final IExtensionFunctionResult functionResult = mock(IExtensionFunctionResult.class);
		// when(functionResult.getXDMValue()).thenReturn(XdmValue.makeValue("foobar"));
		when(this.valueGenerator.generateValue(extensionFunctionRule)).thenReturn(functionResult);
		this.valueDescriptors
			.add(new ValueDescriptor(extensionFunction.getID(), extensionFunctionRule.getID(), "foobar", false));

		final IXMLDocumentContainer document = this.documentGenerator.generateDocument(this.request);
		final IXMLDocumentDescriptor descriptor = document.getDocumentDescriptor();
		assertEquals(VALUE_PREFIX, descriptor.getValuePrefix());
		assertEquals(VALUE_LENGTH, descriptor.getValueLength());

		final ImmutableCollection<IExtensionFunctionResult> functionResults = descriptor.getExtensionFunctionResults();
		assertEquals(1, functionResults.size());
		assertSame(functionResult, functionResults.iterator().next());

		final Optional<ImmutableSet<IValueDescriptor>> valDescSet = descriptor.getValueDescriptors("foobar");
		assertTrue(valDescSet.isPresent());
		assertEquals(1, valDescSet.get().size());
		final IValueDescriptor valDesc = valDescSet.get().iterator().next();
		assertEquals(extensionFunction.getID(), valDesc.getSchemaObjectID());
		assertEquals(extensionFunctionRule.getID(), valDesc.getGenerationRuleID());
	}

	private IXMLSchema loadSchema(String schemaFileName) throws FileNotFoundException, JAXBException {
		final File schemaFile = new File(
				"src/test/resources/data/org.x2vc.xml.document.DocumentGenerator/" + schemaFileName);
		final XMLSchema schema = (XMLSchema) this.unmarshaller
			.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
		schema.setURI(this.schemaURI);
		return schema;
	}

	private boolean traceNodeFilter(Node node) {
		return node.getPrefix() != null && !node.getPrefix().equals(this.traceNamespacePrefix);
	}

}
