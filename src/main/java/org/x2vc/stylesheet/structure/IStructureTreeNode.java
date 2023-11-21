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
 * A common interface to encompass all possible elements of the XSLT structure tree. We distinguish between XSLT
 * elements, other XML elements and text nodes here. XSLT elements are further divided into parameters (xsl:param,
 * xsl:with-param), sorting instructions (xsl:sort) and other directives.
 */
public interface IStructureTreeNode {

	/**
	 * @return the structure object this element belongs to
	 */
	IStylesheetStructure getParentStructure();

}
