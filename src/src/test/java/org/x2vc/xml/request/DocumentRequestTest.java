package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;

class DocumentRequestTest {

	@Test
	void testRequestedValueIndex() {
		final UUID rootUUID = UUID.randomUUID();
		final Builder rootBuilder = new AddElementRule.Builder(rootUUID);
		final UUID rootAttribUUID = UUID.randomUUID();
		final RequestedValue rootAttribValue = new RequestedValue("rootAttrib");
		rootBuilder.addAttributeRule(new SetAttributeRule(rootAttribUUID, rootAttribValue));
		final RequestedValue rootTextValue = new RequestedValue("rootText");
		rootBuilder.addContentRule(new AddTextContentRule(rootTextValue));
		final RequestedValue rootRawValue = new RequestedValue("rootRaw");
		rootBuilder.addContentRule(new AddRawContentRule(rootRawValue));
		final UUID subUUID = UUID.randomUUID();
		final Builder subBuilder = new AddElementRule.Builder(subUUID);
		final UUID subAttribUUID = UUID.randomUUID();
		final RequestedValue subAttribValue = new RequestedValue("subAttrib");
		subBuilder.addAttributeRule(new SetAttributeRule(subAttribUUID, subAttribValue));
		final RequestedValue subTextValue = new RequestedValue("subText");
		subBuilder.addContentRule(new AddTextContentRule(subTextValue));
		final RequestedValue subRawValue = new RequestedValue("subRaw");
		subBuilder.addContentRule(new AddRawContentRule(subRawValue));
		rootBuilder.addContentRule(subBuilder.build());
		final AddElementRule rootRule = rootBuilder.build();

		final URI schemaURI = URI.create("foo:bar");
		final IDocumentRequest request = new DocumentRequest(schemaURI, 1, rootRule);
		assertEquals(schemaURI, request.getSchemaURI());
		assertEquals(1, request.getSchemaVersion());
		assertSame(rootRule, request.getRootElementRule());

		final ImmutableMultimap<UUID, IRequestedValue> requestedValues = request.getRequestedValues();
		final ImmutableCollection<IRequestedValue> rootValues = requestedValues.get(rootUUID);
		assertTrue(rootValues.contains(rootTextValue));
		assertTrue(rootValues.contains(rootRawValue));
		final ImmutableCollection<IRequestedValue> rootAttribValues = requestedValues.get(rootAttribUUID);
		assertTrue(rootAttribValues.contains(rootAttribValue));
		final ImmutableCollection<IRequestedValue> subValues = requestedValues.get(subUUID);
		assertTrue(subValues.contains(subTextValue));
		assertTrue(subValues.contains(subRawValue));
		final ImmutableCollection<IRequestedValue> subAttribValues = requestedValues.get(subAttribUUID);
		assertTrue(subAttribValues.contains(subAttribValue));

	}

}
