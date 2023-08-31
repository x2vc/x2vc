package org.x2vc.analysis.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.analysis.IVulnerabilityReport;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLDatatype;
import org.x2vc.utilities.URIHandling;
import org.x2vc.utilities.URIHandling.ObjectType;
import org.x2vc.xml.document.*;
import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class DirectAttributeCheckRuleTest {

	private AbstractRule rule;

	@Mock
	private ISchemaManager schemaManager;

	@Mock
	private IXMLSchema schema;

	@Mock
	private IXMLAttribute attribute;

	@Mock
	private IXMLDocumentDescriptor documentDescriptor;

	@Mock
	private IXMLDocumentContainer documentContainer;

	@Mock
	private IDocumentModifier modifier;

	private List<IDocumentModifier> modifiers;

	private Consumer<IDocumentModifier> modifierCollector = new Consumer<IDocumentModifier>() {
		@Override
		public void accept(IDocumentModifier t) {
			DirectAttributeCheckRuleTest.this.modifiers.add(t);
		}
	};

	private List<IVulnerabilityReport> vulnerabilities;

	private Consumer<IVulnerabilityReport> vulnerabilityCollector = new Consumer<IVulnerabilityReport>() {
		@Override
		public void accept(IVulnerabilityReport t) {
			DirectAttributeCheckRuleTest.this.vulnerabilities.add(t);
		}
	};

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.rule = new DirectAttributeCheckRule(this.schemaManager);
		this.modifiers = Lists.newLinkedList();
		this.vulnerabilities = Lists.newLinkedList();
		lenient().when(this.documentContainer.getDocumentDescriptor()).thenReturn(this.documentDescriptor);
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
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, qwer, qwertzui, qwertzui, 8",
			"<p onqwertzui=\"foobar\">test</p>, qwer, qwertzui, onqwertzui, 8",
			"<p qwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, qwertzuistyle, 8",
			"<p onqwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, onqwertzuistyle, 8" })
	void testCheckNode(String html, String prefix, String value, String query, int length) {
		// prepare stylesheet URI
		final URI stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.documentContainer.getStylesheeURI()).thenReturn(stylesheetURI);

		// prepare schema information
		when(this.schemaManager.getSchema(stylesheetURI)).thenReturn(this.schema);
		final UUID attributeID = UUID.randomUUID();
		when(this.schema.getObjectByID(attributeID)).thenReturn(this.attribute);
		when(this.attribute.asAttribute()).thenReturn(this.attribute);
		when(this.attribute.getDatatype()).thenReturn(XMLDatatype.STRING);
		when(this.attribute.getMaxLength()).thenReturn(Optional.empty());

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
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_NoModifier() {
		when(this.documentDescriptor.getModifier()).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(this.documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_NoPayload() {
		when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.modifier.getPayload()).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(this.documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_WrongType() {
		when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.modifier.getPayload()).thenReturn(Optional.of(new IModifierPayload() {
			private static final long serialVersionUID = -6630813475107736706L;
		}));
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(this.documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors() {
		when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.modifier.getPayload()).thenReturn(Optional.of(new DirectAttributeCheckPayload("elementSelector", "injectedAttribute")));
		final Set<String> selectors = this.rule.getElementSelectors(this.documentContainer);
		assertEquals(1, selectors.size());
		assertEquals("elementSelector", selectors.iterator().next());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.DirectAttributeCheckRule#verifyNode(org.jsoup.nodes.Node, org.x2vc.xml.document.IXMLDocumentContainer, java.util.function.Consumer)}.
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, /p, qwertzui, foobar, true",
			"<p qwertzui=\"foobar\">test</p>, /p, qwertzui, boofar, false",
			"<p qwertzui=\"foobar\">test</p>, /p, asdfasdf, foobar, false", })
	void testVerifyNode(String html, String elementSelector, String injectedAttribute, String injectedValue,
			boolean result) {
		final Element node = parseToElement(html);

		when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.modifier.getPayload()).thenReturn(
				Optional.of(new DirectAttributeCheckPayload(elementSelector, injectedAttribute, injectedValue)));

		this.rule.verifyNode(node, this.documentContainer, this.vulnerabilityCollector);

		assertEquals(!result, this.vulnerabilities.isEmpty());
		// TODO XSS Vulnerability: check for contents
	}

	private Element parseToElement(String html) {
		final String id = "6702018e-cbd6-4d36-8457-6471a400860d";
		final String frame = String.format("<div id=\"%s\">%s</div>", id, html);
		final Document document = Jsoup.parse(frame);
		return document.getElementById(id).child(0);
	}

}
