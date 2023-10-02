package org.x2vc.xml.document;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLDocumentContainerTest {

	/**
	 * Test method for {@link org.x2vc.xml.document.XMLDocumentContainer#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(XMLDocumentContainer.class).verify();
	}

}
