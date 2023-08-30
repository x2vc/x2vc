package org.x2vc.xml.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddRawContentRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.AddRawContentRule#normalize()}.
	 */
	@Test
	void testNormalize() {
		final UUID ruleID = UUID.randomUUID();
		final UUID elementID = UUID.randomUUID();
		final IRequestedValue originalValue = mock(IRequestedValue.class);
		final IRequestedValue normalizedValue = mock(IRequestedValue.class);
		when(originalValue.normalize()).thenReturn(normalizedValue);

		final AddRawContentRule originalRule = new AddRawContentRule(ruleID, elementID, originalValue);
		final AddRawContentRule normalizedRule = (AddRawContentRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(elementID, normalizedRule.getElementID());
		assertSame(normalizedValue, normalizedRule.getRequestedValue().get());
	}

}
