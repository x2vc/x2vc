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
package org.x2vc.stylesheet.structure;


import java.util.function.Function;

/**
 * Tool to create {@link IStylesheetStructure} instances for testing
 */
public class StylesheetStructureTestFactory {

	/**
	 * @param rootNodeProvider a function that creates the root node of a structure
	 * @return an {@link IStylesheetStructure} instance wrapping the root node
	 *
	 */
	public static IStylesheetStructure createStylesheetStructure(
			Function<IStylesheetStructure, IXSLTDirectiveNode> rootNodeProvider) {
		StylesheetStructure structure = new StylesheetStructure();
		structure.setRootNode(rootNodeProvider.apply(structure));
		return structure;

	}

}
