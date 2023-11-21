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
package org.x2vc.schema.structure;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.sf.saxon.s9api.QName;

/**
 * Standard implementation of {@link IStylesheetParameter}.
 */
public final class StylesheetParameter implements IStylesheetParameter {

	@XmlAttribute(name = "id")
	private final UUID id;

	@XmlElement(name = "comment")
	private final String comment;

	@XmlAttribute(name = "namespaceURI")
	private final String namespaceURI;

	@XmlAttribute(name = "localName")
	private final String localName;

	@XmlElement(name = "type", type = FunctionSignatureType.class)
	private final IFunctionSignatureType type;

	protected StylesheetParameter() {
		// required for marshalling/unmarshalling
		this.id = UUID.randomUUID();
		this.comment = null;
		this.namespaceURI = null;
		this.localName = "";
		this.type = null;
	}

	protected StylesheetParameter(Builder builder) {
		checkNotNull(builder.id);
		checkNotNull(builder.localName);
		this.id = builder.id;
		this.comment = builder.comment;
		this.namespaceURI = builder.namespaceURI;
		this.localName = builder.localName;
		this.type = builder.type;
	}

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public Optional<String> getComment() {
		return Optional.ofNullable(this.comment);
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
	public IFunctionSignatureType getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.comment, this.id, this.localName, this.namespaceURI, this.type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StylesheetParameter)) {
			return false;
		}
		final StylesheetParameter other = (StylesheetParameter) obj;
		return Objects.equals(this.comment, other.comment) && Objects.equals(this.id, other.id)
				&& Objects.equals(this.localName, other.localName)
				&& Objects.equals(this.namespaceURI, other.namespaceURI)
				&& Objects.equals(this.type, other.type);
	}

	@Override
	public String toString() {
		return this.type.toString()
				+ " "
				+ getQualifiedName().getClarkName();
	}

	/**
	 * Creates builder to build {@link StylesheetParameter}.
	 *
	 * @param localName
	 * @return created builder
	 */
	public static Builder builder(String localName) {
		return new Builder(localName);
	}

	/**
	 * Creates builder to build {@link StylesheetParameter}.
	 *
	 * @param parameterID
	 * @param localName
	 * @return created builder
	 */
	public static Builder builder(UUID parameterID, String localName) {
		return new Builder(parameterID, localName);
	}

	/**
	 * Creates builder to build {@link StylesheetParameter}.
	 *
	 * @param parameter
	 *
	 * @return created builder
	 */
	public static Builder builderFrom(IStylesheetParameter parameter) {
		return new Builder(parameter);
	}

	/**
	 * Builder to build {@link StylesheetParameter}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String namespaceURI;
		private String localName;
		private IFunctionSignatureType type;

		private Builder(String localName) {
			this.id = UUID.randomUUID();
			this.localName = localName;
		}

		private Builder(UUID id, String localName) {
			this.id = id;
			this.localName = localName;
		}

		private Builder(IStylesheetParameter parameter) {
			this.id = parameter.getID();
			this.comment = parameter.getComment().orElse(null);
			this.namespaceURI = parameter.getNamespaceURI().orElse(null);
			this.localName = parameter.getLocalName();
			this.type = parameter.getType();
		}

		/**
		 * Builder method for comment parameter.
		 *
		 * @param comment field to set
		 * @return builder
		 */
		public Builder withComment(String comment) {
			this.comment = comment;
			return this;
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
		 * Builder method for type parameter.
		 *
		 * @param type field to set
		 * @return builder
		 */
		public Builder withType(IFunctionSignatureType type) {
			this.type = type;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public StylesheetParameter build() {
			return new StylesheetParameter(this);
		}
	}

}
