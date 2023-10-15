package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;

import javax.xml.namespace.QName;

import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.PolymorphLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.sf.saxon.om.NamespaceUri;

/**
 * Standard implementation of {@link IXSLTDirectiveNode}.
 */
public class XSLTDirectiveNode extends AbstractStructureTreeNode implements IXSLTDirectiveNode {

	private String name;
	private PolymorphLocation startLocation;
	private PolymorphLocation endLocation;
	private ImmutableMap<String, NamespaceUri> namespaces;
	private ImmutableMap<String, String> xsltAttributes;
	private ImmutableMap<QName, String> otherAttributes;
	private ImmutableList<IStructureTreeNode> childElements;
	private ImmutableList<IXSLTDirectiveNode> childDirectives;
	private ImmutableList<IXSLTParameterNode> formalParameters;
	private ImmutableList<IXSLTParameterNode> actualParameters;
	private ImmutableList<IXSLTSortNode> sorting;

	protected XSLTDirectiveNode(Builder builder) {
		super(builder.parentStructure);
		this.name = builder.name;
		this.startLocation = builder.startLocation;
		this.endLocation = builder.endLocation;
		this.namespaces = ImmutableMap.copyOf(builder.namespaces);
		this.xsltAttributes = ImmutableMap.copyOf(builder.xsltAttributes);
		this.otherAttributes = ImmutableMap.copyOf(builder.otherAttributes);
		this.childElements = ImmutableList.copyOf(builder.childElements);
		this.formalParameters = ImmutableList.copyOf(builder.formalParameters);
		this.actualParameters = ImmutableList.copyOf(builder.actualParameters);
		this.sorting = ImmutableList.copyOf(builder.sorting);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Optional<PolymorphLocation> getStartLocation() {
		return Optional.ofNullable(this.startLocation);
	}

	@Override
	public Optional<PolymorphLocation> getEndLocation() {
		return Optional.ofNullable(this.endLocation);
	}

	@Override
	public ImmutableMap<String, NamespaceUri> getNamespaces() {
		return this.namespaces;
	}

	@Override
	public ImmutableMap<String, String> getXSLTAttributes() {
		return this.xsltAttributes;
	}

	@Override
	public Optional<String> getXSLTAttribute(String name) {
		if (this.xsltAttributes.containsKey(name)) {
			return Optional.of(this.xsltAttributes.get(name));
		} else {
			return Optional.empty();
		}
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
					this.childElements.stream()
						.filter(IXSLTDirectiveNode.class::isInstance)
						.map(IXSLTDirectiveNode.class::cast)
						.iterator());
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
	 * Create a builder instance.
	 *
	 * @param parentStructure the parent {@link IStylesheetStructure}
	 * @param name            the name of the directive
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure, String name) {
		return new Builder(parentStructure, name);
	}

	/**
	 * Builder to build {@link XSLTDirectiveNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private String name;
		private PolymorphLocation startLocation;
		private PolymorphLocation endLocation;
		private Map<String, NamespaceUri> namespaces = new HashMap<>();
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
		private Builder(IStylesheetStructure parentStructure, String name) {
			checkNotNull(parentStructure);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.name = name;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(PolymorphLocation startLocation) {
			this.startLocation = startLocation;
			return this;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(javax.xml.stream.Location startLocation) {
			this.startLocation = PolymorphLocation.from(startLocation);
			return this;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(javax.xml.transform.SourceLocator startLocation) {
			this.startLocation = PolymorphLocation.from(startLocation);
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(PolymorphLocation endLocation) {
			this.endLocation = endLocation;
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(javax.xml.stream.Location endLocation) {
			this.endLocation = PolymorphLocation.from(endLocation);
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(javax.xml.transform.SourceLocator endLocation) {
			this.endLocation = PolymorphLocation.from(endLocation);
			return this;
		}

		/**
		 * @param prefix
		 * @param uri
		 * @return builder
		 */
		public Builder withNamespace(String prefix, NamespaceUri uri) {
			this.namespaces.put(prefix, uri);
			return this;
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
		public IXSLTDirectiveNode build() {
			if (this.name.equals(XSLTConstants.Elements.TEMPLATE)) {
				return new XSLTTemplateNode(this);
			} else {
				return new XSLTDirectiveNode(this);
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(this.actualParameters, this.childDirectives, this.childElements, this.endLocation,
						this.formalParameters, this.name, this.otherAttributes, this.sorting, this.startLocation,
						this.xsltAttributes);
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
				&& Objects.equals(this.childDirectives, other.childDirectives)
				&& Objects.equals(this.childElements, other.childElements)
				&& Objects.equals(this.endLocation, other.endLocation)
				&& Objects.equals(this.formalParameters, other.formalParameters)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.otherAttributes, other.otherAttributes)
				&& Objects.equals(this.sorting, other.sorting)
				&& Objects.equals(this.startLocation, other.startLocation)
				&& Objects.equals(this.xsltAttributes, other.xsltAttributes);
	}

}
