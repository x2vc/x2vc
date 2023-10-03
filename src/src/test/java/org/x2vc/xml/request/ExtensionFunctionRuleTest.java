package org.x2vc.xml.request;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class ExtensionFunctionRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.ExtensionFunctionRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(ExtensionFunctionRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}

}
