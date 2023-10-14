package org.x2vc.xml.request;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class StylesheetParameterRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.StylesheetParameterRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(StylesheetParameterRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}

}
