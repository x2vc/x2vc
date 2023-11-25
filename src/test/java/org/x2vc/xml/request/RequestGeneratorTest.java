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
package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IDocumentValueModifier;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Files;

@ExtendWith(MockitoExtension.class)
class RequestGeneratorTest {

	private static final int MAX_ELEMENTS = 42;

	@Mock
	private Random rng;

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
		this.requestGenerator = new RequestGenerator(this.rng, this.schemaManager, MAX_ELEMENTS);
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement() throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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
	void testGenerateNewRequest_MultipleRootElements_ElementA()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MultipleRootElements.x2vc_schema");

		when(this.rng.nextInt(0, 2)).thenReturn(0);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("5a10a2f6-a5c4-42b1-8008-d0a6f8e10775"), rootElementRule.getElementReferenceID());

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_MultipleRootElements_ElementB()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MultipleRootElements.x2vc_schema");

		when(this.rng.nextInt(0, 2)).thenReturn(1);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		assertEquals(new URI("memory:schema/bar"), request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(0, request.getRequestedValues().size()); // no requested values for new requests

		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(UUID.fromString("9bf54a0d-f72c-4d6e-a58b-3f7e0c6476f5"), rootElementRule.getElementReferenceID());

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithRequiredAttribute()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithRequiredAttribute.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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
	void testGenerateNewRequest_SingleEmptyElement_WithOptionalAttribute_Selected()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithOptionalAttribute.x2vc_schema");

		when(this.rng.nextInt(2)).thenReturn(1);
		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(1, rootElementRule.getAttributeRules().size());
		final ISetAttributeRule rootAttributeRule = rootElementRule.getAttributeRules()
			.toArray(new ISetAttributeRule[0])[0];
		assertEquals(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
				rootAttributeRule.getAttributeID());
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithOptionalAttribute_NotSelected()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithOptionalAttribute.x2vc_schema");

		when(this.rng.nextInt(2)).thenReturn(0);
		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		assertEquals(0, rootElementRule.getAttributeRules().size());
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithMultipleAttributes_00()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithMultipleAttributes.x2vc_schema");

		// intAttribute optional="false" id="73a9b784-7a61-48c4-8110-9855cef81cef" -- must exist
		// stringAttribute optional="false" id="68a174ff-ef1d-48ae-aa09-accf2566f390" -- must exist
		// intAttributeOptional optional="true" id="431c772d-4cc9-4074-a35d-509f2af92f07" -- NOT selected
		// stringAttributeOptional optional="true" id="141c4641-47a7-4ff1-9b2c-859033fffd34" -- NOT selected

		when(this.rng.nextInt(2)).thenReturn(0, 0);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		final ImmutableSet<ISetAttributeRule> attributeRules = rootElementRule.getAttributeRules();
		assertEquals(
				Set.of(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
						UUID.fromString("68a174ff-ef1d-48ae-aa09-accf2566f390")),
				attributeRules.stream().map(rule -> rule.getAttributeID()).collect(Collectors.toSet()));
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithMultipleAttributes_01()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithMultipleAttributes.x2vc_schema");

		// intAttribute optional="false" id="73a9b784-7a61-48c4-8110-9855cef81cef" -- must exist
		// stringAttribute optional="false" id="68a174ff-ef1d-48ae-aa09-accf2566f390" -- must exist
		// intAttributeOptional optional="true" id="431c772d-4cc9-4074-a35d-509f2af92f07" -- NOT selected
		// stringAttributeOptional optional="true" id="141c4641-47a7-4ff1-9b2c-859033fffd34" -- selected

		when(this.rng.nextInt(2)).thenReturn(0, 1);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		final ImmutableSet<ISetAttributeRule> attributeRules = rootElementRule.getAttributeRules();
		assertEquals(
				Set.of(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
						UUID.fromString("68a174ff-ef1d-48ae-aa09-accf2566f390"),
						UUID.fromString("141c4641-47a7-4ff1-9b2c-859033fffd34")),
				attributeRules.stream().map(rule -> rule.getAttributeID()).collect(Collectors.toSet()));
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithMultipleAttributes_10()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithMultipleAttributes.x2vc_schema");

		// intAttribute optional="false" id="73a9b784-7a61-48c4-8110-9855cef81cef" -- must exist
		// stringAttribute optional="false" id="68a174ff-ef1d-48ae-aa09-accf2566f390" -- must exist
		// intAttributeOptional optional="true" id="431c772d-4cc9-4074-a35d-509f2af92f07" -- selected
		// stringAttributeOptional optional="true" id="141c4641-47a7-4ff1-9b2c-859033fffd34" -- NOT selected

		when(this.rng.nextInt(2)).thenReturn(1, 0);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		final ImmutableSet<ISetAttributeRule> attributeRules = rootElementRule.getAttributeRules();
		assertEquals(
				Set.of(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
						UUID.fromString("68a174ff-ef1d-48ae-aa09-accf2566f390"),
						UUID.fromString("431c772d-4cc9-4074-a35d-509f2af92f07")),
				attributeRules.stream().map(rule -> rule.getAttributeID()).collect(Collectors.toSet()));
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithMultipleAttributes_11()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithMultipleAttributes.x2vc_schema");

		// intAttribute optional="false" id="73a9b784-7a61-48c4-8110-9855cef81cef" -- must exist
		// stringAttribute optional="false" id="68a174ff-ef1d-48ae-aa09-accf2566f390" -- must exist
		// intAttributeOptional optional="true" id="431c772d-4cc9-4074-a35d-509f2af92f07" -- selected
		// stringAttributeOptional optional="true" id="141c4641-47a7-4ff1-9b2c-859033fffd34" -- selected

		when(this.rng.nextInt(2)).thenReturn(1, 1);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule rootElementRule = request.getRootElementRule();
		final ImmutableSet<ISetAttributeRule> attributeRules = rootElementRule.getAttributeRules();
		assertEquals(
				Set.of(UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef"),
						UUID.fromString("68a174ff-ef1d-48ae-aa09-accf2566f390"),
						UUID.fromString("431c772d-4cc9-4074-a35d-509f2af92f07"),
						UUID.fromString("141c4641-47a7-4ff1-9b2c-859033fffd34")),
				attributeRules.stream().map(rule -> rule.getAttributeID()).collect(Collectors.toSet()));
		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleDataElement() throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleDataElement.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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

		final int numEmptyElements = 7;
		when(this.rng.nextInt(1, 21)).thenReturn(numEmptyElements);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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
		assertEquals(numEmptyElements, emptyElementCount);

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_SubElement_WithArrangementChoice()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SubElement_WithArrangementChoice.x2vc_schema");

		final UUID textElementReferenceID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementReferenceID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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

		final int numTextElements = 6;
		final int numEmptyElements = 4;
		when(this.rng.nextInt(1, 11)).thenReturn(numTextElements, numEmptyElements);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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

		assertEquals(numTextElements, textElementCount);
		assertEquals(numEmptyElements, emptyElementCount);

		assertFalse(request.getModifier().isPresent());
	}

	@ParameterizedTest
	@CsvSource({
			"5, 0, '1, 0, 1, 0, 1, 0, 1, 0, 1, 0'",
			"6, 1, '0, 1, 0, 1, 0, 1, 0, 1, 0, 1'"
	})
	void testGenerateNewRequest_MixedContent_WithSubElements(int numRawContent, Integer firstRawSelection,
			String remainingRawSelectionText)
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MixedContent_WithSubElements.x2vc_schema");

		final UUID rootElementID = UUID.fromString("45023ac4-9c79-4247-bbe5-36f893bd7eaa");
		final UUID textElementReferenceID = UUID.fromString("c90f6614-362f-4c50-a040-ebeb8f9eb113");
		final UUID emptyElementReferenceID = UUID.fromString("dd7fa303-9fe6-49fb-8257-66608a7e434f");

		final int numTextElements = 6;
		final int numEmptyElements = 4;
		when(this.rng.nextInt(1, 11)).thenReturn(numTextElements, numEmptyElements);
		// set raw content rules to be inserted
		final Integer[] remainingRawSelection = Arrays.stream(remainingRawSelectionText.split("\\s*,\\s*"))
			.map(s -> Integer.parseInt(s))
			.toArray(Integer[]::new);
		when(this.rng.nextInt(0, 2)).thenReturn(firstRawSelection, remainingRawSelection);

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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

		assertEquals(numTextElements, textElementCount);
		assertEquals(numEmptyElements, emptyElementCount);
		assertEquals(numRawContent, rawContentCount);

		assertFalse(request.getModifier().isPresent());
	}

	@Test
	void testGenerateNewRequest_MixedContent_WithoutSubElements()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("MixedContent_WithoutSubElements.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
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
	void testGenerateNewRequest_SingleEmptyElement_WithExtensionFunctons()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithExtensionFunctions.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);

		final ImmutableCollection<IExtensionFunctionRule> functionResults = request.getExtensionFunctionRules();
		assertEquals(1, functionResults.size());
		final IExtensionFunctionRule result = functionResults.iterator().next();
		assertEquals(UUID.fromString("b2104652-4db3-4801-9426-f8876fce19b7"), result.getFunctionID());
		assertFalse(result.getRequestedValue().isPresent());
	}

	@Test
	void testGenerateNewRequest_SingleEmptyElement_WithStylesheetParameters()
			throws URISyntaxException, FileNotFoundException, JAXBException {
		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithStylesheetParameters.x2vc_schema");

		final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);

		final ImmutableCollection<IStylesheetParameterRule> parameterResults = request.getStylesheetParameterRules();
		assertEquals(1, parameterResults.size());
		final IStylesheetParameterRule result = parameterResults.iterator().next();
		assertEquals(UUID.fromString("bf4f5fc6-572b-4634-a692-9b546e182dc9"), result.getParameterID());
		assertFalse(result.getRequestedValue().isPresent());
	}

	@Test
	void testModifyRequest_ForAttributeValue() throws FileNotFoundException, JAXBException {

		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithRequiredAttribute.x2vc_schema");
		// shortcut to provide schema for request construction
		lenient().when(this.schemaManager.getSchema(URI.create("file://somewhere/SampleStylesheet.xslt"), 1))
			.thenReturn(schema);

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule originalRootElementRule = originalRequest.getRootElementRule();
		final ISetAttributeRule originalRootAttributeRule = originalRootElementRule.getAttributeRules()
			.toArray(new ISetAttributeRule[0])[0];
		final UUID modifiedAttributeID = UUID.fromString("73a9b784-7a61-48c4-8110-9855cef81cef");
		assertEquals(modifiedAttributeID, originalRootAttributeRule.getAttributeID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalRootAttributeRule.getID());
		lenient().when(this.valueModifier.getSchemaObjectID()).thenReturn(originalRootAttributeRule.getAttributeID());
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier, MixedContentGenerationMode.FULL);

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

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IAddElementRule originalRootElementRule = originalRequest.getRootElementRule();
		final IAddDataContentRule originalRootContentRule = (IAddDataContentRule) originalRootElementRule
			.getContentRules().get(0);
		final UUID modifiedElementID = UUID.fromString("45023ac4-9c79-4247-bbe5-36f893bd7eaa");
		assertEquals(modifiedElementID, originalRootContentRule.getElementID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalRootContentRule.getID());
		lenient().when(this.valueModifier.getSchemaObjectID()).thenReturn(originalRootContentRule.getElementID());
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier, MixedContentGenerationMode.FULL);

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

	@Test
	void testModifyRequest_ForExtensionFunction() throws FileNotFoundException, JAXBException {

		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithExtensionFunctions.x2vc_schema");
		// shortcut to provide schema for request construction
		lenient().when(this.schemaManager.getSchema(URI.create("file://somewhere/SampleStylesheet.xslt"), 1))
			.thenReturn(schema);

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IExtensionFunctionRule originalFunctionRule = originalRequest.getExtensionFunctionRules().iterator()
			.next();
		final UUID modifiedFunctionID = UUID.fromString("b2104652-4db3-4801-9426-f8876fce19b7");
		assertEquals(modifiedFunctionID, originalFunctionRule.getFunctionID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalFunctionRule.getID());
		lenient().when(this.valueModifier.getSchemaObjectID()).thenReturn(modifiedFunctionID);
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier, MixedContentGenerationMode.FULL);

		final IExtensionFunctionRule modifiedFunctionRule = modifiedRequest.getExtensionFunctionRules().iterator()
			.next();

		final Optional<IRequestedValue> rv = modifiedFunctionRule.getRequestedValue();
		assertTrue(rv.isPresent());
		final IRequestedValue requestedValue = rv.get();
		final Optional<IDocumentModifier> mod = requestedValue.getModifier();
		assertTrue(mod.isPresent());
		final IDocumentModifier modifier = mod.get();
		assertSame(this.valueModifier, modifier);
		assertEquals("foobar", requestedValue.getValue());

		final ImmutableMultimap<UUID, IRequestedValue> rvMap = modifiedRequest.getRequestedValues();
		assertTrue(rvMap.containsKey(modifiedFunctionID));
		final ImmutableCollection<IRequestedValue> valuesFromMap = rvMap.get(modifiedFunctionID);
		assertEquals(1, valuesFromMap.size());
		assertSame(requestedValue, valuesFromMap.iterator().next());

		assertTrue(modifiedRequest.getModifier().isPresent());
		assertSame(this.valueModifier, modifiedRequest.getModifier().get());
	}

	@Test
	void testModifyRequest_ForStylesheetParameters() throws FileNotFoundException, JAXBException {

		final IXMLSchema schema = loadSchema("SingleEmptyElement_WithStylesheetParameters.x2vc_schema");
		// shortcut to provide schema for request construction
		lenient().when(this.schemaManager.getSchema(URI.create("file://somewhere/SampleStylesheet.xslt"), 1))
			.thenReturn(schema);

		final IDocumentRequest originalRequest = this.requestGenerator.generateNewRequest(schema,
				MixedContentGenerationMode.FULL);
		final IStylesheetParameterRule originalParameterRule = originalRequest.getStylesheetParameterRules().iterator()
			.next();
		final UUID modifiedParameterID = UUID.fromString("bf4f5fc6-572b-4634-a692-9b546e182dc9");
		assertEquals(modifiedParameterID, originalParameterRule.getParameterID());

		lenient().when(this.valueModifier.getGenerationRuleID()).thenReturn(originalParameterRule.getID());
		lenient().when(this.valueModifier.getSchemaObjectID()).thenReturn(modifiedParameterID);
		lenient().when(this.valueModifier.getReplacementValue()).thenReturn("foobar");

		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(originalRequest,
				this.valueModifier, MixedContentGenerationMode.FULL);

		final IStylesheetParameterRule modifiedParameterRule = modifiedRequest.getStylesheetParameterRules().iterator()
			.next();

		final Optional<IRequestedValue> rv = modifiedParameterRule.getRequestedValue();
		assertTrue(rv.isPresent());
		final IRequestedValue requestedValue = rv.get();
		final Optional<IDocumentModifier> mod = requestedValue.getModifier();
		assertTrue(mod.isPresent());
		final IDocumentModifier modifier = mod.get();
		assertSame(this.valueModifier, modifier);
		assertEquals("foobar", requestedValue.getValue());

		final ImmutableMultimap<UUID, IRequestedValue> rvMap = modifiedRequest.getRequestedValues();
		assertTrue(rvMap.containsKey(modifiedParameterID));
		final ImmutableCollection<IRequestedValue> valuesFromMap = rvMap.get(modifiedParameterID);
		assertEquals(1, valuesFromMap.size());
		assertSame(requestedValue, valuesFromMap.iterator().next());

		assertTrue(modifiedRequest.getModifier().isPresent());
		assertSame(this.valueModifier, modifiedRequest.getModifier().get());
	}

	private IXMLSchema loadSchema(String schemaFileName) throws FileNotFoundException, JAXBException {
		final File schemaFile = new File(
				"src/test/resources/data/org.x2vc.xml.request.RequestGenerator/" + schemaFileName);
		final XMLSchema schema = (XMLSchema) this.unmarshaller
			.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
		return schema;
	}

}
