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


import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * An {@link IAnalyzerRule} that performs a check on an attribute level.
 */
public abstract class AbstractAttributeRule extends AbstractElementRule {

	private static final Logger logger = LogManager.getLogger();

	@Override
	protected final void performCheckOn(Element element, IXMLDocumentContainer xmlContainer,
			Consumer<IDocumentModifier> collector) {
		logger.traceEntry();
		for (final Attribute attribute : element.attributes()) {
			if (isApplicableTo(element, attribute, xmlContainer)) {
				performCheckOn(element, attribute, xmlContainer, collector);
			}
		}
		logger.traceExit();
	}

	/**
	 * Determines whether the entire rule can be applied to the attribute in
	 * question.
	 *
	 * @param element              the element the attribute belongs to
	 * @param attribute            the attribute to check
	 * @param xmlContainer the input document container
	 * @return <code>true</code> if the rule should be checked further
	 */
	protected abstract boolean isApplicableTo(Element element, Attribute attribute,
			IXMLDocumentContainer xmlContainer);

	/**
	 * Performs the actual check on the element in question.
	 *
	 * @param element              the element the attribute belongs to
	 * @param attribute            the attribute to check
	 * @param xmlContainer the input document container
	 * @param collector            a sink to send any resulting modification
	 *                             requests to
	 */
	protected abstract void performCheckOn(Element element, Attribute attribute,
			IXMLDocumentContainer xmlContainer, Consumer<IDocumentModifier> collector);

}
