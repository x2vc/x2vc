package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IXSLTParameterNode}.
 */
public class XSLTParameterNode extends AbstractStructureTreeNode implements IXSLTParameterNode {

	private static final long serialVersionUID = 2146219899310217992L;
	private String name;
	private String selection;
	private ImmutableList<IStructureTreeNode> childElements;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	private XSLTParameterNode(Builder builder) {
		super(builder.parentStructure);
		this.name = builder.name;
		this.selection = builder.selection;
		this.childElements = ImmutableList.copyOf(builder.childElements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.childElements, this.name, this.selection);
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
		final XSLTParameterNode other = (XSLTParameterNode) obj;
		return Objects.equals(this.childElements, other.childElements) && Objects.equals(this.name, other.name)
				&& Objects.equals(this.selection, other.selection);
	}

	@Override
	public NodeType getType() {
		return NodeType.XSLT_PARAM;
	}

	@Override
	public boolean isXSLTParameter() {
		return true;
	}

	@Override
	public IXSLTParameterNode asParameter() throws IllegalStateException {
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Optional<String> getSelection() {
		return Optional.ofNullable(this.selection);
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	/**
	 * Builder to build {@link XSLTParameterNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private String name;
		private String selection;
		private List<IStructureTreeNode> childElements = new ArrayList<>();

		/**
		 * Creates a new builder
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param name            the name of the element
		 */
		public Builder(IStylesheetStructure parentStructure, String name) {
			checkNotNull(parentStructure);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.name = name;
		}

		/**
		 * Sets the selection parameter of the builder.
		 *
		 * @param selection the select parameter
		 * @return builder
		 */
		public Builder withSelection(String selection) {
			checkNotNull(selection);
			this.selection = selection;
			return this;
		}

		/**
		 * Adds a child element to the builder.
		 *
		 * @param element the child element to add
		 * @return builder
		 */
		public Builder addChildElement(IStructureTreeNode element) {
			checkNotNull(element);
			this.childElements.add(element);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XSLTParameterNode build() {
			return new XSLTParameterNode(this);
		}
	}

}
