package org.x2vc.schema.evolution;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class SchemaElementProxyTest {

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(SchemaElementProxy.class).verify();
	}

}
