package org.x2vc.stylesheet.coverage;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class DirectiveCoverageTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.coverage.DirectiveCoverage#equals(Object)} and
	 * {@link org.x2vc.stylesheet.coverage.DirectiveCoverage#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(DirectiveCoverage.class).verify();
	}

}
