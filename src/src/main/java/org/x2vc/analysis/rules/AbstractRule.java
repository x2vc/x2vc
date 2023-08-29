package org.x2vc.analysis.rules;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Deque;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IModifierPayload;
import org.x2vc.xml.document.IXMLDocumentContainer;

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

	/**
	 * Retrieves the {@link IModifierPayload} of an {@link IDocumentModifier} used
	 * to generate a document, checking its type and casting it in the process.
	 *
	 * @param <T>
	 * @param xmlContainer
	 * @param expectedType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends IModifierPayload> T getPayloadChecked(IXMLDocumentContainer xmlContainer,
			Class<T> expectedType) {
		final Optional<IDocumentModifier> oModifier = xmlContainer.getDocumentDescriptor().getModifier();
		checkArgument(oModifier.isPresent());
		final Optional<IModifierPayload> oPayload = oModifier.get().getPayload();
		checkArgument(oPayload.isPresent());
		if (expectedType.isInstance(oPayload.get())) {
			return (T) oPayload.get();
		} else {
			throw logger.throwing(new IllegalArgumentException(
					String.format("payload of document modifier has the wrong type %s, expected %s",
							oPayload.get().getClass().getName(), expectedType.getName())));
		}

	}

}
