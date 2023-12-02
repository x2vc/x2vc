package org.x2vc.stylesheet.structure;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractElementNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.AbstractElementNode#hashCode()}
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(AbstractElementNode.class)
			.withRedefinedSubclass(XMLNode.class)
			.usingGetClass()
			.verify();
	}

}
