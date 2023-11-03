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

import java.util.Optional;

import javax.xml.namespace.QName;

import org.x2vc.utilities.PolymorphLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.sf.saxon.om.NamespaceUri;

/**
 * An XSLT element contained in an {@link IStylesheetStructure} tree. These structure elements can represent most of the
 * valid XSLT elements. Note that <code>xsl:param</code>, <code>xsl:sort</code> and <code>xsl:with-param</code> elements
 * are not part of the tree structure but kept in separate collections like attributes. Also note that for
 * <code>xsl:template</code> elements, a specialized interface {@link IXSLTTemplateNode} exists.
 */
public interface IXSLTDirectiveNode extends IStructureTreeNode {

	/**
	 * @return the name of the element, like "apply-templates"
	 */
	String getName();

	/**
	 * @return the location the starting element was found
	 */
	Optional<PolymorphLocation> getStartLocation();

	/**
	 * @return the location the closing element was found
	 */
	Optional<PolymorphLocation> getEndLocation();

	/**
	 * @return the namespaces defined for the node, organized by prefix
	 */
	ImmutableMap<String, NamespaceUri> getNamespaces();

	/**
	 * @return the XSLT attributes of the element
	 */
	ImmutableMap<String, String> getXSLTAttributes();

	/**
	 * @param name
	 * @return the value of the XSLT attribute with the name provided, if it exists
	 */
	Optional<String> getXSLTAttribute(String name);

	/**
	 * @return the non-XSLT attributes of the element
	 */
	ImmutableMap<QName, String> getOtherAttributes();

	/**
	 * @return the elements contained within the current element. May be empty. Note that xsl:param, xsl:sort and
	 *         xsl:with-param elements are not part of the tree structure but kept in separate collections like
	 *         attributes.
	 */
	ImmutableList<IStructureTreeNode> getChildElements();

	/**
	 * @return the elements contained within the element, filtered to only contain XSLT directives.
	 * @see #getChildElements()
	 */
	ImmutableList<IXSLTDirectiveNode> getChildDirectives();

	/**
	 * @return the formal parameters of an element, specified using xsl:param directives
	 */
	ImmutableList<IXSLTParameterNode> getFormalParameters();

	/**
	 * @return the actual parameters of an element, specified using xsl:with-param directives
	 */
	ImmutableList<IXSLTParameterNode> getActualParameters();

	/**
	 * @return the sorting specifications (xsl:sort) set for the directive, if any
	 */
	ImmutableList<IXSLTSortNode> getSorting();

}
