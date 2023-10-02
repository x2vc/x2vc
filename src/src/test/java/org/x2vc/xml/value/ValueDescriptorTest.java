package org.x2vc.xml.value;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class ValueDescriptorTest {

	/**
	 * Test method for {@link org.x2vc.xml.value.ValueDescriptor#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(ValueDescriptor.class).verify();
	}

}
