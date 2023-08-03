package org.x2vc.analysis.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IDocumentValueDescriptor;
import org.x2vc.xmldoc.IDocumentValueModifier;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class DirectAttributeCheckRuleTest {

	private DirectAttributeCheckRule rule;

	@Mock
	private IXMLDocumentDescriptor documentDescriptor;

	private List<IDocumentModifier> modifiers;

	private Consumer<IDocumentModifier> collector = new Consumer<IDocumentModifier>() {
		@Override
		public void accept(IDocumentModifier t) {
			DirectAttributeCheckRuleTest.this.modifiers.add(t);
		}
	};

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.rule = new DirectAttributeCheckRule();
		this.modifiers = Lists.newLinkedList();
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractElementRule#checkNode(org.jsoup.nodes.Node, org.x2vc.xmldoc.IXMLDocumentDescriptor, java.util.function.Consumer)}.
	 */
	@ParameterizedTest
	@CsvSource({ "<p qwertzui=\"foobar\">test</p>, qwer, qwertzui, 8",
			"<p onqwertzui=\"foobar\">test</p>, qwer, qwertzui, 8",
			"<p qwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, 8",
			"<p onqwertzuistyle=\"foobar\">test</p>, qwer, qwertzui, 8" })
	void testCheckNode(String html, String prefix, String value, int length) {
		// prepare a value descriptor to return a known ID
		final UUID valueID = UUID.randomUUID();
		final IDocumentValueDescriptor valueDescriptor = mock(IDocumentValueDescriptor.class);
		when(valueDescriptor.getSchemaElementID()).thenReturn(valueID);

		final Element node = parseToElement(html);
		lenient().when(this.documentDescriptor.getValuePrefix()).thenReturn(prefix);
		lenient().when(this.documentDescriptor.getValueLength()).thenReturn(length);

		when(this.documentDescriptor.getValueDescriptors(anyString())).thenReturn(Optional.empty());
		when(this.documentDescriptor.getValueDescriptors(value))
				.thenReturn(Optional.of(ImmutableSet.of(valueDescriptor)));

		this.rule.checkNode(node, this.documentDescriptor, this.collector);

		assertFalse(this.modifiers.isEmpty());
		this.modifiers.forEach(m -> {
			if (m instanceof final IDocumentValueModifier vm) {
				assertEquals(valueID, vm.getSchemaElementID());
				assertTrue(vm.getOriginalValue().isPresent());
				assertEquals(value, vm.getOriginalValue().get());
			}
		});
	}

	private Element parseToElement(String html) {
		final String id = "6702018e-cbd6-4d36-8457-6471a400860d";
		final String frame = String.format("<div id=\"%s\">%s</div>", id, html);
		final Document document = Jsoup.parse(frame);
		return document.getElementById(id).child(0);
	}

}
