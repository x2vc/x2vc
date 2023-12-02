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

import org.x2vc.utilities.xml.ITagInfo;

import com.google.common.collect.ImmutableList;

/**
 * An XML element node contained in an {@link IStylesheetStructure} tree. This can either be a non-XSLT {@link IXMLNode}
 * or one of the XSLT node types like {@link IXSLTDirectiveNode}, {@link IXSLTParameterNode} or {@link IXSLTSortNode}.
 */
public interface IElementNode extends IStructureTreeNode {

	/**
	 * Returns the child elements of the XML element. This list may be empty.
	 *
	 * @return the child elements of the XML element
	 */
	ImmutableList<IStructureTreeNode> getChildElements();

	/**
	 * Returns the information about the corresponding tag - most notably the location of the tag. For non-empty-element
	 * tags, this method returns the start tag.
	 *
	 * @return the information about the corresponding tag
	 */
	ITagInfo getTagInformation();

}
