package org.x2vc.schema.evolution;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AddParameterModifierTest {

	/**
	 * Test method for {@link org.x2vc.schema.evolution.AddParameterModifier#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AddParameterModifier.class).verify();
	}

}
