package org.x2vc.stylesheet.structure;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * A non-XSLT XML element contained in an {@link IStylesheetStructure} tree.
 */
public interface IXMLNode extends IStructureTreeNode {

	/**
	 * @return the qualified name of the non-XSLT XML element
	 */
	QName getName();

	/**
	 * @return the attributes of the non-XSLT XML element
	 */
	ImmutableMap<QName, String> getAttributes();

	/**
	 * @return the child elements of the non-XSLT XML element
	 */
	ImmutableList<IStructureTreeNode> getChildElements();
}
