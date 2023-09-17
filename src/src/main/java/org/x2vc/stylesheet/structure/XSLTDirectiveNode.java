package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IXSLTDirectiveNode}.
 */
public class XSLTDirectiveNode extends AbstractStructureTreeNode implements IXSLTDirectiveNode {

	private String name;
	private ImmutableMap<String, String> xsltAttributes;
	private ImmutableMap<QName, String> otherAttributes;
	private ImmutableList<IStructureTreeNode> childElements;
	private ImmutableList<IXSLTDirectiveNode> childDirectives;
	private ImmutableList<IXSLTParameterNode> formalParameters;
	private ImmutableList<IXSLTParameterNode> actualParameters;
	private ImmutableList<IXSLTSortNode> sorting;

	private XSLTDirectiveNode(Builder builder) {
		super(builder.parentStructure);
		this.name = builder.name;
		this.xsltAttributes = ImmutableMap.copyOf(builder.xsltAttributes);
		this.otherAttributes = ImmutableMap.copyOf(builder.otherAttributes);
		this.childElements = ImmutableList.copyOf(builder.childElements);
		this.formalParameters = ImmutableList.copyOf(builder.formalParameters);
		this.actualParameters = ImmutableList.copyOf(builder.actualParameters);
		this.sorting = ImmutableList.copyOf(builder.sorting);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.actualParameters, this.childElements, this.formalParameters,
				this.name, this.otherAttributes, this.sorting, this.xsltAttributes);
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
		final XSLTDirectiveNode other = (XSLTDirectiveNode) obj;
		return Objects.equals(this.actualParameters, other.actualParameters)
				&& Objects.equals(this.childElements, other.childElements)
				&& Objects.equals(this.formalParameters, other.formalParameters)
				&& Objects.equals(this.name, other.name) && Objects.equals(this.otherAttributes, other.otherAttributes)
				&& Objects.equals(this.sorting, other.sorting)
				&& Objects.equals(this.xsltAttributes, other.xsltAttributes);
	}

	@Override
	public NodeType getType() {
		return NodeType.XSLT_DIRECTIVE;
	}

	@Override
	public boolean isXSLTDirective() {
		return true;
	}

	@Override
	public IXSLTDirectiveNode asDirective() throws IllegalStateException {
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ImmutableMap<String, String> getXSLTAttributes() {
		return this.xsltAttributes;
	}

	@Override
	public ImmutableMap<QName, String> getOtherAttributes() {
		return this.otherAttributes;
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	@Override
	public ImmutableList<IXSLTDirectiveNode> getChildDirectives() {
		if (this.childDirectives == null) {
			this.childDirectives = ImmutableList.copyOf(
					this.childElements.stream().filter(e -> e.isXSLTDirective()).map(e -> e.asDirective()).iterator());
		}
		return this.childDirectives;
	}

	@Override
	public ImmutableList<IXSLTParameterNode> getFormalParameters() {
		return this.formalParameters;
	}

	@Override
	public ImmutableList<IXSLTParameterNode> getActualParameters() {
		return this.actualParameters;
	}

	@Override
	public ImmutableList<IXSLTSortNode> getSorting() {
		return this.sorting;
	}

	/**
	 * Builder to build {@link XSLTDirectiveNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private String name;
		private Map<String, String> xsltAttributes = new HashMap<>();
		private Map<QName, String> otherAttributes = new HashMap<>();
		private List<IStructureTreeNode> childElements = new ArrayList<>();
		private List<IXSLTParameterNode> formalParameters = new ArrayList<>();
		private List<IXSLTParameterNode> actualParameters = new ArrayList<>();
		private List<IXSLTSortNode> sorting = new ArrayList<>();

		/**
		 * Create a builder instance.
		 *
		 * @param parentStructure the parent {@link IStylesheetStructure}
		 * @param name            the name of the directive
		 */
		public Builder(IStylesheetStructure parentStructure, String name) {
			checkNotNull(parentStructure);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.name = name;
		}

		/**
		 * Adds an XSLT attribute to the builder.
		 *
		 * @param name  the name of the attribute
		 * @param value the value of the attribute
		 * @return builder
		 */
		public Builder addXSLTAttribute(String name, String value) {
			checkNotNull(name);
			checkNotNull(value);
			this.xsltAttributes.put(name, value);
			return this;
		}

		/**
		 * Adds a non-XSLT attribute to the builder.
		 *
		 * @param name  the name of the attribute
		 * @param value the value of the attribute
		 * @return builder
		 */
		public Builder addOtherAttribute(QName name, String value) {
			checkNotNull(name);
			checkNotNull(value);
			this.otherAttributes.put(name, value);
			return this;
		}

		/**
		 * Adds a child element to the builder.
		 *
		 * @param childElement the child element
		 * @return builder
		 */
		public Builder addChildElement(IStructureTreeNode childElement) {
			checkNotNull(childElement);
			this.childElements.add(childElement);
			return this;
		}

		/**
		 * Adds a formal parameter (xsl:param) to the builder.
		 *
		 * @param formalParameter the parameter to add
		 * @return builder
		 */
		public Builder addFormalParameter(IXSLTParameterNode formalParameter) {
			checkNotNull(formalParameter);
			this.formalParameters.add(formalParameter);
			return this;
		}

		/**
		 * Adds an actual parameter (xsl:with-param) to the builder.
		 *
		 * @param actualParameter the parameter to add
		 * @return builder
		 */
		public Builder addActualParameter(IXSLTParameterNode actualParameter) {
			checkNotNull(actualParameter);
			this.actualParameters.add(actualParameter);
			return this;
		}

		/**
		 * Adds a sorting specification (xsl:sort) to the builder.
		 *
		 * @param sorting the sorting specification to add
		 * @return builder
		 */
		public Builder addSorting(IXSLTSortNode sorting) {
			checkNotNull(sorting);
			this.sorting.add(sorting);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XSLTDirectiveNode build() {
			return new XSLTDirectiveNode(this);
		}
	}

}
