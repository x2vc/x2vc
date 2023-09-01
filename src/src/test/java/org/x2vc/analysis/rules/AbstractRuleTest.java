package org.x2vc.analysis.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.x2vc.analysis.results.IVulnerabilityCandidate;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableSet;

class AbstractRuleTest {

	private AbstractRule rule = new AbstractRule() {
		@Override
		public String getRuleID() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkNode(Node node, IXMLDocumentContainer container, Consumer<IDocumentModifier> collector) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ImmutableSet<String> getElementSelectors(IXMLDocumentContainer container) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void verifyNode(UUID taskID, Node node, IXMLDocumentContainer container,
				Consumer<IVulnerabilityCandidate> collector) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Test method for
	 * {@link org.x2vc.analysis.rules.AbstractRule#getPathToNode(org.jsoup.nodes.Node)}.
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

}
