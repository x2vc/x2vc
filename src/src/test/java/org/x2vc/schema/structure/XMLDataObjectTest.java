package org.x2vc.schema.structure;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLDataObjectTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.XMLDataObject#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLDataObject.class)
			.withRedefinedSubclass(XMLAttribute.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}

}
