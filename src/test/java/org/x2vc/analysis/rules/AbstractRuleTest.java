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
package org.x2vc.analysis.rules;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IModifierPayload;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class AbstractRuleTest {

	public static final String TEST_RULE_ID = "testRuleID";

	private AbstractRule rule = new AbstractRule() {

		@Override
		public String getRuleID() {
			return TEST_RULE_ID;
		}

		@Override
		public void checkNode(Node node, IXMLDocumentContainer container, Consumer<IDocumentModifier> collector) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
				Optional<String> injectedValue, Optional<UUID> schemaElementID,
				Consumer<IVulnerabilityCandidate> collector) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AbstractRule#getPathToNode(org.jsoup.nodes.Node)}.
	 */
	@Test
	void testGetPathToNode() {
		final String html = """
							<html>
							<head>
							<title>foo</title>
							<!-- comment -->
							<style>h1 {color:red;}</style>
							<script type="text/javascript">
							// some script
							//<![CDATA[
							let i = 10;
							//]]>
							</script>
							</head>
							<body onload="foo()">
							<h1>some title</h1>
							text 1
							<p id="201" align="center">test</p>
							text 2
							<p id="202" align="center">test</p>
							text 3
							<p id="203" align="center">test</p>
							text 4
							<a id="101" href="http://invalid/">link</a>
							text 5
							</body>
							</html>
							""";
		final Document document = Jsoup.parse(html);

		final Element element1 = document.getElementById("101");
		final Element element2a = document.getElementById("201");
		final Element element2b = document.getElementById("202");
		final Element element2c = document.getElementById("203");

		final String path1 = this.rule.getPathToNode(element1);
		final String path2a = this.rule.getPathToNode(element2a);
		final String path2b = this.rule.getPathToNode(element2b);
		final String path2c = this.rule.getPathToNode(element2c);

		assertEquals("/html/body/a", path1);
		assertEquals("/html/body/p", path2a);
		assertEquals("/html/body/p", path2b);
		assertEquals("/html/body/p", path2c);

		final Elements elements1 = document.selectXpath("/html/body/a");
		assertEquals(List.of(element1), List.of(elements1.toArray()));

		final Elements elements2 = document.selectXpath("/html/body/p");
		assertEquals(List.of(element2a, element2b, element2c), List.of(elements2.toArray()));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_NoModifier() {
		final IXMLDocumentContainer documentContainer = mock();
		final IXMLDocumentDescriptor documentDescriptor = mock();
		when(documentContainer.getDocumentDescriptor()).thenReturn(documentDescriptor);
		when(documentDescriptor.getModifier()).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_NoPayload() {
		final IXMLDocumentContainer documentContainer = mock();
		final IXMLDocumentDescriptor documentDescriptor = mock();
		when(documentContainer.getDocumentDescriptor()).thenReturn(documentDescriptor);
		final IDocumentModifier modifier = mock();
		when(documentDescriptor.getModifier()).thenReturn(Optional.of(modifier));
		when(modifier.getPayload()).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors_WrongType() {
		final IXMLDocumentContainer documentContainer = mock();
		final IXMLDocumentDescriptor documentDescriptor = mock();
		when(documentContainer.getDocumentDescriptor()).thenReturn(documentDescriptor);
		final IDocumentModifier modifier = mock();
		when(documentDescriptor.getModifier()).thenReturn(Optional.of(modifier));
		when(modifier.getPayload()).thenReturn(Optional.of(new IModifierPayload() {
		}));
		assertThrows(IllegalArgumentException.class, () -> this.rule.getElementSelectors(documentContainer));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)}.
	 */
	@Test
	void testGetElementSelectors() {
		final IXMLDocumentContainer documentContainer = mock();
		final IXMLDocumentDescriptor documentDescriptor = mock();
		when(documentContainer.getDocumentDescriptor()).thenReturn(documentDescriptor);
		final IDocumentModifier modifier = mock();
		when(documentDescriptor.getModifier()).thenReturn(Optional.of(modifier));
		final IAnalyzerRulePayload payload = mock();
		when(modifier.getPayload()).thenReturn(Optional.of(payload));
		when(payload.getElementSelector()).thenReturn(Optional.of("elementSelector"));
		final Set<String> selectors = this.rule.getElementSelectors(documentContainer);
		assertEquals(1, selectors.size());
		assertEquals("elementSelector", selectors.iterator().next());
	}

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AbstractRule#consolidateResults(IXMLSchema, Set)}.
	 */
	@Test
	void testConsolidateResultsWithEmptyInput() {
		final IXMLSchema schema = mock();
		final Set<IVulnerabilityCandidate> candidates = Set.of();
		final var result = this.rule.consolidateResults(schema, candidates);
		assertNotNull(result);
		assertEquals(1, result.size(), "number of sections");
		final var section = result.getFirst();
		assertEquals(TEST_RULE_ID, section.getRuleID());
	}

	/**
	 * Test method for {@link org.x2vc.analysis.rules.AbstractRule#consolidateResults(IXMLSchema, Set)}.
	 */
	@Test
	void testConsolidateResultsSingleCandidate() {
		final var schemaObjectID = UUID.randomUUID();
		final IXMLSchema schema = mock();
		when(schema.getObjectPaths(schemaObjectID)).thenReturn(ImmutableSet.of("pathA"));
		final IVulnerabilityCandidate candidate = mock();
		when(candidate.getAffectedOutputElement()).thenReturn("outputElement");
		when(candidate.getAffectingSchemaObject()).thenReturn(schemaObjectID);
		final Set<IVulnerabilityCandidate> candidates = Set.of(candidate);
		final var result = this.rule.consolidateResults(schema, candidates);
		assertNotNull(result);
		assertEquals(1, result.size(), "number of sections");
		final var section = result.getFirst();
		assertEquals(TEST_RULE_ID, section.getRuleID());
		final var issues = section.getIssues();
		assertEquals(1, issues.size(), "number of issues in section");
		final var issue = issues.getFirst();
		assertEquals("outputElement", issue.getAffectedOutputElement());
	}

}
