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
	 * Test method for
	 * {@link org.x2vc.xml.request.DocumentRequest#buildRequestedValues()}.
	 */
	@Test
	void testRequestedValueIndex() {
		final UUID rootReferenceUUID = UUID.randomUUID();
		final UUID rootElementUUID = UUID.randomUUID();
		final Builder rootBuilder = new AddElementRule.Builder(rootReferenceUUID);

		final UUID rootAttribUUID = UUID.randomUUID();
		final RequestedValue rootAttribValue = new RequestedValue("rootAttrib");
		rootBuilder.addAttributeRule(new SetAttributeRule(rootAttribUUID, rootAttribValue));

		final RequestedValue rootTextValue = new RequestedValue("rootText");
		rootBuilder.addContentRule(new AddDataContentRule(rootElementUUID, rootTextValue));

		final RequestedValue rootRawValue = new RequestedValue("rootRaw");
		rootBuilder.addContentRule(new AddRawContentRule(rootElementUUID, rootRawValue));

		final UUID subReferenceUUID = UUID.randomUUID();
		final UUID subElementUUID = UUID.randomUUID();
		final Builder subBuilder = new AddElementRule.Builder(subReferenceUUID);

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
		final IDocumentRequest request = new DocumentRequest(schemaURI, 1, stylesheetURI, rootRule);
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

		final IDocumentRequest originalRequest = new DocumentRequest(schemaURI, schemaVersion, stylesheetURI,
				originalRootRule);
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

		final IDocumentRequest originalRequest = new DocumentRequest(schemaURI, schemaVersion, stylesheetURI,
				originalRootRule, originalModifier);
		final IDocumentRequest normalizedRequest = originalRequest.normalize();

		assertNotSame(originalRequest, normalizedRequest);
		assertEquals(schemaURI, normalizedRequest.getSchemaURI());
		assertEquals(schemaVersion, normalizedRequest.getSchemaVersion());
		assertEquals(stylesheetURI, normalizedRequest.getStylesheeURI());
		assertSame(normalizedRootRule, normalizedRequest.getRootElementRule());
		assertSame(normalizedModifier, normalizedRequest.getModifier().get());
	}

}
