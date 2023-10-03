package org.x2vc.xml.request;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractGenerationRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.AbstractGenerationRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AbstractGenerationRule.class)
			.withRedefinedSubclass(SetAttributeRule.class)
			.usingGetClass()
			.verify();
	}
}
