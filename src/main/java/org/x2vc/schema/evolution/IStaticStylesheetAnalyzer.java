package org.x2vc.schema.evolution;

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

import java.util.UUID;
import java.util.function.Consumer;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

/**
 * This component uses the static stylesheet structure to identify missing parts of the associated schema.
 */
public interface IStaticStylesheetAnalyzer {

	/**
	 * Analyze the stylesheet structure and check for potential missing schema elements.
	 *
	 * @param taskID            the ID of the task being executed
	 * @param stylesheet        the stylesheet information
	 * @param schema            the schema to check
	 * @param modifierCollector a sink to handle the modification requests issued by the analyzer
	 */
	void analyze(UUID taskID, IStylesheetStructure stylesheet, IXMLSchema schema,
			Consumer<ISchemaModifier> modifierCollector);

}
