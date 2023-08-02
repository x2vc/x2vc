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
import org.x2vc.xmldoc.IXMLDocumentContainer;
import org.x2vc.xmldoc.IXMLDocumentDescriptor;

@ExtendWith(MockitoExtension.class)
class DocumentAnalyzerTest {

	@Mock
	private IAnalyzerRule rule;

	private DocumentAnalyzer analyzer;

	@Mock
	private Consumer<IRuleDataModifier> modifierCollector;

	@Mock
	private IHTMLDocumentContainer container;

	@Mock
	private IXMLDocumentContainer source;

	@Mock
	private IXMLDocumentDescriptor descriptor;

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
	void testAnalyzeDocument() {
		when(this.descriptor.isMutated()).thenReturn(false);
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
		doAnswer(invocation -> {
			final Consumer<IRuleDataModifier> argCollector = invocation.getArgument(2);
			argCollector.accept(mock(IRuleDataModifier.class));
			return null;
		}).when(this.rule).checkNode(any(Node.class), same(this.descriptor), any());
		this.analyzer.analyzeDocument(this.container, this.modifierCollector);
		verify(this.rule, times(29)).checkNode(any(Node.class), same(this.descriptor), any());
		verify(this.modifierCollector, times(29)).accept(any());
	}

}
