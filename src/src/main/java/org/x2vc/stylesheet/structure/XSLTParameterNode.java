package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.x2vc.stylesheet.IStylesheetStructure;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IXSLTParameterNode}.
 */
public class XSLTParameterNode extends AbstractStructureTreeNode implements IXSLTParameterNode {

	private String name;
	private String selection;
	private ImmutableList<IStructureTreeNode> childElements;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	private XSLTParameterNode(Builder builder) {
		super(builder.parentStructure, builder.parentElement);
		this.name = builder.name;
		this.selection = builder.selection;
		this.childElements = ImmutableList.copyOf(builder.childElements);
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
	public static final class Builder {
		private IStylesheetStructure parentStructure;
		private IStructureTreeNode parentElement;
		private String name;
		private String selection;
		private List<IStructureTreeNode> childElements = new ArrayList<>();

		/**
		 * Creates a new builder
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param parentElement   the parent element
		 * @param name            the name of the element
		 */
		public Builder(IStylesheetStructure parentStructure, IStructureTreeNode parentElement, String name) {
			checkNotNull(parentStructure);
			checkNotNull(parentElement);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.parentElement = parentElement;
			this.name = name;
		}

		/**
		 * Builder method for selection parameter.
		 *
		 * @param selection field to set
		 * @return builder
		 */
		public Builder withSelection(String selection) {
			checkNotNull(selection);
			this.selection = selection;
			return this;
		}

		/**
		 * Builder method for childElements parameter.
		 *
		 * @param element field to set
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
