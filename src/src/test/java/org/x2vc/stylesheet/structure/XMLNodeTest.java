package org.x2vc.stylesheet.structure;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XMLNode#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLNode.class).verify();
	}

}
