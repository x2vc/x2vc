package org.x2vc.analysis.rules;

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
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.xml.document.IDocumentValueModifier;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class ElementCopyCheckRuleTest extends AnalyzerRuleTestBase {

	private AbstractRule rule;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	void setUp() throws Exception {
		super.setUp();
		this.rule = new ElementCopyCheckRule(this.schemaManager);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#getRuleID()}.
	 */
	@Test
	void testRuleID() {
		assertEquals(ElementCopyCheckRule.RULE_ID, this.rule.getRuleID());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#checkNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentDescriptor, java.util.function.Consumer)}.
	 *
	 * @param html   the source text of the node that will be passed to the rule to
	 *               check
	 * @param prefix the prefix of the simulated generated value
	 * @param value  the simulated generated value
	 * @param length the length of the simulated generated value
	 */
	@ParameterizedTest
	@CsvSource({ "foobar, qwer, qwertzui, 8, true",
			"qwertzui, qwer, qwertzui, 8, false",
			"abcqwertzui, qwer, qwertzui, 8, false",
			"qwertzuixyz, qwer, qwertzui, 8, false" })
	void testCheckElementNode(String text, String prefix, String value, int length, boolean modifiersEmpty) {
		final IXMLElementType elementType = mockUnlimitedStringElement();
		final UUID elementTypeID = elementType.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock(IValueDescriptor.class);
		lenient().when(valueDescriptor.getSchemaElementID()).thenReturn(elementTypeID);
		lenient().when(valueDescriptor.getValue()).thenReturn(value);

		final Node node = parseToNode(text);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(prefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(length);

		lenient().when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		lenient().when(this.documentDescriptor.getValueDescriptors(text))
			.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentContainer, this.modifierCollector);

		assertEquals(modifiersEmpty, this.modifiers.isEmpty());
		this.modifiers.forEach(m -> {
			if (m instanceof final IDocumentValueModifier vm) {
				assertEquals(elementTypeID, vm.getSchemaElementID());
				assertTrue(vm.getOriginalValue().isPresent());
				assertEquals(value, vm.getOriginalValue().get());
			}
		});
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectElementCheckRule#verifyNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentContainer, java.util.function.Consumer)}.
	 *
	 * @param html                       the source of the HTML snippet containing
	 *                                   the text that will be passed to the rule to
	 *                                   verify
	 * @param elementSelector            the selector issued by the rule to identify
	 *                                   the element
	 * @param injectedElement            the name of the injected element identified
	 *                                   by the payload
	 * @param expectedVulnerabilityCount the number of vulnerabilities we expect to
	 *                                   find
	 * @param expectedOutputElement      the expected output element path
	 */
	@ParameterizedTest
	@CsvSource({ "<p>foobar123</p>, sel, script, 0, -",
			"<p>barfoo<script>alert('XSS')</script>foobar</p>, sel, script, 1, /html/body/div/p" })
	void testVerifyNode(String html, String elementSelector, String injectedElement,
			int expectedVulnerabilityCount, String expectedOutputElement) {
		final UUID taskID = UUID.randomUUID();
		final UUID schemaElementID = UUID.randomUUID();

		mockModifierWithPayload(elementSelector, injectedElement, schemaElementID);

		final Element element = parseToElement(html);
		this.rule.verifyNode(taskID, element, this.documentContainer, this.vulnerabilityCollector);

		assertEquals(expectedVulnerabilityCount, this.vulnerabilities.size());
		if (expectedVulnerabilityCount > 0) {
			final IVulnerabilityCandidate vc = this.vulnerabilities.get(0);
			assertEquals(ElementCopyCheckRule.RULE_ID, vc.getAnalyzerRuleID());
			assertEquals(schemaElementID, vc.getAffectingSchemaObject());
			assertEquals(expectedOutputElement, vc.getAffectedOutputElement());
//			assertEquals(, vc.getInputSample());
			// TODO XSS Vulnerability: check input sampler
			assertEquals(html.replaceAll("\\s", ""), vc.getOutputSample().replaceAll("\\s", ""));
			assertEquals(taskID, vc.getTaskID());
		}
	}

}
