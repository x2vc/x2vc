package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.IExecutionTraceEvent.ExecutionEventType;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

@ExtendWith(MockitoExtension.class)
class ProcessorObserverExecutionTest extends ProcessorObserverTestBase {

	@Test
	void test_Execution_SimpleTemplate() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_SimpleTemplate");
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 4); // match="/"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 5); // html
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 5); // html
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 4); // match="/"
	}

	@Test
	void test_Execution_CallNamedTemplate() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_CallNamedTemplate");
		assertEventRecorded(events, ExecutionEventType.ENTER, "call-template", 6); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 9); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 10); // body
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 10); // body
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 9); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "call-template", 6);
	}

	@Test
	void test_Execution_TemplateParam() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_TemplateParam");
		assertEventRecorded(events, ExecutionEventType.ENTER, "call-template", 6); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 11); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.ENTER, "param", 12); // name="param1"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "param", 12); // name="param1"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 13); // body
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 14); // select="$param1"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 14); // select="$param1"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 13); // body
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 11); // name="foobar"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "call-template", 6); // name="foobar"
	}

	@Test
	void test_Execution_ValueOf() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_ValueOf");
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 6); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 6); // select="@attrib"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 6); // select="@attrib"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 6); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 7); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 7); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 7); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 7); // p
	}

	@Test
	void test_Execution_Element() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_Element");
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 6); // name="body"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 6); // name="body"
	}

	@Test
	void test_Execution_Attribute() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_Attribute");
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 6); // name="body"
		assertEventRecorded(events, ExecutionEventType.ENTER, "att", 7); // name="foo"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "att", 7); // name="foo"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 6); // name="body"
	}

	@Test
	void test_Execution_If() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_If");
		// if statements are recorded as choose statements
		assertEventRecorded(events, ExecutionEventType.ENTER, "choose", 6); // if test="@attribA='foo'"
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 6); // A is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 6); // A is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "choose", 6); // if test="@attribA='foo'"
		assertEventRecorded(events, ExecutionEventType.ENTER, "choose", 9); // if test="@attribB='foo'"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "choose", 9); // if test="@attribB='foo'"
	}

	@Test
	void test_Execution_Choose_01() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_Choose_01");
		assertEventRecorded(events, ExecutionEventType.ENTER, "choose", 7); // when test=...
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 7); // Value 1 is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 7); // Value 1 is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "choose", 7); // when test=...
	}

	@Test
	void test_Execution_Choose_02() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_Choose_02");
		assertEventRecorded(events, ExecutionEventType.ENTER, "choose", 7); // when test=...
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 10); // Value 2 is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 10); // Value 2 is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "choose", 7); // when test=...
	}

	@Test
	void test_Execution_Choose_03() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_Choose_03");
		assertEventRecorded(events, ExecutionEventType.ENTER, "choose", 7); // when test=...
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 13); // None of the values is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 13); // None of the values is foo.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "choose", 7); // when test=...
	}

	@Test
	void test_Execution_ApplyTemplates() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_ApplyTemplates");
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 6); // body
		assertEventRecorded(events, ExecutionEventType.ENTER, "apply-templates", 7);
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.ENTER, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "text", 12); // Some Content.
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 12); // p
		assertEventRecorded(events, ExecutionEventType.LEAVE, "template", 11); // match="elem"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "apply-templates", 7);
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 6); // body
	}

	@Test
	void test_Execution_ForEach() throws SaxonApiException {
		final ImmutableList<ITraceEvent> events = transformAndObserve("Execution_ForEach");
		assertEventRecorded(events, ExecutionEventType.ENTER, "forEach", 6); // select="elem[@attrib='foo']"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 7); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 8); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 8); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 7); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 7); // p
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 8); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 8); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 7); // p
		assertEventRecorded(events, ExecutionEventType.LEAVE, "forEach", 6); // select="elem[@attrib='foo']"
		assertEventRecorded(events, ExecutionEventType.ENTER, "forEach", 11); // select="elem[@attrib='bar']"
		assertEventRecorded(events, ExecutionEventType.ENTER, "element", 12); // div
		assertEventRecorded(events, ExecutionEventType.ENTER, "value-of", 13); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "value-of", 13); // select="."
		assertEventRecorded(events, ExecutionEventType.LEAVE, "element", 12); // div
		assertEventRecorded(events, ExecutionEventType.LEAVE, "forEach", 11); // select="elem[@attrib='bar']"
		assertEventRecorded(events, ExecutionEventType.ENTER, "forEach", 16); // select="elem[@attrib='baz']"
		assertEventRecorded(events, ExecutionEventType.LEAVE, "forEach", 16); // select="elem[@attrib='baz']"
	}

	// ===== auxiliary methods ==============================================================================

	protected void assertEventRecorded(ImmutableList<ITraceEvent> events, ExecutionEventType eventType,
			String executedElement, int expectedLineNumber) {
		final List<IExecutionTraceEvent> filteredEvents = events.stream()
			.filter(IExecutionTraceEvent.class::isInstance)
			.map(IExecutionTraceEvent.class::cast)
			.filter(e -> e.getEventType() == eventType)
			.filter(e -> e.getExecutedElement().equals(Optional.of(executedElement)))
			.toList();
		assertNotEquals(0, filteredEvents.size(),
				String.format("No %s event for %s was recorded", eventType, executedElement));
		final Optional<IExecutionTraceEvent> matchingEvent = filteredEvents.stream()
			.filter(e -> e.getElementLocation().getLineNumber() == expectedLineNumber)
			.findFirst();
		if (matchingEvent.isEmpty()) {
			fail(String.format("%s event for %s was not found in line %d, but in line(s) %s",
					eventType, executedElement, expectedLineNumber,
					filteredEvents.stream()
						.map(e -> e.getElementLocation().getLineNumber())
						.toList()
						.toString()));
		}
	}

}
