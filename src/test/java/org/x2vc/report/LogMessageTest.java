package org.x2vc.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class LogMessageTest {

	@Test
	void testDefaultMessage() {
		final var msg = new LogMessage();
		assertEquals(Level.TRACE, msg.getLevel());
		assertEquals("", msg.getThreadName());
		assertEquals("", msg.getMessage());
	}

	@Test
	void testStandardMessage() {
		final var msg = new LogMessage(Level.INFO, "thread", "message");
		assertEquals(Level.INFO, msg.getLevel());
		assertEquals("thread", msg.getThreadName());
		assertEquals("message", msg.getMessage());

		final var str = msg.toString();
		assertTrue(str.contains("level=INFO"));
		assertTrue(str.contains("threadName=thread"));
		assertTrue(str.contains("message=message"));
	}

	@Test
	void testHTMLMessage() {
		final var msg = new LogMessage(Level.INFO, "thread", "<b>\"He's dead, Jim &amp; Spock.\"</b>");
		assertEquals("&lt;b&gt;&quot;He&apos;s dead, Jim &amp; Spock.&quot;&lt;/b&gt;", msg.getMessage());
	}

	/**
	 * Test method for {@link org.x2vc.report.LogMessage#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(LogMessage.class)
			.suppress(Warning.REFERENCE_EQUALITY)
			.verify();
	}

}
