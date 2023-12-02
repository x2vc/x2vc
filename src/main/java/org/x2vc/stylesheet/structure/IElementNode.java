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
