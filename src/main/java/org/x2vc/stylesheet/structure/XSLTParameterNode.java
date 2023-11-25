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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlTransient;

import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.QName;

/**
 * Standard implementation of {@link IXSLTParameterNode}.
 */
public final class XSLTParameterNode extends AbstractStructureTreeNode implements IXSLTParameterNode {

	private final String namespaceURI;
	private final String localName;
	private final PolymorphLocation startLocation;
	private final PolymorphLocation endLocation;
	private final String selection;
	private final ImmutableList<IStructureTreeNode> childElements;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	private XSLTParameterNode(Builder builder) {
		super(builder.parentStructure);
		checkNotNull(builder.localName);
		this.namespaceURI = builder.namespaceURI;
		this.localName = builder.localName;
		this.startLocation = builder.startLocation;
		this.endLocation = builder.endLocation;
		this.selection = builder.selection;
		this.childElements = ImmutableList.copyOf(builder.childElements);
	}

	@Override
	public Optional<String> getNamespaceURI() {
		return Optional.ofNullable(this.namespaceURI);
	}

	@Override
	public String getLocalName() {
		return this.localName;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<QName> qualifiedNameSupplier = Suppliers
		.memoize(() -> {
			final Optional<String> oNamespace = getNamespaceURI();
			if (oNamespace.isPresent()) {
				return new QName(oNamespace.get(), getLocalName());
			} else {
				return new QName(getLocalName());
			}
		});

	@XmlTransient
	@Override
	public QName getQualifiedName() {
		return this.qualifiedNameSupplier.get();
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
	public Optional<String> getSelection() {
		return Optional.ofNullable(this.selection);
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	/**
	 * Creates a new builder
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @param localName       the name of the element
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure, String localName) {
		return new Builder(parentStructure, localName);
	}

	/**
	 * Builder to build {@link XSLTParameterNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private String namespaceURI;
		private String localName;
		private PolymorphLocation startLocation;
		private PolymorphLocation endLocation;
		private String selection;
		private List<IStructureTreeNode> childElements = new ArrayList<>();

		/**
		 * Creates a new builder
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param name            the name of the element
		 */
		private Builder(IStylesheetStructure parentStructure, String localName) {
			checkNotNull(parentStructure);
			checkNotNull(localName);
			this.parentStructure = parentStructure;
			this.localName = localName;
		}

		/**
		 * Builder method for namespaceURI parameter.
		 *
		 * @param namespaceURI field to set
		 * @return builder
		 */
		public Builder withNamespaceURI(String namespaceURI) {
			this.namespaceURI = namespaceURI;
			return this;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(this.childElements, this.endLocation, this.localName, this.namespaceURI, this.selection,
						this.startLocation);
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
		if (!(obj instanceof XSLTParameterNode)) {
			return false;
		}
		final XSLTParameterNode other = (XSLTParameterNode) obj;
		return Objects.equals(this.childElements, other.childElements)
				&& Objects.equals(this.endLocation, other.endLocation)
				&& Objects.equals(this.localName, other.localName)
				&& Objects.equals(this.namespaceURI, other.namespaceURI)
				&& Objects.equals(this.selection, other.selection)
				&& Objects.equals(this.startLocation, other.startLocation);
	}

}
