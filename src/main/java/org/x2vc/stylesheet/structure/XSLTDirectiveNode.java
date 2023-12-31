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
package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.xml.ITagInfo;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.sf.saxon.om.NamespaceUri;

/**
 * Standard implementation of {@link IXSLTDirectiveNode}.
 */
public class XSLTDirectiveNode extends AbstractElementNode implements IXSLTDirectiveNode {

	private static final Logger logger = LogManager.getLogger();

	private final String name;
	private final ImmutableMap<String, NamespaceUri> namespaces;
	private final ImmutableMap<String, String> xsltAttributes;
	private final ImmutableMap<QName, String> otherAttributes;
	private final ImmutableList<IXSLTParameterNode> formalParameters;
	private final ImmutableList<IXSLTParameterNode> actualParameters;
	private final ImmutableList<IXSLTSortNode> sorting;

	protected XSLTDirectiveNode(Builder builder) {
		super(builder.parentStructure, builder.tagInfo, ImmutableList.copyOf(builder.childElements));
		this.name = builder.name;
		this.namespaces = ImmutableMap.copyOf(builder.namespaces);
		this.xsltAttributes = ImmutableMap.copyOf(builder.xsltAttributes);
		this.otherAttributes = ImmutableMap.copyOf(builder.otherAttributes);
		this.formalParameters = ImmutableList.copyOf(builder.formalParameters);
		this.actualParameters = ImmutableList.copyOf(builder.actualParameters);
		this.sorting = ImmutableList.copyOf(builder.sorting);
	}

	@Override
	public String getName() {
		return this.name;
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

	@SuppressWarnings({
			"java:S2065", // transient is used to mark the field as irrelevant for equals()/hashCode()
			"java:S4738" // Java supplier does not support memoization
	})
	private transient Supplier<ImmutableList<IXSLTDirectiveNode>> childDirectivesSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final List<IXSLTDirectiveNode> childDirectives = Lists.newArrayList();
		this.getChildElements().forEach(e -> collectChildDirectives(e, childDirectives));
		return logger.traceExit(ImmutableList.copyOf(childDirectives));
	});

	@Override
	public ImmutableList<IXSLTDirectiveNode> getChildDirectives() {
		return this.childDirectivesSupplier.get();
	}

	private void collectChildDirectives(IStructureTreeNode node, List<IXSLTDirectiveNode> childDirectives) {
		if (node instanceof final IXSLTDirectiveNode directive) {
			childDirectives.add(directive);
		} else if (node instanceof final IXMLNode xmlNode) {
			xmlNode.getChildElements().forEach(e -> collectChildDirectives(e, childDirectives));
		}
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
	 * @param tagInfo         the tag information
	 * @param name            the name of the directive
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure, ITagInfo tagInfo, String name) {
		return new Builder(parentStructure, tagInfo, name);
	}

	/**
	 * Builder to build {@link XSLTDirectiveNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private ITagInfo tagInfo;
		private String name;
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
		private Builder(IStylesheetStructure parentStructure, ITagInfo tagInfo, String name) {
			checkNotNull(parentStructure);
			checkNotNull(tagInfo);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.tagInfo = tagInfo;
			this.name = name;
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
		result = prime * result + Objects.hash(this.actualParameters, this.formalParameters, this.name, this.namespaces,
				this.otherAttributes,
				this.sorting, this.xsltAttributes);
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
		if (!(obj instanceof XSLTDirectiveNode)) {
			return false;
		}
		final XSLTDirectiveNode other = (XSLTDirectiveNode) obj;
		return Objects.equals(this.actualParameters, other.actualParameters)
				&& Objects.equals(this.formalParameters, other.formalParameters)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.namespaces, other.namespaces)
				&& Objects.equals(this.otherAttributes, other.otherAttributes)
				&& Objects.equals(this.sorting, other.sorting)
				&& Objects.equals(this.xsltAttributes, other.xsltAttributes);
	}

}
