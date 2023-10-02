package org.x2vc.analysis.rules;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AnalyzerRulePayloadTest {

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AnalyzerRulePayload#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AnalyzerRulePayload.class).verify();
	}

}
