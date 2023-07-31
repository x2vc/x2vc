package org.x2vc.stylesheet.structure;

import java.util.function.Function;

/**
 * Tool to create {@link IStylesheetStructure} instances for testing
 */
public class StylesheetStructureTestFactory {

	/**
	 * @param rootNodeProvider a function that creates the root node of a structure
	 * @return an {@link IStylesheetStructure} instance wrapping the root node
	 *
	 */
	public static IStylesheetStructure createStylesheetStructure(
			Function<IStylesheetStructure, IXSLTDirectiveNode> rootNodeProvider) {
		StylesheetStructure structure = new StylesheetStructure();
		structure.setRootNode(rootNodeProvider.apply(structure));
		return structure;

	}

}
