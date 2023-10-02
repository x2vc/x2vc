package org.x2vc.utilities;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class PolymorphLocationTest {

	/**
	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(PolymorphLocation.class).verify();
	}

//	/**
//	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#from(javax.xml.stream.Location)}.
//	 */
//	@Test
//	void testFromLocation() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#from(javax.xml.transform.SourceLocator)}.
//	 */
//	@Test
//	void testFromSourceLocator() {
//		fail("Not yet implemented");
//	}

}
