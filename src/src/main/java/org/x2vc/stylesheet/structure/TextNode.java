package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Standard implementation of {@link ITextNode}.
 */
public class TextNode extends AbstractStructureTreeNode implements ITextNode {

	private static final long serialVersionUID = -4649767702919548805L;
	private String text;

	private TextNode(Builder builder) {
		super(builder.parentStructure);
		this.text = builder.text.toString();
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

	/**
	 * Builder to build {@link TextNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private StringBuilder text = new StringBuilder();

		/**
		 * Create a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 */
		public Builder(IStylesheetStructure parentStructure) {
			this.parentStructure = parentStructure;
		}

		/**
		 * Builder method for text parameter.
		 *
		 * @param text field to set
		 * @return builder
		 */
		public Builder withText(String text) {
			checkNotNull(text);
			this.text.append(text);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public TextNode build() {
			return new TextNode(this);
		}
	}

}
