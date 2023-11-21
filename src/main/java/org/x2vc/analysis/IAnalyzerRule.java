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
package org.x2vc.analysis;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.nodes.Node;
import org.x2vc.analysis.rules.AbstractAttributeRule;
import org.x2vc.analysis.rules.AbstractElementRule;
import org.x2vc.analysis.rules.AbstractTextRule;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.IVulnerabilityReportSection;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * A rule that checks for a certain condition that might be exploited in order
 * to introduce an XSS vulnerability. This interface should not be implemented
 * directly, but rather by subclassing {@link AbstractElementRule},
 * {@link AbstractAttributeRule}, {@AbstractDataRule} or
 * {@link AbstractTextRule}.
 */
public interface IAnalyzerRule {

	/**
	 * @return a short technical designator to identify the rule, e.g. in log
	 *         entries.
	 */
	String getRuleID();

	/**
	 * This method is called once for each DOM node during the first pass and may
	 * emit any number of data modification requests to perform follow-up checks.
	 *
	 * @param node         the DOM node to check
	 * @param xmlContainer the input document container
	 * @param collector    a sink to send any resulting modification requests to
	 */
	void checkNode(Node node, IXMLDocumentContainer xmlContainer, Consumer<IDocumentModifier> collector);

	/**
	 * If the rule needs to examine the entire document for the follow-up pass, emit
	 * an empty set here. Otherwise, emit a set of XPath expressions and only the
	 * corresponding values will be presented to
	 * {@link #verifyNode(UUID, Node, IXMLDocumentContainer, Consumer)}
	 *
	 * @param xmlContainer the input document container
	 * @return a set of XPath expressions to identify the entities to filter, or an
	 *         empty set for no filtering
	 */
	Set<String> getElementSelectors(IXMLDocumentContainer xmlContainer);

	/**
	 * This method is called once for each DOM node - possibly filtered, see
	 * {@link #getElementSelectors(IXMLDocumentContainer)} - during the follow-up
	 * pass and may emit any number of vulnerability reports.
	 *
	 * @param taskID       the ID of the task being executed
	 * @param node         the DOM node to check
	 * @param xmlContainer the input document container
	 * @param collector    a sink to send any resulting vulnerability reports to
	 */
	void verifyNode(UUID taskID, Node node, IXMLDocumentContainer xmlContainer,
			Consumer<IVulnerabilityCandidate> collector);

	/**
	 * Combines the {@link IVulnerabilityCandidate}s generated using
	 * {@link #verifyNode(UUID, Node, IXMLDocumentContainer, Consumer)} and produces
	 * one or more report sections. Note that the input set may be empty, in which
	 * case a "no findings" section is supposed to be generated.
	 *
	 * @param schema
	 *
	 * @param candidates
	 * @return the sections to add to the report
	 */
	List<IVulnerabilityReportSection> consolidateResults(IXMLSchema schema, Set<IVulnerabilityCandidate> candidates);

}
