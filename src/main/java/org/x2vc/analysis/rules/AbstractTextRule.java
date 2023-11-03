package org.x2vc.analysis.rules;

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

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * An {@link IAnalyzerRule} that performs a check on text nodes. Note that blank
 * text nodes (see {@link TextNode#isBlank()}) will be skipped without further
 * notice.
 */
public abstract class AbstractTextRule extends AbstractRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public final void checkNode(Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		if (node instanceof final TextNode textNode && (!textNode.isBlank())
				&& (isApplicableTo(textNode, xmlContainer))) {
			performCheckOn(textNode, xmlContainer, collector);
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the node at all.
	 *
	 * @param textNode             the text node to check
	 * @param xmlContainer the input document container
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(TextNode textNode, IXMLDocumentContainer xmlContainer);

	/**
	 * Performs the actual check on the text node in question.
	 *
	 * @param textNode             the text node to check
	 * @param xmlContainer the input document container
	 * @param collector            a sink to send any resulting modification
	 *                             requests to
	 */
	protected abstract void performCheckOn(TextNode textNode, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector);

}
