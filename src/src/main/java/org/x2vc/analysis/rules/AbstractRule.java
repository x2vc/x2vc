package org.x2vc.analysis.rules;

import java.util.Deque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;

import com.google.common.collect.Lists;

/**
 * Base class for all {@link IAnalyzerRule} implementations that provides some
 * common implementations.
 */
public abstract class AbstractRule implements IAnalyzerRule {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * Determines an XPath selector string to make it easier to relocate an element
	 * inside the HTML document later.
	 *
	 * @param node the node in question
	 * @return the selection path
	 */
	String getPathToNode(Node node) {
		final Deque<String> pathElements = Lists.newLinkedList();
		Node nextNode = node;
		while (nextNode != null) {
			if (nextNode instanceof Document) {
				// ignore document node
			} else if (nextNode instanceof final Element element) {
				pathElements.addFirst(element.nodeName());
			} else {
				if (!pathElements.isEmpty()) {
					logger.warn("non-element node as intermediate node encountered - check situation!");
				}
			}
			nextNode = nextNode.parent();
		}
		final StringBuilder result = new StringBuilder();
		pathElements.forEach(e -> result.append("/" + e));
		return result.toString();
	}

}
