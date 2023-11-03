package org.x2vc.processor;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

@ExtendWith(MockitoExtension.class)
class ProcessorObserverValueAccessTest extends ProcessorObserverTestBase {

	@Test
	void test_ValueAccess_ApplyTemplates() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_ApplyTemplates");
		assertEventRecorded(result.events(),
				"child::element(Q{}elem)[(Q{http://www.w3.org/2001/XMLSchema}string(data(@attrib))) eq foo]",
				"d9a68d96-b6c9-404b-89ec-a9cddddb449c", 6);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"833148ee-068f-4436-96a2-f5d5605e74b2", 11);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"07172937-4229-4689-b360-59bcc267caec", 11);
	}

	@Test
	void test_ValueAccess_Attribute() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_Attribute");
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(attribute::attribute(Q{}attrib1)))",
				"8d871801-d06a-43b6-a169-bcb4c55a4192", 8);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(TraceExpression(ValueOf(convertTo_xs:string(data(.))))))",
				"8d871801-d06a-43b6-a169-bcb4c55a4192", 8);
	}

	@Test
	void test_ValueAccess_Choose_01() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_Choose_01");
		assertEventRecorded(result.events(),
				"(Q{http://www.w3.org/2001/XMLSchema}string(data(attribute::attribute(Q{}value1)))) eq foo",
				"a738b1f1-2697-4e94-8c60-fe0c50e0febc", 7);
		assertEventRecorded(result.events(),
				"(Q{http://www.w3.org/2001/XMLSchema}string(data(attribute::attribute(Q{}value2)))) eq foo",
				"a738b1f1-2697-4e94-8c60-fe0c50e0febc", 7);
	}

	@Test
	void test_ValueAccess_Choose_02() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_Choose_02");
		assertEventRecorded(result.events(),
				"exists(descendant::element(Q{}foo))",
				result.documentTraceID().toString(), 8);
	}

	@Test
	void test_ValueAccess_Element() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_Element");
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(attribute::attribute(Q{}attrib1)))",
				"0c216fb2-d7ca-4d2c-8c8c-7c73b7fcb152", 7);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"0c216fb2-d7ca-4d2c-8c8c-7c73b7fcb152", 8);
	}

	@Test
	void test_ValueAccess_ForEach() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_ForEach");
		assertEventRecorded(result.events(),
				"child::element(Q{}elem)[(Q{http://www.w3.org/2001/XMLSchema}string(data(@attrib))) eq foo]",
				"69920556-6b60-44fd-a508-7a2c463ec347", 6);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"6cd96bc3-5837-4d5f-8418-bf10fecc96a2", 8);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"b486f94b-a754-4f3c-9eab-ba0eb329e3a8", 8);
		assertEventRecorded(result.events(),
				"child::element(Q{}elem)[(Q{http://www.w3.org/2001/XMLSchema}string(data(@attrib))) eq bar]",
				"69920556-6b60-44fd-a508-7a2c463ec347", 11);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"9aa2897d-5bb4-4b2f-9ed3-6156f7687edc", 13);
		assertEventRecorded(result.events(),
				"SortExpression(child::element(Q{}elem)[(Q{http://www.w3.org/2001/XMLSchema}string(data(@attrib))) eq baz], SortKeyDefinitionList(SortKeyDefinition(data(.), ascending, #default, , yes, http://www.w3.org/2005/xpath-functions/collation/codepoint)))",
				"69920556-6b60-44fd-a508-7a2c463ec347", 16);
	}

	@Test
	void test_ValueAccess_If() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_If");
		assertEventRecorded(result.events(),
				"(Q{http://www.w3.org/2001/XMLSchema}string(data(attribute::attribute(Q{}attribA)))) eq foo",
				"7da1b8d0-0286-47f0-8290-b76f29f64412", 6);
		assertEventRecorded(result.events(),
				"(Q{http://www.w3.org/2001/XMLSchema}string(data(attribute::attribute(Q{}attribB)))) eq foo",
				"7da1b8d0-0286-47f0-8290-b76f29f64412", 9);
	}

	@Test
	void test_ValueAccess_TemplateParam() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_TemplateParam");
		assertEventRecorded(result.events(),
				"((.) treat as node())/attribute::attribute(Q{}attrib2)",
				"6e8d0b49-107a-4465-93d0-6e025ba9c264", 14);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(first(data($param1)))",
				"6e8d0b49-107a-4465-93d0-6e025ba9c264", 16);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(first(data($param2)))",
				"6e8d0b49-107a-4465-93d0-6e025ba9c264", 18);
	}

	@Test
	void test_ValueAccess_ValueOf() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_ValueOf");
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(attribute::attribute(Q{}attrib)))",
				"844d8129-bccb-40f5-bc56-8d98ae4658e2", 6);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data(.))",
				"844d8129-bccb-40f5-bc56-8d98ae4658e2", 7);
	}

	@Test
	void test_ValueAccess_Variables() throws SaxonApiException {
		final TransformResult result = transformAndObserve("ValueAccess_Variables");
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data($globalVar))",
				"a3610577-74f5-4302-bc38-5eaf8f129f1b", 13);
		assertEventRecorded(result.events(),
				"convertTo_xs:string(data($localVar))",
				"a3610577-74f5-4302-bc38-5eaf8f129f1b", 15);
	}

	// ===== auxiliary methods ==============================================================================

	protected void assertEventRecorded(ImmutableList<ITraceEvent> events, String expectedExpression,
			String contextElementID, int expectedLineNumber) {

		final List<IValueAccessTraceEvent> filteredContextEvents = events.stream()
			.filter(IValueAccessTraceEvent.class::isInstance)
			.map(IValueAccessTraceEvent.class::cast)
			.filter(e -> e.getContextElementID().equals(Optional.of(UUID.fromString(contextElementID))))
			.toList();
		assertNotEquals(0, filteredContextEvents.size(),
				String.format("No event for context element %s was recorded", contextElementID));

		final List<IValueAccessTraceEvent> filteredExpressionEvent = filteredContextEvents.stream()
			.filter(e -> e.getExpression().toString().equals(expectedExpression))
			.toList();
		if (filteredExpressionEvent.isEmpty()) {
			fail(String.format("No event for expression %s was recorded. Expressions recorded: %s",
					expectedExpression,
					filteredExpressionEvent.stream()
						.map(e -> e.toString())
						.toList()
						.toString()));
		}

		final Optional<IValueAccessTraceEvent> matchingEvent = filteredExpressionEvent.stream()
			.filter(e -> e.getLocation().getLineNumber() == expectedLineNumber)
			.findFirst();

		if (matchingEvent.isEmpty()) {
			fail(String.format(
					"Expression %s was not found for source line %d, but for source line(s) %s",
					expectedExpression, expectedLineNumber,
					filteredExpressionEvent.stream()
						.map(e -> e.getLocation().getLineNumber())
						.toList()
						.toString()));
		}
	}

}
