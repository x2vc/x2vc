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

import org.x2vc.schema.structure.XMLDataType;

/**
 * Standard implementation of {@link IAddAttributeModifier}.
 */
public final class AddAttributeModifier implements IAddAttributeModifier {

	@XmlTransient
	private final URI schemaURI;

	@XmlTransient
	private final int schemaVersion;

	@XmlAttribute
	private final UUID elementID;

	@XmlAttribute
	private final UUID attributeID;

	@XmlAttribute
	private final String name;

	@XmlAttribute
	private final XMLDataType dataType;

	@XmlElement
	private final String comment;

	private AddAttributeModifier(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.elementID = builder.elementID;
		this.attributeID = builder.attributeID;
		this.name = builder.name;
		this.dataType = builder.dataType;
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
		return Optional.of(this.elementID);
	}

	@Override
	public UUID getAttributeID() {
		return this.attributeID;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public XMLDataType getDataType() {
		return this.dataType;
	}

	@Override
	public Optional<String> getComment() {
		return Optional.ofNullable(this.comment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				this.dataType,
				this.elementID,
				this.name,
				this.attributeID,
				this.schemaURI,
				this.schemaVersion,
				this.comment);
	}

	@Override
	public int hashCodeIgnoringIDs() {
		return Objects.hash(
				this.dataType,
				this.elementID,
				this.name,
				this.schemaURI,
				this.schemaVersion,
				this.comment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AddAttributeModifier other = (AddAttributeModifier) obj;
		return this.dataType == other.dataType
				&& Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.attributeID, other.attributeID)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.comment, other.comment);
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
		final AddAttributeModifier other = (AddAttributeModifier) otherModifier;
		return this.dataType == other.dataType
				&& Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.comment, other.comment);
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
	 * Creates a builder to build {@link AddAttributeModifier} and initialize it with the given object.
	 *
	 * @param addAttributeModifier to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IAddAttributeModifier addAttributeModifier) {
		return new Builder(addAttributeModifier);
	}

	/**
	 * Builder to build {@link AddAttributeModifier}.
	 */
	public static final class Builder {
		private URI schemaURI;
		private int schemaVersion;
		private UUID elementID;
		private UUID attributeID = UUID.randomUUID();
		private String name;
		private XMLDataType dataType = XMLDataType.STRING;
		private String comment;

		private Builder(URI schemaURI, int schemaVersion) {
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
		}

		private Builder(IAddAttributeModifier addAttributeModifier) {
			this.schemaURI = addAttributeModifier.getSchemaURI();
			this.schemaVersion = addAttributeModifier.getSchemaVersion();
			this.elementID = addAttributeModifier.getElementID().orElseThrow();
			this.attributeID = addAttributeModifier.getAttributeID();
			this.name = addAttributeModifier.getName();
			this.dataType = addAttributeModifier.getDataType();
			this.comment = addAttributeModifier.getComment().orElse(null);
		}

		/**
		 * Builder method for elementID parameter.
		 *
		 * @param elementID field to set
		 * @return builder
		 */
		public Builder withElementID(UUID elementID) {
			this.elementID = elementID;
			return this;
		}

		/**
		 * Builder method for attributeID parameter.
		 *
		 * @param attributeID field to set
		 * @return builder
		 */
		public Builder withAttributeID(UUID attributeID) {
			this.attributeID = attributeID;
			return this;
		}

		/**
		 * Builder method for name parameter.
		 *
		 * @param name field to set
		 * @return builder
		 */
		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Builder method for dataType parameter.
		 *
		 * @param dataType field to set
		 * @return builder
		 */
		public Builder withXMLDataType(XMLDataType dataType) {
			this.dataType = dataType;
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
		public AddAttributeModifier build() {
			return new AddAttributeModifier(this);
		}
	}

	@Override
	public String toString() {
		return "AddAttributeModifier [name=" + this.name + ", elementID=" + this.elementID + ", attributeID="
				+ this.attributeID + ", dataType=" + this.dataType + "]";
	}

	@Override
	public int count() {
		// this modifier has a fixed structure
		return 1;
	}

}
