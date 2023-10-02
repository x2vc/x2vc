package org.x2vc.schema.evolution;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class AddElementModifierTest {

	/**
	 * Test method for {@link org.x2vc.schema.evolution.AddElementModifier#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AddElementModifier.class)
			.suppress(Warning.NONFINAL_FIELDS)
			.verify();
	}

}
