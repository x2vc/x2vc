package org.x2vc.stylesheet.structure;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.x2vc.utilities.PolymorphLocation;

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
	 * @return the location the starting element was found
	 */
	Optional<PolymorphLocation> getStartLocation();

	/**
	 * @return the location the closing element was found
	 */
	Optional<PolymorphLocation> getEndLocation();

	/**
	 * @return the attributes of the non-XSLT XML element
	 */
	ImmutableMap<QName, String> getAttributes();

	/**
	 * @return the child elements of the non-XSLT XML element
	 */
	ImmutableList<IStructureTreeNode> getChildElements();
}
