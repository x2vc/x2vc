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
package org.x2vc.schema.evolution;


import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.x2vc.schema.structure.FunctionSignatureType;
import org.x2vc.schema.structure.IFunctionSignatureType;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.sf.saxon.s9api.QName;

/**
 * Standard implementation of {@link IAddParameterModifier}.
 */
public final class AddParameterModifier implements IAddParameterModifier {

	@XmlTransient
	private final URI schemaURI;

	@XmlTransient
	private final int schemaVersion;

	@XmlAttribute
	private final UUID parameterID;

	@XmlAttribute
	private final String namespaceURI;

	@XmlAttribute
	private final String localName;

	@XmlElement(name = "type", type = FunctionSignatureType.class)
	private final IFunctionSignatureType type;

	@XmlElement
	private final String comment;

	private AddParameterModifier(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.parameterID = builder.parameterID;
		this.namespaceURI = builder.namespaceURI;
		this.localName = builder.localName;
		this.type = builder.type;
		this.comment = builder.comment;
	}

	@Override
	public URI getSchemaURI() {
		return this.schemaURI;
	}

	@Override
	public int getSchemaVersion() {
		return this.schemaVersion;
	}

	@Override
	public Optional<UUID> getElementID() {
		return Optional.empty();
	}

	@Override
	public UUID getParameterID() {
		return this.parameterID;
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
	public Optional<String> getComment() {
		return Optional.ofNullable(this.comment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.comment, this.localName, this.namespaceURI, this.parameterID, this.schemaURI,
				this.schemaVersion, this.type);
	}

	@Override
	public int hashCodeIgnoringIDs() {
		return Objects.hash(
				this.comment,
				this.localName,
				this.namespaceURI,
				this.schemaURI,
				this.schemaVersion,
				this.type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AddParameterModifier)) {
			return false;
		}
		final AddParameterModifier other = (AddParameterModifier) obj;
		return Objects.equals(this.comment, other.comment) && Objects.equals(this.localName, other.localName)
				&& Objects.equals(this.namespaceURI, other.namespaceURI)
				&& Objects.equals(this.parameterID, other.parameterID)
				&& Objects.equals(this.schemaURI, other.schemaURI) && this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.type, other.type);
	}

	@Override
	public boolean equalsIgnoringIDs(ISchemaModifier otherModifier) {
		if (this == otherModifier) {
			return true;
		}
		if (otherModifier == null) {
			return false;
		}
		if (getClass() != otherModifier.getClass()) {
			return false;
		}
		final AddParameterModifier other = (AddParameterModifier) otherModifier;
		return Objects.equals(this.comment, other.comment) && Objects.equals(this.localName, other.localName)
				&& Objects.equals(this.namespaceURI, other.namespaceURI)
				&& Objects.equals(this.schemaURI, other.schemaURI) && this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.type, other.type);
	}

	/**
	 * Creates a new builder
	 *
	 * @param schemaURI
	 * @param schemaVersion
	 * @return the builder
	 */
	public static Builder builder(URI schemaURI, int schemaVersion) {
		return new Builder(schemaURI, schemaVersion);
	}

	/**
	 * Creates a builder to build {@link AddParameterModifier} and initialize it with the given object.
	 *
	 * @param addParameterModifier to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IAddParameterModifier addParameterModifier) {
		return new Builder(addParameterModifier);
	}

	/**
	 * Builder to build {@link AddParameterModifier}.
	 */
	public static final class Builder {
		private URI schemaURI;
		private int schemaVersion;
		private UUID parameterID = UUID.randomUUID();
		private String namespaceURI;
		private String localName;
		private IFunctionSignatureType type;
		private String comment;

		private Builder(URI schemaURI, int schemaVersion) {
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
		}

		private Builder(IAddParameterModifier addParameterModifier) {
			this.schemaURI = addParameterModifier.getSchemaURI();
			this.schemaVersion = addParameterModifier.getSchemaVersion();
			this.parameterID = addParameterModifier.getParameterID();
			this.namespaceURI = addParameterModifier.getNamespaceURI().orElse(null);
			this.localName = addParameterModifier.getLocalName();
			this.type = addParameterModifier.getType();
			this.comment = addParameterModifier.getComment().orElse(null);
		}

		/**
		 * Builder method for attributeID parameter.
		 *
		 * @param attributeID field to set
		 * @return builder
		 */
		public Builder withParameterID(UUID attributeID) {
			this.parameterID = attributeID;
			return this;
		}

		/**
		 * Builder method for name parameter.
		 *
		 * @param localName field to set
		 * @return builder
		 */
		public Builder withLocalName(String localName) {
			this.localName = localName;
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
		 * Builder method for comment parameter.
		 *
		 * @param comment field to set
		 * @return builder
		 */
		public Builder withComment(Optional<String> comment) {
			this.comment = comment.orElse(null);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public AddParameterModifier build() {
			return new AddParameterModifier(this);
		}
	}

	@Override
	public String toString() {
		return "AddParameterModifier [name=" + this.getQualifiedName() + ", parameterID="
				+ this.parameterID + ", type=" + this.type + "]";
	}

	@Override
	public int count() {
		// this modifier has a fixed structure
		return 1;
	}

}
