package org.x2vc.stylesheet.structure;

import java.util.Optional;

import org.x2vc.utilities.PolymorphLocation;

import com.google.common.collect.ImmutableList;

/**
 * This object is used to represent both formal parameters (xsl:param) and
 * actual parameters (xsl:with-param).
 */
public interface IXSLTParameterNode extends IStructureTreeNode {

	/**
	 * @return the name of the parameter
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
	 * @return the value of the select attribute if it exists
	 */
	Optional<String> getSelection();

	/**
	 * @return the template elements, i.e. the contents of the parameter element.
	 *         This list may be empty.
	 */
	ImmutableList<IStructureTreeNode> getChildElements();

}
