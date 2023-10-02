package org.x2vc.schema.evolution;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AddAttributeModifierTest {

	/**
	 * Test method for {@link org.x2vc.schema.evolution.AddAttributeModifier#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AddAttributeModifier.class).verify();
	}

}
