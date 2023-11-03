package org.x2vc.stylesheet.structure;

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

/**
 * This component takes an the extended XSLT stylesheet and derives a structure
 * information. It acts as a factory for the stylesheet structure information.
 * In order to use the same message IDs, it has to be applied to the extended
 * stylesheet.
 */
public interface IStylesheetStructureExtractor {

	/**
	 * @param source the extended source of the stylesheet
	 * @return the structure information
	 */
	IStylesheetStructure extractStructure(String source);
}
