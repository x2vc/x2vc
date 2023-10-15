package org.x2vc.xml.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class DocumentValueModifierTest {

	/**
	 * Test method for {@link org.x2vc.xml.document.DocumentValueModifier#normalize()}.
	 */
	@Test
	void testNormalize() {
		final UUID schemaElementID = UUID.randomUUID();
		final UUID generationRuleID = UUID.randomUUID();
		final String originalValue = "$$ORIGINAL$$";
		final String replacementValue = "%%REPLACEMENT%%";
		final String analyzerRuleID = "Rule-42-ABC";
		final IModifierPayload payload = mock();

		final DocumentValueModifier originalModifier = DocumentValueModifier.builder(schemaElementID,
				generationRuleID)
			.withOriginalValue(originalValue).withReplacementValue(replacementValue).withAnalyzerRuleID(analyzerRuleID)
			.withPayload(payload).build();
		final DocumentValueModifier normalizedModifier = (DocumentValueModifier) originalModifier.normalize();

		assertNotSame(originalModifier, normalizedModifier);
		assertEquals(schemaElementID, normalizedModifier.getSchemaObjectID());
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedModifier.getGenerationRuleID());
		assertTrue(normalizedModifier.getOriginalValue().isEmpty());
		assertEquals(replacementValue, normalizedModifier.getReplacementValue());
		assertTrue(normalizedModifier.getAnalyzerRuleID().isEmpty());
		assertTrue(normalizedModifier.getPayload().isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.xml.document.DocumentValueModifier#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(DocumentValueModifier.class).verify();
	}

}
