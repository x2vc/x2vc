package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;

class DocumentRequestTest {

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

}
