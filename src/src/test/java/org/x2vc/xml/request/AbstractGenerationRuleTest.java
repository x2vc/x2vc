package org.x2vc.xml.request;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractGenerationRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.AbstractGenerationRule#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(AbstractGenerationRule.class).verify();
	}
}
