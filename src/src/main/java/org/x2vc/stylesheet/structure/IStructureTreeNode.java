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
