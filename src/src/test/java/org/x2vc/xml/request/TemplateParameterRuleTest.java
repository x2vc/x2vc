package org.x2vc.xml.request;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class TemplateParameterRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.TemplateParameterRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(TemplateParameterRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}

}
