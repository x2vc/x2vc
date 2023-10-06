package org.x2vc.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.x2vc.report.IProcessingMessage.Severity;

import com.google.common.collect.ImmutableSet;

import nl.jqno.equalsverifier.EqualsVerifier;

class ProcessingMessageTest {

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#getStylesheetURI()},
	 * {@link org.x2vc.report.ProcessingMessage#getSeverity()} and
	 * {@link org.x2vc.report.ProcessingMessage#getMessage()}.
	 */
	@Test
	void testBasicProperties() {
		final ProcessingMessage message = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.build();
		assertEquals(Severity.ERROR, message.getSeverity());
		assertEquals("message 123 test", message.getMessage());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#getDetails()}.
	 */
	@Test
	void testGetDetails_NoEntry() {
		final ProcessingMessage message = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.build();
		assertTrue(message.getDetails().isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#getDetails()}.
	 */
	@Test
	void testGetDetails_OneEntry() {
		final ProcessingMessage message = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.build();
		assertEquals(ImmutableSet.of("details 1"), message.getDetails());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#getDetails()}.
	 */
	@Test
	void testGetDetails_MultipleDifferentEntries() {
		final ProcessingMessage message = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.withDetails(ImmutableSet.of("details 2", "details 3"))
			.build();
		assertEquals(ImmutableSet.of("details 1", "details 2", "details 3"), message.getDetails());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#getDetails()}.
	 */
	@Test
	void testGetDetails_MultipleSameEntries() {
		final ProcessingMessage message = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.withDetails(ImmutableSet.of("details 1", "details 2"))
			.withDetail("details 2")
			.build();
		assertEquals(ImmutableSet.of("details 1", "details 2"), message.getDetails());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#isSameMessage(org.x2vc.report.IProcessingMessage)}.
	 */
	@Test
	void testIsSameMessage() {
		final ProcessingMessage message1 = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.build();
		final ProcessingMessage message2 = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.build();
		final ProcessingMessage message3 = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 2")
			.build();
		final ProcessingMessage message4 = ProcessingMessage.builder(Severity.ERROR, "message 456 test")
			.withDetail("details 2")
			.build();
		assertTrue(message1.isSameMessage(message2));
		assertTrue(message2.isSameMessage(message1));
		assertTrue(message1.isSameMessage(message3));
		assertTrue(message3.isSameMessage(message1));
		assertTrue(message2.isSameMessage(message3));
		assertTrue(message3.isSameMessage(message2));

		assertFalse(message1.isSameMessage(message4));
		assertFalse(message4.isSameMessage(message1));
		assertFalse(message2.isSameMessage(message4));
		assertFalse(message4.isSameMessage(message2));
		assertFalse(message3.isSameMessage(message4));
		assertFalse(message4.isSameMessage(message3));
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#combineWith(org.x2vc.report.IProcessingMessage)}.
	 */
	@Test
	void testCombineWith() {
		final ProcessingMessage message1 = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.withDetails(ImmutableSet.of("details 1", "details 2"))
			.withDetail("details 2")
			.build();

		final ProcessingMessage message2 = ProcessingMessage.builder(Severity.ERROR, "message 123 test")
			.withDetail("details 1")
			.withDetails(ImmutableSet.of("details 5", "details 4"))
			.withDetail("details 2")
			.build();

		final IProcessingMessage combinedMessage1 = message1.combineWith(message2);
		final IProcessingMessage combinedMessage2 = message2.combineWith(message1);

		assertEquals(combinedMessage1, combinedMessage2);

		assertEquals(Severity.ERROR, combinedMessage1.getSeverity());
		assertEquals("message 123 test", combinedMessage1.getMessage());
		assertEquals(ImmutableSet.of("details 1", "details 2", "details 4", "details 5"),
				combinedMessage1.getDetails());

	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessage#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(ProcessingMessage.class).verify();
	}

}
