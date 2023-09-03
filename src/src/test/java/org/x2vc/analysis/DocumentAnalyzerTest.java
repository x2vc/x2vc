package org.x2vc.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.analysis.results.IVulnerabilityCandidate;
import org.x2vc.analysis.results.IVulnerabilityReport;
import org.x2vc.analysis.results.IVulnerabilityReportSection;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.utilities.URIHandling;
import org.x2vc.utilities.URIHandling.ObjectType;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class DocumentAnalyzerTest {

	@Mock
	private IAnalyzerRule rule;

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

	private URI stylesheetURI;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.taskID = UUID.randomUUID();
		this.stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
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
		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(this.rule));

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
				"""));

		// mock up rule to produce modifiers for collector wiring test
		doAnswer(invocation -> {
			final Consumer<IDocumentModifier> argCollector = invocation.getArgument(2);
			argCollector.accept(mock(IDocumentModifier.class));
			return null;
		}).when(this.rule).checkNode(any(Node.class), same(this.source), any());

		analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).checkNode(any(Node.class), same(this.source), any());
		verify(this.modifierCollector, times(30)).accept(any());
	}

	/**
	 * Test method for {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithoutFilter() {
		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(this.rule));

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
				"""));
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

		analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(30)).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());
		verify(this.vulnerabilityCollector, times(30)).accept(any());
	}

	/**
	 * Test method for {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithUnknownRuleID() {
		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(this.rule));

		// connect rule and modifier
		when(this.source.getDocumentDescriptor().getModifier()).thenReturn(Optional.of(this.modifier));
		when(this.rule.getRuleID()).thenReturn("FOO-RULE");
		when(this.modifier.getAnalyzerRuleID()).thenReturn(Optional.of("BAR-RULE")); // does not match the line above

		assertThrows(IllegalArgumentException.class, () -> analyzer.analyzeDocument(this.taskID, this.container,
				this.modifierCollector, this.vulnerabilityCollector));
	}

	/**
	 * Test method for {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
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
		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(rule1, rule2));

		assertThrows(IllegalArgumentException.class, () -> analyzer.analyzeDocument(this.taskID, this.container,
				this.modifierCollector, this.vulnerabilityCollector));
	}

	/**
	 * Test method for {@link org.x2vc.analysis.DocumentAnalyzer#verifyDocument}.
	 */
	@Test
	void testFollowUpPassWithFilter() {
		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(this.rule));

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
				"""));
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

		analyzer.analyzeDocument(this.taskID, this.container, this.modifierCollector, this.vulnerabilityCollector);

		verify(this.rule, times(3)).verifyNode(same(this.taskID), any(Node.class), same(this.source), any());
		verify(this.vulnerabilityCollector, times(3)).accept(any());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.DocumentAnalyzer#consolidateResults(java.net.URI, java.util.Set)}.
	 */
	@Test
	void testConsolidate() {
		final IAnalyzerRule rule1 = mock(IAnalyzerRule.class);
		when(rule1.getRuleID()).thenReturn("RULE-1");

		final IAnalyzerRule rule2 = mock(IAnalyzerRule.class);
		when(rule2.getRuleID()).thenReturn("RULE-2");

		final IAnalyzerRule rule3 = mock(IAnalyzerRule.class);
		when(rule3.getRuleID()).thenReturn("RULE-3");

		final IVulnerabilityCandidate candidate1a = mock(IVulnerabilityCandidate.class);
		when(candidate1a.getAnalyzerRuleID()).thenReturn("RULE-1");

		final IVulnerabilityCandidate candidate1b = mock(IVulnerabilityCandidate.class);
		when(candidate1b.getAnalyzerRuleID()).thenReturn("RULE-1");

		final IVulnerabilityCandidate candidate2a = mock(IVulnerabilityCandidate.class);
		when(candidate2a.getAnalyzerRuleID()).thenReturn("RULE-2");

		final IVulnerabilityCandidate candidate2b = mock(IVulnerabilityCandidate.class);
		when(candidate2b.getAnalyzerRuleID()).thenReturn("RULE-2");

		final IVulnerabilityReportSection section1a = mock(IVulnerabilityReportSection.class);
		final IVulnerabilityReportSection section1b = mock(IVulnerabilityReportSection.class);
		final IVulnerabilityReportSection section1c = mock(IVulnerabilityReportSection.class);
		final List<IVulnerabilityReportSection> sections1 = List.of(section1a, section1b, section1c);
		when(rule1.consolidateResults(Set.of(candidate1a, candidate1b))).thenReturn(sections1);

		final IVulnerabilityReportSection section2a = mock(IVulnerabilityReportSection.class);
		final List<IVulnerabilityReportSection> sections2 = List.of(section2a);
		when(rule2.consolidateResults(Set.of(candidate2a, candidate2b))).thenReturn(sections2);

		final IVulnerabilityReportSection section3a = mock(IVulnerabilityReportSection.class);
		final List<IVulnerabilityReportSection> sections3 = List.of(section3a);
		when(rule3.consolidateResults(Set.of())).thenReturn(sections3);

		final Set<IVulnerabilityCandidate> candidates = Set.of(candidate1a, candidate1b, candidate2a, candidate2b);

		final DocumentAnalyzer analyzer = new DocumentAnalyzer(Sets.newSet(rule1, rule2, rule3));

		final IVulnerabilityReport report = analyzer.consolidateResults(this.stylesheetURI, candidates);

		assertEquals(this.stylesheetURI, report.getStylesheetURI());
		assertEquals(List.of(section1a, section1b, section1c, section2a, section3a), report.getSections());
	}

}
