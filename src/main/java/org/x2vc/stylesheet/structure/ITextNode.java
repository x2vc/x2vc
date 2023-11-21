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


/**
 * A simple text node contained in an {@link IStylesheetStructure} tree.
 */
public interface ITextNode extends IStructureTreeNode {

	/**
	 * @return the contents of the text node
	 */
	String getText();

}
