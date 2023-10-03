package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.StringWriter;
import java.net.URI;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;

import nl.jqno.equalsverifier.EqualsVerifier;

class DocumentRequestTest {

	/**
	 * Test to determine whether a document request can be serialized.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerializer() throws JAXBException {
		final UUID rootElementUUID = UUID.fromString("6ffd1551-1bd1-4a8a-bdf6-eaeff9ae6885");
		final RequestedValue rootAttribValue = new RequestedValue("rootAttrib");
		final RequestedValue rootTextValue = new RequestedValue("rootText");
		final RequestedValue rootRawValue = new RequestedValue("rootRaw");
		final UUID subElementUUID = UUID.fromString("b437def9-af22-49c6-93d0-4fda8eea1ef4");
		final RequestedValue subAttribValue = new RequestedValue("subAttrib");
		final RequestedValue subTextValue = new RequestedValue("subText");
		final RequestedValue subRawValue = new RequestedValue("subRaw");

		final AddElementRule rootRule = AddElementRule.builder(UUID.fromString("69663c5a-899f-4c6e-b654-601656a20143"))
			.withRuleID(UUID.fromString("791baa58-7355-4397-bb4a-a5f1f8971886"))
			.addAttributeRule(
					new SetAttributeRule(UUID.fromString("33c47ac1-1234-4ce3-bfbd-40e0dd25a2ab"),
							UUID.fromString("451f471f-9b02-4957-84f0-81f5688a01bd"), rootAttribValue))
			.addContentRule(new AddDataContentRule(UUID.fromString("b82f9349-b7e4-4520-9ac2-ad54d14bb979"),
					rootElementUUID, rootTextValue))
			.addContentRule(new AddRawContentRule(UUID.fromString("a99587c6-8b5e-496d-98d2-4fa055a9c8d4"),
					rootElementUUID, rootRawValue))
			.addContentRule(AddElementRule.builder(UUID.fromString("2f76969b-56ad-4526-8ed8-85603f977a3f"))
				.withRuleID(UUID.fromString("5a854b7c-e985-4317-9d45-48cf23ac3da1"))
				.addAttributeRule(
						new SetAttributeRule(UUID.fromString("19fd9630-f228-4f35-a797-2af3adecb06b"),
								UUID.fromString("b978f495-a9f1-48bd-a11f-5e83ed91702d"), subAttribValue))
				.addContentRule(new AddDataContentRule(UUID.fromString("770eda4e-cec2-444f-a07f-5ff2f4aaf527"),
						subElementUUID, subTextValue))
				.addContentRule(new AddRawContentRule(UUID.fromString("6a8c19ad-299c-4907-8a85-c6c49e8c9ec5"),
						subElementUUID, subRawValue))
				.build())
			.build();

		final IDocumentRequest request = DocumentRequest
			.builder(URI.create("foo:bar"), 1, URI.create("bar:foo"), rootRule)
			.addExtensionFunctionRule(new ExtensionFunctionRule(UUID.fromString("93cf9247-0968-4c57-956c-0f9ca1e238cd"),
					UUID.fromString("bf7ef119-3ce7-4b64-a7a0-b789d30d1fa6")))
			.addExtensionFunctionRule(new ExtensionFunctionRule(UUID.fromString("b001c2b8-cae7-45e3-8634-ad51566af497"),
					UUID.fromString("b63f6510-fd72-4b42-b0e6-ebfc3cca5d85"),
					new RequestedValue("foobar")))
			.build();

		final JAXBContext context = JAXBContext.newInstance(DocumentRequest.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		final StringWriter writer = new StringWriter();
		marshaller.marshal(request, writer);

		final String expected = """
								<request schemaURI="foo:bar" schemaVersion="1" stylesheetURI="bar:foo" mixedContentGenerationMode="FULL">
								    <rootElementRule elementReferenceID="69663c5a-899f-4c6e-b654-601656a20143" ruleID="791baa58-7355-4397-bb4a-a5f1f8971886">
								        <attributeRules>
								            <attributeRule attributeID="451f471f-9b02-4957-84f0-81f5688a01bd" ruleID="33c47ac1-1234-4ce3-bfbd-40e0dd25a2ab">
								                <requestedValue>
								                    <value>rootAttrib</value>
								                </requestedValue>
								            </attributeRule>
								        </attributeRules>
								        <contentRules>
								            <addDataContentRule elementID="6ffd1551-1bd1-4a8a-bdf6-eaeff9ae6885" ruleID="b82f9349-b7e4-4520-9ac2-ad54d14bb979">
								                <requestedValue>
								                    <value>rootText</value>
								                </requestedValue>
								            </addDataContentRule>
								            <addRawContentRule elementID="6ffd1551-1bd1-4a8a-bdf6-eaeff9ae6885" ruleID="a99587c6-8b5e-496d-98d2-4fa055a9c8d4">
								                <requestedValue>
								                    <value>rootRaw</value>
								                </requestedValue>
								            </addRawContentRule>
								            <addElementRule elementReferenceID="2f76969b-56ad-4526-8ed8-85603f977a3f" ruleID="5a854b7c-e985-4317-9d45-48cf23ac3da1">
								                <attributeRules>
								                    <attributeRule attributeID="b978f495-a9f1-48bd-a11f-5e83ed91702d" ruleID="19fd9630-f228-4f35-a797-2af3adecb06b">
								                        <requestedValue>
								                            <value>subAttrib</value>
								                        </requestedValue>
								                    </attributeRule>
								                </attributeRules>
								                <contentRules>
								                    <addDataContentRule elementID="b437def9-af22-49c6-93d0-4fda8eea1ef4" ruleID="770eda4e-cec2-444f-a07f-5ff2f4aaf527">
								                        <requestedValue>
								                            <value>subText</value>
								                        </requestedValue>
								                    </addDataContentRule>
								                    <addRawContentRule elementID="b437def9-af22-49c6-93d0-4fda8eea1ef4" ruleID="6a8c19ad-299c-4907-8a85-c6c49e8c9ec5">
								                        <requestedValue>
								                            <value>subRaw</value>
								                        </requestedValue>
								                    </addRawContentRule>
								                </contentRules>
								            </addElementRule>
								        </contentRules>
								    </rootElementRule>
								    <extensionFunctions>
								        <function functionID="bf7ef119-3ce7-4b64-a7a0-b789d30d1fa6" ruleID="93cf9247-0968-4c57-956c-0f9ca1e238cd"/>
								        <function functionID="b63f6510-fd72-4b42-b0e6-ebfc3cca5d85" ruleID="b001c2b8-cae7-45e3-8634-ad51566af497">
								            <requestedValue>
								                <value>foobar</value>
								            </requestedValue>
								        </function>
								    </extensionFunctions>
								</request>
								""";
		assertXMLEquals(expected, writer.toString());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.DocumentRequest#buildRequestedValues()}.
	 */
	@Test
	void testRequestedValueIndex() {
		final UUID rootReferenceUUID = UUID.randomUUID();
		final UUID rootElementUUID = UUID.randomUUID();
		final Builder rootBuilder = AddElementRule.builder(rootReferenceUUID);

		final UUID rootAttribUUID = UUID.randomUUID();
		final RequestedValue rootAttribValue = new RequestedValue("rootAttrib");
		rootBuilder.addAttributeRule(new SetAttributeRule(rootAttribUUID, rootAttribValue));

		final RequestedValue rootTextValue = new RequestedValue("rootText");
		rootBuilder.addContentRule(new AddDataContentRule(rootElementUUID, rootTextValue));

		final RequestedValue rootRawValue = new RequestedValue("rootRaw");
		rootBuilder.addContentRule(new AddRawContentRule(rootElementUUID, rootRawValue));

		final UUID subReferenceUUID = UUID.randomUUID();
		final UUID subElementUUID = UUID.randomUUID();
		final Builder subBuilder = AddElementRule.builder(subReferenceUUID);

		final UUID subAttribUUID = UUID.randomUUID();
		final RequestedValue subAttribValue = new RequestedValue("subAttrib");
		subBuilder.addAttributeRule(new SetAttributeRule(subAttribUUID, subAttribValue));

		final RequestedValue subTextValue = new RequestedValue("subText");
		subBuilder.addContentRule(new AddDataContentRule(subElementUUID, subTextValue));

		final RequestedValue subRawValue = new RequestedValue("subRaw");
		subBuilder.addContentRule(new AddRawContentRule(subElementUUID, subRawValue));

		rootBuilder.addContentRule(subBuilder.build());
		final AddElementRule rootRule = rootBuilder.build();

		final URI stylesheetURI = URI.create("bar:foo");
		final URI schemaURI = URI.create("foo:bar");
		final IDocumentRequest request = DocumentRequest.builder(schemaURI, 1, stylesheetURI, rootRule).build();
		assertEquals(schemaURI, request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertEquals(stylesheetURI, request.getStylesheeURI());
		assertSame(rootRule, request.getRootElementRule());

		final ImmutableMultimap<UUID, IRequestedValue> requestedValues = request.getRequestedValues();

		final ImmutableCollection<IRequestedValue> rootValues = requestedValues.get(rootElementUUID);
		assertTrue(rootValues.contains(rootTextValue));
		assertTrue(rootValues.contains(rootRawValue));

		final ImmutableCollection<IRequestedValue> rootAttribValues = requestedValues.get(rootAttribUUID);
		assertTrue(rootAttribValues.contains(rootAttribValue));

		final ImmutableCollection<IRequestedValue> subValues = requestedValues.get(subElementUUID);
		assertTrue(subValues.contains(subTextValue));
		assertTrue(subValues.contains(subRawValue));

		final ImmutableCollection<IRequestedValue> subAttribValues = requestedValues.get(subAttribUUID);
		assertTrue(subAttribValues.contains(subAttribValue));

	}

	/**
	 * Test method for {@link org.x2vc.xml.request.DocumentRequest#normalize()}.
	 */
	@Test
	void testNormalizeWithoutModifier() {

		final IAddElementRule originalRootRule = mock(IAddElementRule.class);
		final IAddElementRule normalizedRootRule = mock(IAddElementRule.class);
		when(originalRootRule.normalize()).thenReturn(normalizedRootRule);

		final URI schemaURI = URI.create("foo:bar");
		final int schemaVersion = 1;
		final URI stylesheetURI = URI.create("bar:foo");

		final IDocumentRequest originalRequest = DocumentRequest.builder(schemaURI, schemaVersion, stylesheetURI,
				originalRootRule)
			.build();
		final IDocumentRequest normalizedRequest = originalRequest.normalize();

		assertNotSame(originalRequest, normalizedRequest);
		assertEquals(schemaURI, normalizedRequest.getSchemaURI());
		assertEquals(schemaVersion, normalizedRequest.getSchemaVersion());
		assertEquals(stylesheetURI, normalizedRequest.getStylesheeURI());
		assertSame(normalizedRootRule, normalizedRequest.getRootElementRule());
		assertFalse(normalizedRequest.getModifier().isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.DocumentRequest#normalize()}.
	 */
	@Test
	void testNormalizeWithModifier() {

		final IAddElementRule originalRootRule = mock(IAddElementRule.class);
		final IAddElementRule normalizedRootRule = mock(IAddElementRule.class);
		when(originalRootRule.normalize()).thenReturn(normalizedRootRule);

		final IDocumentModifier originalModifier = mock(IDocumentModifier.class);
		final IDocumentModifier normalizedModifier = mock(IDocumentModifier.class);
		when(originalModifier.normalize()).thenReturn(normalizedModifier);

		final URI schemaURI = URI.create("foo:bar");
		final int schemaVersion = 1;
		final URI stylesheetURI = URI.create("bar:foo");

		final IDocumentRequest originalRequest = DocumentRequest.builder(schemaURI, schemaVersion, stylesheetURI,
				originalRootRule)
			.withModifier(originalModifier)
			.build();
		final IDocumentRequest normalizedRequest = originalRequest.normalize();

		assertNotSame(originalRequest, normalizedRequest);
		assertEquals(schemaURI, normalizedRequest.getSchemaURI());
		assertEquals(schemaVersion, normalizedRequest.getSchemaVersion());
		assertEquals(stylesheetURI, normalizedRequest.getStylesheeURI());
		assertSame(normalizedRootRule, normalizedRequest.getRootElementRule());
		assertSame(normalizedModifier, normalizedRequest.getModifier().get());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.DocumentRequest#getRuleByID(UUID)}.
	 */
	@Test
	void testGetRuleByID() {
		final UUID rootReferenceUUID = UUID.randomUUID();
		final UUID rootElementUUID = UUID.randomUUID();
		final Builder rootBuilder = AddElementRule.builder(rootReferenceUUID);

		final UUID rootAttribUUID = UUID.randomUUID();
		final RequestedValue rootAttribValue = new RequestedValue("rootAttrib");
		final SetAttributeRule rootAttribRule = new SetAttributeRule(rootAttribUUID, rootAttribValue);
		rootBuilder.addAttributeRule(rootAttribRule);

		final RequestedValue rootTextValue = new RequestedValue("rootText");
		final AddDataContentRule rootDataContentRule = new AddDataContentRule(rootElementUUID, rootTextValue);
		rootBuilder.addContentRule(rootDataContentRule);

		final RequestedValue rootRawValue = new RequestedValue("rootRaw");
		final AddRawContentRule rootRawContentRule = new AddRawContentRule(rootElementUUID, rootRawValue);
		rootBuilder.addContentRule(rootRawContentRule);

		final UUID subReferenceUUID = UUID.randomUUID();
		final UUID subElementUUID = UUID.randomUUID();
		final Builder subBuilder = AddElementRule.builder(subReferenceUUID);

		final UUID subAttribUUID = UUID.randomUUID();
		final RequestedValue subAttribValue = new RequestedValue("subAttrib");
		final SetAttributeRule subAttributeRule = new SetAttributeRule(subAttribUUID, subAttribValue);
		subBuilder.addAttributeRule(subAttributeRule);

		final RequestedValue subTextValue = new RequestedValue("subText");
		final AddDataContentRule subDataContentRule = new AddDataContentRule(subElementUUID, subTextValue);
		subBuilder.addContentRule(subDataContentRule);

		final RequestedValue subRawValue = new RequestedValue("subRaw");
		final AddRawContentRule subRawContentRule = new AddRawContentRule(subElementUUID, subRawValue);
		subBuilder.addContentRule(subRawContentRule);

		final AddElementRule subRule = subBuilder.build();
		rootBuilder.addContentRule(subRule);
		final AddElementRule rootRule = rootBuilder.build();

		final URI stylesheetURI = URI.create("bar:foo");
		final URI schemaURI = URI.create("foo:bar");
		final IDocumentRequest request = DocumentRequest.builder(schemaURI, 1, stylesheetURI, rootRule).build();

		assertSame(rootAttribRule, request.getRuleByID(rootAttribRule.getID()));
		assertSame(rootDataContentRule, request.getRuleByID(rootDataContentRule.getID()));
		assertSame(rootRawContentRule, request.getRuleByID(rootRawContentRule.getID()));
		assertSame(subAttributeRule, request.getRuleByID(subAttributeRule.getID()));
		assertSame(subDataContentRule, request.getRuleByID(subDataContentRule.getID()));
		assertSame(subRawContentRule, request.getRuleByID(subRawContentRule.getID()));
		assertSame(subRule, request.getRuleByID(subRule.getID()));
		assertSame(rootRule, request.getRuleByID(rootRule.getID()));
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.DocumentRequest#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(DocumentRequest.class).verify();
	}

}
