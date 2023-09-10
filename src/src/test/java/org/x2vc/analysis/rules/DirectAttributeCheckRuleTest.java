package org.x2vc.analysis.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.xml.document.IDocumentValueModifier;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class DirectAttributeCheckRuleTest extends AnalyzerRuleTestBase {

	private AbstractRule rule;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	void setUp() throws Exception {
		super.setUp();
		this.rule = new DirectAttributeCheckRule(this.schemaManager);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#getRuleID()}.
	 */
	@Test
	void testRuleID() {
		assertEquals(DirectAttributeCheckRule.RULE_ID, this.rule.getRuleID());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#checkNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentDescriptor, java.util.function.Consumer)}.
	 *
	 * @param html   the source code of the node that will be passed to the rule to
	 *               check
	 * @param prefix the prefix of the simulated generated value
	 * @param value  the simulated generated value
	 * @param query  the query value that is supposed to be used to retrieve the
	 *               value descriptor
	 * @param length the length of the simulated generated value
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, qwer, qwertzui, qwertzui, 8",
			"<p onqwertzui=\"foobar\">test</p>, qwer, qwertzui, onqwertzui, 8",
			"<p qwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, qwertzuistyle, 8",
			"<p onqwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, onqwertzuistyle, 8" })
	void testCheckAttributeNode(String html, String prefix, String value, String query, int length) {
		// prepare schema information
		final IXMLAttribute attribute = mockUnlimitedStringAttribute();
		final UUID attributeID = attribute.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock(IValueDescriptor.class);
		when(valueDescriptor.getSchemaElementID()).thenReturn(attributeID);
		when(valueDescriptor.getValue()).thenReturn(value);

		final Element node = parseToElement(html);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(prefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(length);

		when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		when(this.documentDescriptor.getValueDescriptors(query))
			.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentContainer, this.modifierCollector);

		assertFalse(this.modifiers.isEmpty());
		this.modifiers.forEach(m -> {
			if (m instanceof final IDocumentValueModifier vm) {
				assertEquals(attributeID, vm.getSchemaElementID());
				assertTrue(vm.getOriginalValue().isPresent());
				assertEquals(value, vm.getOriginalValue().get());
			}
		});
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#checkNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentDescriptor, java.util.function.Consumer)}.
	 *
	 * @param html   the source code of the node that will be passed to the rule to
	 *               check
	 * @param prefix the prefix of the simulated generated value
	 * @param value  the simulated generated value
	 * @param query  the query value that is supposed to be used to retrieve the
	 *               value descriptor
	 * @param length the length of the simulated generated value
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, qwer, qwertzui, qwertzui, 8",
			"<p onqwertzui=\"foobar\">test</p>, qwer, qwertzui, onqwertzui, 8",
			"<p qwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, qwertzuistyle, 8",
			"<p onqwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, onqwertzuistyle, 8" })
	void testCheckElementNode(String html, String prefix, String value, String query, int length) {
		final IXMLElementType elementType = mockUnlimitedStringElement();
		final UUID elementTypeID = elementType.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock(IValueDescriptor.class);
		when(valueDescriptor.getSchemaElementID()).thenReturn(elementTypeID);
		when(valueDescriptor.getValue()).thenReturn(value);

		final Element node = parseToElement(html);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(prefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(length);

		when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		when(this.documentDescriptor.getValueDescriptors(query))
			.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentContainer, this.modifierCollector);

		assertFalse(this.modifiers.isEmpty());
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
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#verifyNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentContainer, java.util.function.Consumer)}.
	 *
	 * @param html                       the source code of the node that will be
	 *                                   passed to the rule to verify
	 * @param elementSelector            the selector issued by the rule to identify
	 *                                   the element
	 * @param injectedAttribute          the name of the injected attribute
	 *                                   identified by the payload
	 * @param expectedVulnerabilityCount the number of vulnerabilities we expect to
	 *                                   find
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, /p, qwertzui, 1",
			"<p qwertzui=\"foobar\">test</p>, /p, asdfasdf, 0" })
	void testVerifyNode(String html, String elementSelector, String injectedAttribute,
			int expectedVulnerabilityCount) {
		final UUID taskID = UUID.randomUUID();
		final UUID schemaElementID = UUID.randomUUID();

		mockModifierWithPayload(elementSelector, injectedAttribute, schemaElementID);

		final Element node = parseToElement(html);
		this.rule.verifyNode(taskID, node, this.documentContainer, this.vulnerabilityCollector);

		assertEquals(expectedVulnerabilityCount, this.vulnerabilities.size());
		if (expectedVulnerabilityCount > 0) {
			final IVulnerabilityCandidate vc = this.vulnerabilities.get(0);
			assertEquals(DirectAttributeCheckRule.RULE_ID, vc.getAnalyzerRuleID());
			assertEquals(schemaElementID, vc.getAffectingSchemaObject());
			assertEquals(elementSelector, vc.getAffectedOutputElement());
//			assertEquals(, vc.getInputSample());
			// TODO XSS Vulnerability: check input sampler
			assertEquals(html, vc.getOutputSample());
			assertEquals(taskID, vc.getTaskID());
		}
	}

}
