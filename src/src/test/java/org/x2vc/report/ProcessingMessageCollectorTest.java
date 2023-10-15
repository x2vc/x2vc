package org.x2vc.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.x2vc.report.IProcessingMessage.Severity;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

class ProcessingMessageCollectorTest {

	private ProcessingMessageCollector collector;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.collector = new ProcessingMessageCollector();
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessageCollector#getSinkFor(java.net.URI)} and
	 * {@link org.x2vc.report.ProcessingMessageCollector#getMessages(java.net.URI)}.
	 */
	@Test
	void testURIAccess() {
		final IProcessingMessage message = mock();
		when(message.getSeverity()).thenReturn(Severity.INFO);
		when(message.getMessage()).thenReturn("rhubarb");

		final URI fileURI = URI.create("foo://bar/baz");

		final Consumer<IProcessingMessage> sink = this.collector.getSinkFor(fileURI);
		sink.accept(message);

		final ImmutableCollection<IProcessingMessage> messages = this.collector.getMessages(fileURI);
		assertEquals(ImmutableSet.of(message), messages);

		this.collector.clear();
		final ImmutableCollection<IProcessingMessage> messages2 = this.collector.getMessages(fileURI);
		assertTrue(messages2.isEmpty());

	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessageCollector#getSinkFor(java.io.File)} and
	 * {@link org.x2vc.report.ProcessingMessageCollector#getMessages(java.io.File)}.
	 */
	@Test
	void testFileAccess() {
		final IProcessingMessage message = mock();
		when(message.getSeverity()).thenReturn(Severity.INFO);
		when(message.getMessage()).thenReturn("rhubarb");

		final URI fileURI = URI.create("foo://bar/baz");
		final File file = mock();
		when(file.toURI()).thenReturn(fileURI);

		final Consumer<IProcessingMessage> sink = this.collector.getSinkFor(file);
		sink.accept(message);

		final ImmutableCollection<IProcessingMessage> messages = this.collector.getMessages(file);
		assertEquals(ImmutableSet.of(message), messages);

		this.collector.clear();
		final ImmutableCollection<IProcessingMessage> messages2 = this.collector.getMessages(file);
		assertTrue(messages2.isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessageCollector#getMessages()}.
	 */
	@Test
	void testGetMessages() {
		final IProcessingMessage message1 = mock(IProcessingMessage.class, "message 1");
		when(message1.getSeverity()).thenReturn(Severity.INFO);
		when(message1.getMessage()).thenReturn("rhubarb1");
		final URI fileURI1 = URI.create("foo://bar/baz1");
		this.collector.getSinkFor(fileURI1).accept(message1);

		final IProcessingMessage message2 = mock(IProcessingMessage.class, "message 2");
		when(message2.getSeverity()).thenReturn(Severity.INFO);
		when(message2.getMessage()).thenReturn("rhubarb2");
		this.collector.getSinkFor(fileURI1).accept(message2);

		final IProcessingMessage message3 = mock(IProcessingMessage.class, "message 3");
		when(message3.getSeverity()).thenReturn(Severity.INFO);
		when(message3.getMessage()).thenReturn("rhubarb3");
		final URI fileURI3 = URI.create("foo://bar/baz2");
		this.collector.getSinkFor(fileURI3).accept(message3);

		final ImmutableMap<URI, ImmutableCollection<IProcessingMessage>> messages = this.collector.getMessages();
		assertEquals(2, messages.keySet().size());
		assertTrue(messages.containsKey(fileURI1));
		assertEquals(ImmutableSet.of(message1, message2), messages.get(fileURI1));
		assertTrue(messages.containsKey(fileURI3));
		assertEquals(ImmutableSet.of(message3), messages.get(fileURI3));

		this.collector.clear();
		final ImmutableMap<URI, ImmutableCollection<IProcessingMessage>> messages2 = this.collector.getMessages();
		assertTrue(messages2.isEmpty());
	}

	/**
	 * Test method for {@link org.x2vc.report.ProcessingMessageCollector#getMessages()}.
	 */
	@Test
	void testMessageMerging() {
		final URI fileURI = URI.create("foo://bar/baz1");
		final IProcessingMessage message1 = mock(IProcessingMessage.class, "message 1");
		when(message1.getSeverity()).thenReturn(Severity.INFO);
		when(message1.getMessage()).thenReturn("rhubarb");
		when(message1.getDetails()).thenReturn(ImmutableSet.of("details 1"));

		final IProcessingMessage message2 = mock(IProcessingMessage.class, "message 2");
		when(message2.getSeverity()).thenReturn(Severity.INFO);
		when(message2.getMessage()).thenReturn("rhubarb");
		when(message1.getDetails()).thenReturn(ImmutableSet.of("details 2"));

		final IProcessingMessage message3 = mock(IProcessingMessage.class, "message 3");
		lenient().when(message1.isSameMessage(message2)).thenReturn(true);
		lenient().when(message2.isSameMessage(message2)).thenReturn(true);
		lenient().when(message1.combineWith(message2)).thenReturn(message3);
		lenient().when(message2.combineWith(message1)).thenReturn(message3);

		this.collector.getSinkFor(fileURI).accept(message1);
		this.collector.getSinkFor(fileURI).accept(message2);

		final ImmutableCollection<IProcessingMessage> messages = this.collector.getMessages(fileURI);
		assertEquals(ImmutableSet.of(message3), messages);
	}

}
