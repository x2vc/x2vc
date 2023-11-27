package org.x2vc.utilities.xml;

import static org.x2vc.CustomAssertions.assertInjectionPossible;

import org.junit.jupiter.api.Test;

class ILocationMapBuilderTest {

	@Test
	void testDependencyInjection() throws Exception {
		assertInjectionPossible(ILocationMapBuilder.class);
	}

}
