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
package org.x2vc.utilities;


import java.util.UUID;

import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.IDocumentRequest;

/**
 * This component is used to write certain objects to the file system for debugging purposes.
 */
public interface IDebugObjectWriter {

	/**
	 * Writes an {@link IDocumentRequest} to a file.
	 *
	 * @param taskID  a common task ID to keep all the output files belonging to a single task together
	 * @param request
	 */
	void writeRequest(UUID taskID, IDocumentRequest request);

	/**
	 * Writes an XML document kept in an {@link IXMLDocumentContainer} to a file.
	 *
	 * @param taskID      a common task ID to keep all the output files belonging to a single task together
	 * @param xmlDocument
	 */
	void writeXMLDocument(UUID taskID, IXMLDocumentContainer xmlDocument);

	/**
	 * Writes a HTML document kept in an {@link IHTMLDocumentContainer} to a file.
	 *
	 * @param taskID       a common task ID to keep all the output files belonging to a single task together
	 * @param htmlDocument
	 */
	void writeHTMLDocument(UUID taskID, IHTMLDocumentContainer htmlDocument);

	/**
	 * Writes a {@link IVulnerabilityCandidate} to a file.
	 *
	 * @param taskID                 a common task ID to keep all the output files belonging to a single task together
	 * @param candidateNumber
	 * @param vulnerabilityCandidate
	 */
	void writeVulnerabilityCandidate(UUID taskID, int candidateNumber, IVulnerabilityCandidate vulnerabilityCandidate);

	/**
	 * Writes an {@link IXMLSchema} to a file.
	 *
	 * @param taskID a common task ID to keep all the output files belonging to a single task together
	 * @param schema
	 */
	void writeSchema(UUID taskID, IXMLSchema schema);

	/**
	 * Writes a set of {@link ISchemaModifier}s to a file
	 *
	 * @param taskID            a common task ID to keep all the output files belonging to a single task together
	 * @param modifierCollector
	 */
	void writeSchemaModifiers(UUID taskID, ISchemaModifierCollector modifierCollector);

}
