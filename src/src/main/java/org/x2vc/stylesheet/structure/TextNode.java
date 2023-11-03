package org.x2vc.stylesheet.structure;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

/**
 * Standard implementation of {@link ITextNode}.
 */
public class TextNode extends AbstractStructureTreeNode implements ITextNode {

	private String text;

	private TextNode(Builder builder) {
		super(builder.parentStructure);
		this.text = builder.text.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.text);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TextNode other = (TextNode) obj;
		return Objects.equals(this.text, other.text);
	}

	@Override
	public String getText() {
		return this.text;
	}

	/**
	 * Create a new builder instance.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure) {
		return new Builder(parentStructure);
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
		private Builder(IStylesheetStructure parentStructure) {
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
