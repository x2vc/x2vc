package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.x2vc.common.ExtendedXSLTConstants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IXSLTDirectiveNode}.
 */
public class XSLTDirectiveNode extends AbstractStructureTreeNode implements IXSLTDirectiveNode {

	private static final long serialVersionUID = 767926132327224364L;
	private String name;
	private ImmutableMap<String, String> xsltAttributes;
	private ImmutableMap<QName, String> otherAttributes;
	private transient boolean traceIDExtracted = false;
	private transient Integer traceID;
	private ImmutableList<IStructureTreeNode> childElements;
	private transient ImmutableList<IXSLTDirectiveNode> childDirectives;
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
	public Optional<Integer> getTraceID() {
		if (!this.traceIDExtracted) {
			if (this.otherAttributes.containsKey(ExtendedXSLTConstants.ATTRIBUTE_NAME_TRACE_ID)) {
				this.traceID = Integer
						.parseInt(this.otherAttributes.get(ExtendedXSLTConstants.ATTRIBUTE_NAME_TRACE_ID));
			}
			this.traceIDExtracted = true;
		}
		return Optional.ofNullable(this.traceID);
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
		 * Builder method for xsltAttributes parameter.
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
		 * Builder method for otherAttributes parameter.
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
		 * Builder method for childElements parameter.
		 *
		 * @param childElement field to set
		 * @return builder
		 */
		public Builder addChildElement(IStructureTreeNode childElement) {
			checkNotNull(childElement);
			this.childElements.add(childElement);
			return this;
		}

		/**
		 * Builder method for formalParameters parameter.
		 *
		 * @param formalParameter field to set
		 * @return builder
		 */
		public Builder addFormalParameter(IXSLTParameterNode formalParameter) {
			checkNotNull(formalParameter);
			this.formalParameters.add(formalParameter);
			return this;
		}

		/**
		 * Builder method for actualParameters parameter.
		 *
		 * @param actualParameter field to set
		 * @return builder
		 */
		public Builder addActualParameter(IXSLTParameterNode actualParameter) {
			checkNotNull(actualParameter);
			this.actualParameters.add(actualParameter);
			return this;
		}

		/**
		 * Builder method for sorting parameter.
		 *
		 * @param sorting field to set
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
