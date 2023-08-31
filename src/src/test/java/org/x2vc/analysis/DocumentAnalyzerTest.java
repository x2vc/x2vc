package org.x2vc.analysis;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class DocumentAnalyzerTest {

	@Mock
	private IAnalyzerRule rule;

	private DocumentAnalyzer analyzer;

	@Mock
	private Consumer<IDocumentModifier> modifierCollector;

	@Mock
	private Consumer<IVulnerabilityCandidate> vulnerabilityCollector;

	@Mock
	private IHTMLDocumentContainer container;

	@Mock
	private IXMLDocumentContainer source;

	@Mock
	private IXMLDocumentDescriptor descriptor;

	@Mock
	private IDocumentModifier modifier;

	private UUID taskID;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.taskID = UUID.randomUUID();
		this.analyzer = new DocumentAnalyzer(Sets.newSet(this.rule));
		lenient().when(this.container.isFailed()).thenReturn(false);
		lenient().when(this.container.getSource()).thenReturn(this.source);
		lenient().when(this.source.getDocumentDescriptor()).thenReturn(this.descriptor);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#analyzeDocument(org.x2vc.processor.IHTMLDocumentContainer)}.
	 */
	@Test
	void testFirstPass() {
		when(this.container.getDocument()).thenReturn(Optional.of(
			"""
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
			<p align="center">test</p>
			text 2
			<a href="http://invalid/">link</a>
			text 3
			</body>
			</html>
			"""
		));

		// mock up rule to produce modifiers for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IDocumentModifier> argCollector = invocation.getArgument(2);
			argCollector.accept(mock(IDocumentModifier.class));
			return null;
		}).when(this.rule).checkNode(any(Node.class), same(this.source), any());

		this.analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).checkNode(any(Node.class), same(this.source), any());
		verify(this.modifierCollector, times(30)).accept(any());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithoutFilter() {
		when(this.container.getDocument()).thenReturn(Optional.of(
			"""
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
			<p align="center">test</p>
			text 2
			<a href="http://invalid/">link</a>
			text 3
			</body>
			</html>
			"""
		));
		when(this.source.getDocumentDescriptor().getModifier()).thenReturn(Optional.of(this.modifier));

		// connect rule and modifier
		when(this.rule.getRuleID()).thenReturn("FOO-RULE");
		when(this.modifier.getAnalyzerRuleID()).thenReturn(Optional.of("FOO-RULE"));

		// rule does not provide filter for this test
		when(this.rule.getElementSelectors(this.source)).thenReturn(ImmutableSet.of());

		// mock up rule to produce reports for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IVulnerabilityCandidate> argCollector = invocation.getArgument(3);
			argCollector.accept(mock(IVulnerabilityCandidate.class));
			return null;
		}).when(this.rule).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());

		this.analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());
		verify(this.vulnerabilityCollector, times(30)).accept(any());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithUnknownRuleID() {
		// connect rule and modifier
		when(this.source.getDocumentDescriptor().getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.rule.getRuleID()).thenReturn("FOO-RULE");
		when(this.modifier.getAnalyzerRuleID()).thenReturn(Optional.of("BAR-RULE")); // does not match the line above

		assertThrows(IllegalArgumentException.class, () -> this.analyzer.analyzeDocument(this.taskID, this.container,
				this.modifierCollector, this.vulnerabilityCollector));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithDuplicateID() {
		// connect rules with same ID
		when(this.source.getDocumentDescriptor().getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.modifier.getAnalyzerRuleID()).thenReturn(Optional.of("FOO-RULE"));
		final IAnalyzerRule rule1 = mock(IAnalyzerRule.class);
		final IAnalyzerRule rule2 = mock(IAnalyzerRule.class);
		when(rule1.getRuleID()).thenReturn("FOO-RULE");
		when(rule2.getRuleID()).thenReturn("FOO-RULE"); // must trigger an exception because rule IDs have to be unique
		this.analyzer = new DocumentAnalyzer(Sets.newSet(rule1, rule2));

		assertThrows(IllegalArgumentException.class, () -> this.analyzer.analyzeDocument(this.taskID, this.container,
				this.modifierCollector, this.vulnerabilityCollector));
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithFilter() {
		when(this.container.getDocument()).thenReturn(Optional.of(
			"""
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
			<p align="center">test</p>
			text 2
			<p align="center">test</p>
			text 1
			<p align="center">test</p>
			text 2
			<a href="http://invalid/">link</a>
			text 3
			</body>
			</html>
			"""
		));
		when(this.source.getDocumentDescriptor().getModifier()).thenReturn(Optional.of(this.modifier));

		// connect rule and modifier
		when(this.rule.getRuleID()).thenReturn("FOO-RULE");
		when(this.modifier.getAnalyzerRuleID()).thenReturn(Optional.of("FOO-RULE"));

		// rule does provide filter for this test
		when(this.rule.getElementSelectors(this.source)).thenReturn(ImmutableSet.of("/html/body/p"));

		// mock up rule to produce reports for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IVulnerabilityCandidate> argCollector = invocation.getArgument(3);
			argCollector.accept(mock(IVulnerabilityCandidate.class));
			return null;
		}).when(this.rule).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());

		this.analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(3)).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());
		verify(this.vulnerabilityCollector, times(3)).accept(any());
	}

}
