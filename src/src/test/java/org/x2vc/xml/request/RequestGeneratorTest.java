package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IDocumentValueModifier;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Files;

@ExtendWith(MockitoExtension.class)
class RequestGeneratorTest {

	@Mock
	private ISchemaManager schemaManager;

	@Mock
	private IDocumentValueModifier valueModifier;

	private JAXBContext context;
	private Unmarshaller unmarshaller;

	private IRequestGenerator requestGenerator;

	@BeforeEach
	void setUp() throws Exception {
		this.context = JAXBContext.newInstance(XMLSchema.class);
		this.unmarshaller = this.context.createUnmarshaller();
		this.requestGenerator = new RequestGenerator(this.schemaManager, 42);
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement() throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());
		assertEquals(0, rootElementRule.getContentRules().size());

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_MultipleRootElements() throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MultipleRootElements.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final Set<UUID> rootElementReferenceIDs = Set.of(UUID.fromString("5a10a2f6-a5c4-42b1-8008-d0a6f8e10775"),
				UUID.fromString("9bf54a0d-f72c-4d6e-a58b-3f7e0c6476f5"));

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertTrue(rootElementReferenceIDs.contains(rootElementRule.getElementReferenceID()));

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithRequiredAttribute()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithRequiredAttribute.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(1, rootElementRule.getAttributeRules().size());
		assertEquals(0, rootElementRule.getContentRules().size());

		final ISetAttributeRule rootAttributeRule = rootElementRule.getAttributeRules()
			.toArray(new ISetAttributeRule[0])[0];
		assertEquals(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"), rootAttributeRule.getAttributeID());

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithOptionalAttribute()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithOptionalAttribute.x2vc_schema");

		// to test an optional argument, we generate a larger number of requests and
		// check the number of times the attribute appeared - should be half of the time
		// on average

		final int NUM_REQUESTS = 20;
		int attributesFound = 0;

		for (int i = 0; i < NUM_REQUESTS; i++) {
			final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
			final IAddElementRule rootElementRule = request.getRootElementRule();
			if (rootElementRule.getAttributeRules().size() == 1) {
				final ISetAttributeRule rootAttributeRule = rootElementRule.getAttributeRules()
					.toArray(new ISetAttributeRule[0])[0];
				assertEquals(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
						rootAttributeRule.getAttributeID());
				attributesFound += 1;
			}
			assertFalse(request.getModifier().isPresent());
		}

		final int MAX_ERROR = 5; // allow for random error
		final int lowerBound = (int) Math.round(Math.floor((NUM_REQUESTS / 2) - MAX_ERROR));
		final int upperBound = (int) Math.round(Math.ceil((NUM_REQUESTS / 2) + MAX_ERROR));

		assertInRange("attributesFound", lowerBound, attributesFound, upperBound);
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithMultipleAttributes()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithMultipleAttributes.x2vc_schema");

		// the schema allows for 2, 3 or 4 attributes - again generate a larger number
		// of requests and check the number of attributes generated on average (should
		// be 3 per request)

		final int NUM_REQUESTS = 30;
		int attributesFound = 0;

		for (int i = 0; i < NUM_REQUESTS; i++) {
			final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
			final IAddElementRule rootElementRule = request.getRootElementRule();
			attributesFound += rootElementRule.getAttributeRules().size();
			assertFalse(request.getModifier().isPresent());
		}

		final int MAX_ERROR = 10; // allow for random error
		final int lowerBound = (int) Math.round(Math.floor((NUM_REQUESTS * 3) - MAX_ERROR));
		final int upperBound = (int) Math.round(Math.ceil((NUM_REQUESTS * 3) + MAX_ERROR));

		assertInRange("attributesFound", lowerBound, attributesFound, upperBound);
	}

