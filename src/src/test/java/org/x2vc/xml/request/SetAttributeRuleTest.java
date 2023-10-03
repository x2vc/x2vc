package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class SetAttributeRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.SetAttributeRule#normalize()}.
	 */
	@Test
	void testNormalizeWithoutRequestedValue() {
		final UUID ruleID = UUID.randomUUID();
		final UUID attributeID = UUID.randomUUID();

		final SetAttributeRule originalRule = new SetAttributeRule(ruleID, attributeID);
		final SetAttributeRule normalizedRule = (SetAttributeRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(attributeID, normalizedRule.getAttributeID());
		assertFalse(normalizedRule.getRequestedValue().isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.SetAttributeRule#normalize()}.
	 */
	@Test
	void testNormalizeWithRequestedValue() {
		final UUID ruleID = UUID.randomUUID();
		final UUID attributeID = UUID.randomUUID();
		final IRequestedValue originalValue = mock(IRequestedValue.class);
		final IRequestedValue normalizedValue = mock(IRequestedValue.class);
		when(originalValue.normalize()).thenReturn(normalizedValue);

		final SetAttributeRule originalRule = new SetAttributeRule(ruleID, attributeID, originalValue);
		final SetAttributeRule normalizedRule = (SetAttributeRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(attributeID, normalizedRule.getAttributeID());
		assertSame(normalizedValue, normalizedRule.getRequestedValue().get());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.SetAttributeRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(SetAttributeRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}
}
