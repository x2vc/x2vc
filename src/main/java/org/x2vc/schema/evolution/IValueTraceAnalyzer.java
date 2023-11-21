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
package org.x2vc.schema.evolution;


import java.util.UUID;
import java.util.function.Consumer;

import org.x2vc.processor.IHTMLDocumentContainer;

/**
 * This component uses the trace output generated by the XSLT processor to identify missing parts of the input document
 * schema.
 */
public interface IValueTraceAnalyzer {

	/**
	 * Analyze a HTML document and check for potential missing schema elements.
	 *
	 * @param taskID            the ID of the task being executed
	 * @param htmlContainer     the document to analyze
	 * @param modifierCollector a sink to handle the modification requests issued by the analyzer
	 */
	void analyzeDocument(UUID taskID, IHTMLDocumentContainer htmlContainer,
			Consumer<ISchemaModifier> modifierCollector);

}
