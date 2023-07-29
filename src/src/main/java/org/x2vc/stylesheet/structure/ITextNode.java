package org.x2vc.stylesheet.structure;

import org.x2vc.stylesheet.IStylesheetStructure;

/**
 * A simple text node contained in an {@link IStylesheetStructure} tree.
 */
public interface ITextNode extends IStructureTreeNode {

	/**
	 * @return the contents of the text node
	 */
	String getText();

}
