package org.x2vc.analysis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Consumer;

import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.xmldoc.IDocumentModifier;
import org.x2vc.xmldoc.IXMLDocumentContainer;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class DocumentAnalyzerTest {

	@Mock
	private IAnalyzerRule rule;

	private DocumentAnalyzer analyzer;

	@Mock
	private Consumer<IDocumentModifier> modifierCollector;

	@Mock
	private Consumer<IVulnerabilityReport> vulnerabilityCollector;

	@Mock
	private IHTMLDocumentContainer container;

	@Mock
	private IXMLDocumentContainer source;

	@Mock
	private IXMLDocumentDescriptor descriptor;

	@Mock
	private IDocumentModifier modifier;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
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
		}).when(this.rule).checkNode(any(Node.class), same(this.descriptor), any());

		this.analyzer.analyzeDocument(this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).checkNode(any(Node.class), same(this.descriptor), any());
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
		when(this.rule.getElementSelectors(this.descriptor)).thenReturn(ImmutableSet.of());

		// mock up rule to produce reports for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IVulnerabilityReport> argCollector = invocation.getArgument(2);
			argCollector.accept(mock(IVulnerabilityReport.class));
			return null;
		}).when(this.rule).verifyNode(any(Node.class), same(this.descriptor), any());

		this.analyzer.analyzeDocument(this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).verifyNode(any(Node.class), same(this.descriptor), any());
		verify(this.vulnerabilityCollector, times(30)).accept(any());
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
		when(this.rule.getElementSelectors(this.descriptor)).thenReturn(ImmutableSet.of("/html/body/p"));

		// mock up rule to produce reports for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IVulnerabilityReport> argCollector = invocation.getArgument(2);
			argCollector.accept(mock(IVulnerabilityReport.class));
			return null;
		}).when(this.rule).verifyNode(any(Node.class), same(this.descriptor), any());

		this.analyzer.analyzeDocument(this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(3)).verifyNode(any(Node.class), same(this.descriptor), any());
		verify(this.vulnerabilityCollector, times(3)).accept(any());
	}

}
