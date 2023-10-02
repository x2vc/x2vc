package org.x2vc.schema.structure;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractSchemaObjectTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.AbstractSchemaObject#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AbstractSchemaObject.class)
			.withRedefinedSubclass(XMLElementReference.class)
			.usingGetClass()
			.verify();
	}

}
