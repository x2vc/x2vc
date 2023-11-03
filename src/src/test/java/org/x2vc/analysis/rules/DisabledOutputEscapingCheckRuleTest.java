package org.x2vc.analysis.rules;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.UUID;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.xml.document.IDocumentValueModifier;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class DisabledOutputEscapingCheckRuleTest extends AnalyzerRuleTestBase {

	private AbstractRule rule;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	void setUp() throws Exception {
		super.setUp();
		this.rule = new DisabledOutputEscapingCheckRule(this.schemaManager);
	}

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AbstractElementRule#getRuleID()}.
	 */
	@Test
	void testRuleID() {
		assertEquals(DisabledOutputEscapingCheckRule.RULE_ID, this.rule.getRuleID());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#checkNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentDescriptor, java.util.function.Consumer)}.
	 *
	 * @param html              the source code of the node that will be passed to the rule to check
	 * @param query             the query value that is supposed to be used to retrieve the value descriptor
	 * @param modifiersExpected whether the check should result in modifiers being issued
	 */
	@ParameterizedTest
	@CsvSource({
			"foobar,      DATA,  false",
			"foobar,      MIXED, false",
			"qwer1234,    DATA,  true",
			"qwer1234,    MIXED, true",
			"abcqwer1234, DATA,  true",
			"abcqwer1234, MIXED, true",
			"qwer1234xyz, DATA,  true",
			"qwer1234xyz, MIXED, true"
	})
	void testCheckElementNode(String text,
			IElementType.ContentType contentType, boolean modifiersExpected) {
		// common test values
		final String valuePrefix = "qwer";
		final int valueLength = 8;
		final String generatedValue = "qwer1234";

		IElementType elementType;
		if (contentType == ContentType.DATA) {
			elementType = mockUnlimitedStringElement();
		} else if (contentType == ContentType.MIXED) {
			elementType = mockMixedElement();
		} else {
			throw new IllegalArgumentException("unsupported content type");
		}
		final UUID elementTypeID = elementType.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock();

		lenient().when(valueDescriptor.getSchemaObjectID()).thenReturn(elementTypeID);
		lenient().when(valueDescriptor.getValue()).thenReturn(generatedValue);

		final Node node = parseToNode(text);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(valuePrefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(valueLength);

		lenient().when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		lenient().when(this.documentDescriptor.getValueDescriptors(text))
			.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentContainer, this.modifierCollector);

		assertEquals(modifiersExpected, !this.modifiers.isEmpty());
		this.modifiers.forEach(m -> {
			if (m instanceof final IDocumentValueModifier vm) {
				assertEquals(elementTypeID, vm.getSchemaObjectID());
				assertTrue(vm.getOriginalValue().isPresent());
				assertEquals(generatedValue, vm.getOriginalValue().get());
			}
		});
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectElementCheckRule#verifyNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentContainer, java.util.function.Consumer)}.
	 *
	 * @param html                       the source of the HTML snippet containing the text that will be passed to the
	 *                                   rule to verify
	 * @param elementSelector            the selector issued by the rule to identify the element
	 * @param injectedElement            the name of the injected element identified by the payload
	 * @param expectedVulnerabilityCount the number of vulnerabilities we expect to find
	 * @param expectedOutputElement      the expected output element path
	 */
	@ParameterizedTest
	@CsvSource({
			"<p>foobar123</p>, sel, script, 0, -",
			"<p>barfoo<script>alert('XSS')</script>foobar</p>, sel, XSS, 1, /html/body/div/p"
	})
	void testVerifyNode(String html, String elementSelector, String injectedElement,
			int expectedVulnerabilityCount, String expectedOutputElement) {
		final UUID taskID = UUID.randomUUID();
		final UUID schemaElementID = UUID.randomUUID();

		mockModifierWithPayload(elementSelector, injectedElement, schemaElementID, "script", null);
		lenient().when(this.documentContainer.getDocument()).thenReturn("<xml-doc</>");

		final Element element = parseToElement(html);
		this.rule.verifyNode(taskID, element, this.documentContainer, this.vulnerabilityCollector);

		assertEquals(expectedVulnerabilityCount, this.vulnerabilities.size());
		if (expectedVulnerabilityCount > 0) {
			final IVulnerabilityCandidate vc = this.vulnerabilities.get(0);
			assertEquals(DisabledOutputEscapingCheckRule.RULE_ID, vc.getAnalyzerRuleID());
			assertEquals(schemaElementID, vc.getAffectingSchemaObject());
			assertEquals(expectedOutputElement, vc.getAffectedOutputElement());
			assertEquals("<xml-doc</>", vc.getInputSample());
			assertEquals(html.replaceAll("\\s", ""), vc.getOutputSample().replaceAll("\\s", ""));
			assertEquals(taskID, vc.getTaskID());
		}
	}

}
