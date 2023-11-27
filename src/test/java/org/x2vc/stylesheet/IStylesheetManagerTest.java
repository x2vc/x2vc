package org.x2vc.stylesheet;

import static org.x2vc.CustomAssertions.assertInjectionPossible;

import org.junit.jupiter.api.Test;

class IStylesheetManagerTest {

	@Test
	void testDependencyInjection() throws Exception {
		assertInjectionPossible(IStylesheetManager.class);
	}

}
