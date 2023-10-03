package org.x2vc.analysis.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

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
import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.xml.document.IDocumentValueModifier;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class GeneralURLCheckRuleTest extends AnalyzerRuleTestBase {

	private AbstractRule rule;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeEach
	void setUp() throws Exception {
		super.setUp();
		this.rule = new GeneralURLCheckRule(this.schemaManager, Lists.newArrayList("mytarget"));
	}

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AbstractElementRule#getRuleID()}.
	 */
	@Test
	void testRuleID() {
		assertEquals(GeneralURLCheckRule.RULE_ID, this.rule.getRuleID());
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
			"<p src=\"asdfghjk\">test</p>,                                -,           false",
			"<p src=\"qwer1234\">test</p>,                                qwer1234,    false",
			"<p src=\"aaaqwer1234\">test</p>,                             aaaqwer1234, false",
			"<p src=\"qwer1234zzz\">test</p>,                             qwer1234zzz, false",
			"<p mytarget=\"asdfghjk\">test</p>,                           -,           false",
			"<p mytarget=\"qwer1234\">test</p>,                           qwer1234,    true",
			"<p mytarget=\"aaaqwer1234\">test</p>,                        aaaqwer1234, true",
			"<p mytarget=\"qwer1234zzz\">test</p>,                        qwer1234zzz, true",
			"<script mytarget=\"asdfghjk\">test</p>,                      -,           false",
			"<script mytarget=\"qwer1234\">test</p>,                      qwer1234,    false",
			"<script mytarget=\"aaaqwer1234\">test</p>,                   aaaqwer1234, false",
			"<script mytarget=\"qwer1234zzz\">test</p>,                   qwer1234zzz, false",
			"<link rel=\"stylesheet\" mytarget=\"asdfghjk\">test</p>,     -,           false",
			"<link rel=\"stylesheet\" mytarget=\"qwer1234\">test</p>,     qwer1234,    false",
			"<link rel=\"stylesheet\" mytarget=\"aaaqwer1234\">test</p>,  aaaqwer1234, false",
			"<link rel=\"stylesheet\" mytarget=\"qwer1234zzz\">test</p>,  qwer1234zzz, false",
	})
	void testCheckAttributeNode(String html, String query, boolean modifiersExpected) {
		// common test values
		final String valuePrefix = "qwer";
		final int valueLength = 8;
		final String generatedValue = "qwer1234";

		// prepare schema information
		final IAttribute attribute = mockUnlimitedStringAttribute();
		final UUID attributeID = attribute.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock(IValueDescriptor.class);
		lenient().when(valueDescriptor.getSchemaObjectID()).thenReturn(attributeID);
		lenient().when(valueDescriptor.getValue()).thenReturn(generatedValue);

		final Element node = parseToElement(html);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(valuePrefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(valueLength);

		lenient().when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		lenient().when(this.documentDescriptor.getValueDescriptors(query))
			.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentContainer, this.modifierCollector);

		assertEquals(modifiersExpected, !this.modifiers.isEmpty());
		this.modifiers.forEach(m -> {
			if (m instanceof final IDocumentValueModifier vm) {
				assertEquals(attributeID, vm.getSchemaObjectID());
				assertTrue(vm.getOriginalValue().isPresent());
				assertEquals(generatedValue, vm.getOriginalValue().get());
			}
		});
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
			"<p src=\"asdfghjk\">test</p>,                                -,           false",
			"<p src=\"qwer1234\">test</p>,                                qwer1234,    false",
			"<p src=\"aaaqwer1234\">test</p>,                             aaaqwer1234, false",
			"<p src=\"qwer1234zzz\">test</p>,                             qwer1234zzz, false",
			"<p mytarget=\"asdfghjk\">test</p>,                           -,           false",
			"<p mytarget=\"qwer1234\">test</p>,                           qwer1234,    true",
			"<p mytarget=\"aaaqwer1234\">test</p>,                        aaaqwer1234, true",
			"<p mytarget=\"qwer1234zzz\">test</p>,                        qwer1234zzz, true",
			"<script mytarget=\"asdfghjk\">test</p>,                      -,           false",
			"<script mytarget=\"qwer1234\">test</p>,                      qwer1234,    false",
			"<script mytarget=\"aaaqwer1234\">test</p>,                   aaaqwer1234, false",
			"<script mytarget=\"qwer1234zzz\">test</p>,                   qwer1234zzz, false",
			"<link rel=\"stylesheet\" mytarget=\"asdfghjk\">test</p>,     -,           false",
			"<link rel=\"stylesheet\" mytarget=\"qwer1234\">test</p>,     qwer1234,    false",
			"<link rel=\"stylesheet\" mytarget=\"aaaqwer1234\">test</p>,  aaaqwer1234, false",
			"<link rel=\"stylesheet\" mytarget=\"qwer1234zzz\">test</p>,  qwer1234zzz, false",
	})
	void testCheckElementNode(String html, String query, boolean modifiersExpected) {
		// common test values
		final String valuePrefix = "qwer";
		final int valueLength = 8;
		final String generatedValue = "qwer1234";

		final IElementType elementType = mockUnlimitedStringElement();
		final UUID elementTypeID = elementType.getID();

		// prepare a value descriptor to return a known ID
		final IValueDescriptor valueDescriptor = mock(IValueDescriptor.class);
		lenient().when(valueDescriptor.getSchemaObjectID()).thenReturn(elementTypeID);
		lenient().when(valueDescriptor.getValue()).thenReturn(generatedValue);

		final Element node = parseToElement(html);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(valuePrefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(valueLength);

		lenient().when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		lenient().when(this.documentDescriptor.getValueDescriptors(query))
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
	 * {@link org.x2vc.analysis.rules.GeneralURLCheckRule#verifyNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentContainer, java.util.function.Consumer)}.
	 *
	 * @param html                       the source code of the node that will be passed to the rule to verify
	 * @param elementSelector            the selector issued by the rule to identify the element
	 * @param injectedValue              the value injected by the modifier
	 * @param expectedVulnerabilityCount the number of vulnerabilities we expect to find
	 * @param expectedOutputElement      the expected output element path
	 */
	@ParameterizedTest
	@CsvSource({
			"<p mytarget=\"qwer1234\">test</p>,    /p, qwer1234, 1, /html/body/div/p",
			"<p mytarget=\"aaaqwer1234\">test</p>, /p, qwer1234, 1, /html/body/div/p",
			"<p mytarget=\"qwer1234zzz\">test</p>, /p, qwer1234, 1, /html/body/div/p",
	})
	void testVerifyNode(String html, String elementSelector, String injectedValue,
			int expectedVulnerabilityCount, String expectedOutputElement) {
		final UUID taskID = UUID.randomUUID();
		final UUID schemaElementID = UUID.randomUUID();

		mockModifierWithPayload(elementSelector, injectedValue, schemaElementID, null, "mytarget");
		lenient().when(this.documentContainer.getDocument()).thenReturn("<xml-doc</>");

		final Element node = parseToElement(html);
		this.rule.verifyNode(taskID, node, this.documentContainer, this.vulnerabilityCollector);

		assertEquals(expectedVulnerabilityCount, this.vulnerabilities.size());
		if (expectedVulnerabilityCount > 0) {
			final IVulnerabilityCandidate vc = this.vulnerabilities.get(0);
			assertEquals(GeneralURLCheckRule.RULE_ID, vc.getAnalyzerRuleID());
			assertEquals(schemaElementID, vc.getAffectingSchemaObject());
			assertEquals(expectedOutputElement, vc.getAffectedOutputElement());
			assertEquals("<xml-doc</>", vc.getInputSample());
			assertEquals(html, vc.getOutputSample());
			assertEquals(taskID, vc.getTaskID());
		}
	}

}
