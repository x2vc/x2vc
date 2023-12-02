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

import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IXMLNode}.
 */
public final class XMLNode extends AbstractElementNode implements IXMLNode {

	private final QName name;
	private final PolymorphLocation startLocation;
	private final PolymorphLocation endLocation;
	private final ImmutableMap<QName, String> attributes;

	private XMLNode(Builder builder) {
		super(builder.parentStructure, ImmutableList.copyOf(builder.childElements));
		this.name = builder.name;
		this.startLocation = builder.startLocation;
		this.endLocation = builder.endLocation;
		this.attributes = ImmutableMap.copyOf(builder.attributes);
	}

	@Override
	public QName getName() {
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
	public ImmutableMap<QName, String> getAttributes() {
		return this.attributes;
	}

	/**
	 * Creates a new builder instance.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @param name            the name of the element
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure, QName name) {
		return new Builder(parentStructure, name);
	}

	/**
	 * Builder to build {@link XMLNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private QName name;
		private PolymorphLocation startLocation;
		private PolymorphLocation endLocation;
		private Map<QName, String> attributes = new HashMap<>();
		private List<IStructureTreeNode> childElements = new ArrayList<>();

		/**
		 * Creates a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param name            the name of the element
		 */
		private Builder(IStylesheetStructure parentStructure, QName name) {
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
		 * Adds an attribute to the builder.
		 *
		 * @param name  the attribute name
		 * @param value the attribute value
		 * @return builder
		 */
		public Builder addAttribute(QName name, String value) {
			checkNotNull(name);
			checkNotNull(value);
			this.attributes.put(name, value);
			return this;
		}

		/**
		 * Adds a child element to the builder.
		 *
		 * @param element the child element
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
		public XMLNode build() {
			return new XMLNode(this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.attributes, this.endLocation, this.name, this.startLocation);
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
		if (!(obj instanceof XMLNode)) {
			return false;
		}
		final XMLNode other = (XMLNode) obj;
		return Objects.equals(this.attributes, other.attributes) && Objects.equals(this.endLocation, other.endLocation)
				&& Objects.equals(this.name, other.name) && Objects.equals(this.startLocation, other.startLocation);
	}

}
