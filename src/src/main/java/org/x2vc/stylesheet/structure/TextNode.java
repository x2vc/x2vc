package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Standard implementation of {@link ITextNode}.
 */
public class TextNode extends AbstractStructureTreeNode implements ITextNode {

	private static final long serialVersionUID = -4649767702919548805L;
	private String text;

	/**
	 * Constructor for a text node with a parent element.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @param text            the text contained in the element
	 */
	public TextNode(IStylesheetStructure parentStructure, String text) {
		super(parentStructure);
		checkNotNull(text);
		this.text = text;
	}

	@Override
	public NodeType getType() {
		return NodeType.TEXT;
	}

	@Override
	public boolean isText() {
		return true;
	}

	@Override
	public ITextNode asText() throws IllegalStateException {
		return this;
	}

	@Override
	public String getText() {
		return this.text;
	}

}
