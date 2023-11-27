package org.x2vc.utilities;

import static org.x2vc.CustomAssertions.assertInjectionPossible;

import org.junit.jupiter.api.Test;

class IFileReaderTest {

	@Test
	void testDependencyInjection() throws Exception {
		assertInjectionPossible(IFileReader.class);
	}

}