	@Test
	void testGenerateNewRequest_SingleDataElement() throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleDataElement.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());
		assertEquals(1, rootElementRule.getContentRules().size());

		final IContentGenerationRule rootContentRule = rootElementRule.getContentRules().get(0);
		assertInstanceOf(IAddDataContentRule.class, rootContentRule);
		if (rootContentRule instanceof final IAddDataContentRule addDataContentRule) {
			assertEquals(UUID.fromString("45023ac4-9c79-4247-bbe5-36f893bd7eaa"), addDataContentRule.getElementID());
		}

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SubElement_WithArrangementAll()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SubElement_WithArrangementAll.x2vc_schema");

		final UUID textElementID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());

		// count how often each sub-element is generated
		int textElementCount = 0;
		int emptyElementCount = 0;

		for (final UnmodifiableIterator<IContentGenerationRule> iterator = rootElementRule.getContentRules()
			.iterator(); iterator.hasNext();) {
			final IContentGenerationRule rule = iterator.next();
			assertInstanceOf(IAddElementRule.class, rule);
			if (rule instanceof final IAddElementRule addElementRule) {
				final UUID elementID = addElementRule.getElementReferenceID();
				if (elementID.equals(textElementID)) {
					textElementCount++;
				} else if (elementID.equals(emptyElementID)) {
					emptyElementCount++;
				} else {
					fail("Unexpected element ID " + elementID);
				}
			}
		}

		assertEquals(1, textElementCount);
		assertInRange("emptyElementCount", 1, emptyElementCount, 20);

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SubElement_WithArrangementChoice()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SubElement_WithArrangementChoice.x2vc_schema");

		final UUID textElementReferenceID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementReferenceID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());
		assertEquals(1, rootElementRule.getContentRules().size());

		final UUID subElementReferenceID = ((IAddElementRule) rootElementRule.getContentRules().get(0))
			.getElementReferenceID();

		assertTrue(Set.of(textElementReferenceID, emptyElementReferenceID).contains(subElementReferenceID));

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SubElement_WithArrangementSequence()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SubElement_WithArrangementSequence.x2vc_schema");

		final UUID textElementReferenceID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementReferenceID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());

		// first element MUST be the text element ([1..1])
		assertEquals(textElementReferenceID,
				((IAddElementRule) rootElementRule.getContentRules().get(0)).getElementReferenceID());

		// second element MUST be the empty element ([1..1])
		assertEquals(emptyElementReferenceID,
				((IAddElementRule) rootElementRule.getContentRules().get(1)).getElementReferenceID());

		// next must be [1..10] text elements followed by [1..10] empty elements
		int textElementCount = 0;
		int emptyElementCount = 0;
		for (int i = 2; i < rootElementRule.getContentRules().size(); i++) {
			final UUID elementID = ((IAddElementRule) rootElementRule.getContentRules().get(i)).getElementReferenceID();
			if (elementID.equals(textElementReferenceID)) {
				// no empty elements may have occurred before a text element
				assertEquals(0, emptyElementCount);
				textElementCount++;
			} else if (elementID.equals(emptyElementReferenceID)) {
				// at least one text element must have occurred before the empty element
				assertNotEquals(0, textElementCount);
				emptyElementCount++;
			} else {
				fail("Unexpected element ID " + elementID);
			}
		}

		assertInRange("textElementCount", 1, textElementCount, 10);
		assertInRange("emptyElementCount", 1, emptyElementCount, 10);

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_MixedContent_WithSubElements()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MixedContent_WithSubElements.x2vc_schema");

		final UUID rootElementID = UUID.fromString("45023ac4-9c79-4247-bbe5-36f893bd7eaa");
		final UUID textElementReferenceID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementReferenceID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());

		// expect a mix of [1..10] text elements, [1..10] empty elements and any number
		// of text elements in between
		int textElementCount = 0;
		int emptyElementCount = 0;
		int rawContentCount = 0;
		for (int i = 0; i < rootElementRule.getContentRules().size(); i++) {
			final IContentGenerationRule rule = rootElementRule.getContentRules().get(i);
			if (rule instanceof final IAddElementRule addElementRule) {
				final UUID elementID = addElementRule.getElementReferenceID();
				if (elementID.equals(textElementReferenceID)) {
					textElementCount++;
				} else if (elementID.equals(emptyElementReferenceID)) {
					emptyElementCount++;
				} else {
					fail("Unexpected element ID " + elementID);
				}
			} else if (rule instanceof final IAddRawContentRule addRawContentRule) {
				final UUID elementID = addRawContentRule.getElementID();
				if (elementID.equals(rootElementID)) {
					rawContentCount++;
				} else {
					fail("Unexpected element ID " + elementID);
				}
			}
		}

		assertInRange("textElementCount", 0, textElementCount, 10);
		assertInRange("emptyElementCount", 1, emptyElementCount, 10);
		assertInRange("rawContentCount", 0, rawContentCount, 121);

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_MixedContent_WithoutSubElements()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MixedContent_WithoutSubElements.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("fe3fa767-685a-4c5a-8531-ca717a7cb72b"), rootElementRule.getElementReferenceID());
		assertEquals(0, rootElementRule.getAttributeRules().size());

		// expect a single raw data generation rule
		// expect a mix of [1..10] text elements, [1..10] empty elements and any number
		// of text elements in between
		assertEquals(1, rootElementRule.getContentRules().size());
		assertInstanceOf(IAddRawContentRule.class, rootElementRule.getContentRules().get(0));
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testModifyRequest_ForAttributeValue() throws FileNotFoundException, JAXBException {

		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithRequiredAttribute.x2vc_schema");
		// shortcut to provide schema for request construction
		lenient().when(this.schemaManager.getSchema(URI.create("file://somewhere/SampleStylesheet.xslt"), 1))
			.thenReturn(schema);

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule originalRootElementRule = originalRequest.getRootElementRule();
		final ISetAttributeRule originalRootAttributeRule = originalRootElementRule.getAttributeRules()
			.toArray(new ISetAttributeRule[0])[0];
		final UUID modifiedAttributeID = UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef");
		assertEquals(modifiedAttributeID, originalRootAttributeRule.getAttributeID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalRootAttributeRule.getID());
		lenient().when(this.valueModifier.getSchemaElementID()).thenReturn(originalRootAttributeRule.getAttributeID());
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier);

		final IAddElementRule modifiedRootElementRule = modifiedRequest.getRootElementRule();
		final ISetAttributeRule modifiedRootAttributeRule = modifiedRootElementRule.getAttributeRules()
			.toArray(new ISetAttributeRule[0])[0];

		final Optional<IRequestedValue> rv = modifiedRootAttributeRule.getRequestedValue();
		assertTrue(rv.isPresent());
		final IRequestedValue requestedValue = rv.get();
		final Optional<IDocumentModifier> mod = requestedValue.getModifier();
		assertTrue(mod.isPresent());
		final IDocumentModifier modifier = mod.get();
		assertSame(this.valueModifier, modifier);
		assertEquals("foobar", requestedValue.getValue());

		final ImmutableMultimap<UUID, IRequestedValue> rvMap = modifiedRequest.getRequestedValues();
		assertTrue(rvMap.containsKey(modifiedAttributeID));
		final ImmutableCollection<IRequestedValue> valuesFromMap = rvMap.get(modifiedAttributeID);
		assertEquals(1, valuesFromMap.size());
		assertSame(requestedValue, valuesFromMap.iterator().next());

		assertTrue(modifiedRequest.getModifier().isPresent());
		assertSame(this.valueModifier, modifiedRequest.getModifier().get());
	}

	@Test
	void testModifyRequest_ForElementContent() throws FileNotFoundException, JAXBException {

		final IXMLSchema schema = loadSchema("SingleDataElement.x2vc_schema");
		// shortcut to provide schema for request construction
		lenient().when(this.schemaManager.getSchema(URI.create("file://somewhere/SampleStylesheet.xslt"), 1))
			.thenReturn(schema);

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema);
		final IAddElementRule originalRootElementRule = originalRequest.getRootElementRule();
		final IAddDataContentRule originalRootContentRule = (IAddDataContentRule) originalRootElementRule
			.getContentRules().get(0);
		final UUID modifiedElementID = UUID.fromString("45023ac4-9c79-4247-bbe5-36f893bd7eaa");
		assertEquals(modifiedElementID, originalRootContentRule.getElementID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalRootContentRule.getID());
		lenient().when(this.valueModifier.getSchemaElementID()).thenReturn(originalRootContentRule.getElementID());
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier);

		final IAddElementRule modifiedRootElementRule = modifiedRequest.getRootElementRule();
		final IAddDataContentRule modifiedRootContentRule = (IAddDataContentRule) modifiedRootElementRule
			.getContentRules().get(0);

		final Optional<IRequestedValue> rv = modifiedRootContentRule.getRequestedValue();
		assertTrue(rv.isPresent());
		final IRequestedValue requestedValue = rv.get();
		final Optional<IDocumentModifier> mod = requestedValue.getModifier();
		assertTrue(mod.isPresent());
		final IDocumentModifier modifier = mod.get();
		assertSame(this.valueModifier, modifier);
		assertEquals("foobar", requestedValue.getValue());

		final ImmutableMultimap<UUID, IRequestedValue> rvMap = modifiedRequest.getRequestedValues();
		assertTrue(rvMap.containsKey(modifiedElementID));
		final ImmutableCollection<IRequestedValue> valuesFromMap = rvMap.get(modifiedElementID);
		assertEquals(1, valuesFromMap.size());
		assertSame(requestedValue, valuesFromMap.iterator().next());

		assertTrue(modifiedRequest.getModifier().isPresent());
		assertSame(this.valueModifier, modifiedRequest.getModifier().get());
	}

	private void assertInRange(String valueName, int lowerBound, int value, int upperBound) {
		if (!((lowerBound <= value) && (value <= upperBound))) {
			fail(String.format("Value %s not within range: %d <= %d <= %d", valueName, lowerBound, value, upperBound));
		}
	}

	private IXMLSchema loadSchema(String schemaFileName) throws FileNotFoundException, JAXBException {
		final File schemaFile = new File(
				"src/test/resources/data/org.x2vc.xml.request.RequestGenerator/" + schemaFileName);
		final XMLSchema schema = (XMLSchema) this.unmarshaller
			.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
		return schema;
	}

}
