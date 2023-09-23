package org.x2vc.stylesheet.structure;

/**
 * A common interface to encompass all possible elements of the XSLT structure tree. We distinguish between XSLT
 * elements, other XML elements and text nodes here. XSLT elements are further divided into parameters (xsl:param,
 * xsl:with-param), sorting instructions (xsl:sort) and other directives.
 */
public interface IStructureTreeNode {

	/**
	 * @return <code>true</code> if the element is an XSLT directive that can be cast to {@link IXSLTDirectiveNode}
	 */
	boolean isXSLTDirective();

	/**
	 * @return the element reference as an {@link IXSLTDirectiveNode} if it is of the correct type
	 * @throws IllegalStateException if the type of the tree node does not match
	 */
	IXSLTDirectiveNode asDirective() throws IllegalStateException;

	/**
	 * @return <code>true</code> if the element is an XSLT parameter that can be cast to {@link IXSLTParameterNode}
	 */
	boolean isXSLTParameter();

	/**
	 * @return the element reference as an {@link IXSLTParameterNode} if it is of the correct type
	 * @throws IllegalStateException if the type of the tree node does not match
	 */
	IXSLTParameterNode asParameter() throws IllegalStateException;

	/**
	 * @return <code>true</code> if the element is an XSLT sorting instruction that can be cast to {@link IXSLTSortNode}
	 */
	boolean isXSLTSort();

	/**
	 * @return the element reference as an {@link IXSLTSortNode} if it is of the correct type
	 * @throws IllegalStateException if the type of the tree node does not match
	 */
	IXSLTSortNode asSort() throws IllegalStateException;

	/**
	 * @return <code>true</code> if the element is a non-XSLT XML element and can be cast to {@link IXMLNode}
	 */
	boolean isXML();

	/**
	 * @return the element reference as an {@link IXMLNode} if it is of the correct type
	 * @throws IllegalStateException if the type of the tree node does not match
	 */
	IXMLNode asXML() throws IllegalStateException;

	/**
	 * @return <code>true</code> if the element is a simple text node and can be cast to {@link ITextNode}
	 */
	boolean isText();

	/**
	 * @return the element reference as an {@link ITextNode} if it is of the correct type
	 * @throws IllegalStateException if the type of the tree node does not match
	 */
	ITextNode asText() throws IllegalStateException;

	/**
	 * @return the structure object this element belongs to
	 */
	IStylesheetStructure getParentStructure();

}
