package org.x2vc.stylesheet.structure;

import java.util.Optional;

/**
 * A sorting instruction (xsl:sort) within the XSLT structure information.
 */
public interface IXSLTSortNode extends IStructureTreeNode {

	/**
	 * @return the expression used to sort the elements by (attribute select of
	 *         xsl:sort)
	 */
	Optional<String> getSortingExpression();

	/**
	 * @return the sorting language (attribute lang of xsl:sort)
	 */
	Optional<String> getLanguage();

	/**
	 * @return the data type (attribute data-type of xsl:sort)
	 */
	Optional<String> getDataType();

	/**
	 * @return the sorting order (ascending or descending, attribute order of
	 *         xsl:sort)
	 */
	Optional<String> getSortOrder();

	/**
	 * @return the case handling order (upper-first or lower-first, attribute
	 *         case-order of xsl:sort)
	 */
	Optional<String> getCaseOrder();

}