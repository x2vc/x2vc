package org.x2vc.processor;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class ExecutionTraceEventTest {

	/**
	 * Test method for {@link org.x2vc.processor.ExecutionTraceEvent#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(ExecutionTraceEvent.class).verify();
	}

}
