package org.x2vc.stylesheet.structure;

/**
 * A simple text node contained in an {@link IStylesheetStructure} tree.
 */
public interface ITextNode extends IStructureTreeNode {

	/**
	 * @return the contents of the text node
	 */
	String getText();

}
