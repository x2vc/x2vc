package org.x2vc.stylesheet.structure;

import java.util.Optional;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * An XSLT element contained in an {@link IStylesheetStructure} tree. These
 * structure elements can represent most of the valid XSLT elements. Note that
 * xsl:param, xsl:sort and xsl:with-param elements are not part of the tree
 * structure but kept in separate collections like attributes.
 */
public interface IXSLTDirectiveNode extends IStructureTreeNode {

	/**
	 * @return the name of the element, like "apply-templates"
	 */
	String getName();

	/**
	 * @return the XSLT attributes of the element
	 */
	ImmutableMap<String, String> getXSLTAttributes();

	/**
	 * @return the non-XSLT attributes of the element
	 */
	ImmutableMap<QName, String> getOtherAttributes();

	/**
	 * @return the trace ID if any is set
	 */
	Optional<Integer> getTraceID();

	/**
	 * @return the elements contained within the current element. May be empty. Note
	 *         that xsl:param, xsl:sort and xsl:with-param elements are not part of
	 *         the tree structure but kept in separate collections like attributes.
	 */
	ImmutableList<IStructureTreeNode> getChildElements();

	/**
	 * @return the elements contained within the element, filtered to only contain
	 *         XSLT directives.
	 * @see #getChildElements()
	 */
	ImmutableList<IXSLTDirectiveNode> getChildDirectives();

	/**
	 * @return the formal parameters of an element, specified using xsl:param
	 *         directives
	 */
	ImmutableList<IXSLTParameterNode> getFormalParameters();

	/**
	 * @return the actual parameters of an element, specified using xsl:with-param
	 *         directives
	 */
	ImmutableList<IXSLTParameterNode> getActualParameters();

	/**
	 * @return the sorting specifications (xsl:sort) set for the directive, if any
	 */
	ImmutableList<IXSLTSortNode> getSorting();

}
