package org.x2vc.schema.evolution;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.x2vc.schema.structure.XMLDataType;

/**
 * Standard implementation of {@link IAddAttributeModifier}.
 */
public class AddAttributeModifier implements IAddAttributeModifier {

	private URI schemaURI;
	private int schemaVersion;
	private UUID elementID;
	private UUID attributeID;
	private String name;
	private XMLDataType dataType;

	private AddAttributeModifier(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.elementID = builder.elementID;
		this.attributeID = builder.attributeID;
		this.name = builder.name;
		this.dataType = builder.dataType;
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
	public UUID getElementID() {
		return this.elementID;
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
	public int hashCode() {
		return Objects.hash(
				this.dataType,
				this.elementID,
				this.name,
				this.attributeID,
				this.schemaURI,
				this.schemaVersion);
	}

	@Override
	public int hashCodeIgnoringIDs() {
		return Objects.hash(
				this.dataType,
				this.elementID,
				this.name,
				this.schemaURI,
				this.schemaVersion);
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
				&& this.schemaVersion == other.schemaVersion;
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
				&& this.schemaVersion == other.schemaVersion;
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

		private Builder(URI schemaURI, int schemaVersion) {
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
		}

		private Builder(IAddAttributeModifier addAttributeModifier) {
			this.schemaURI = addAttributeModifier.getSchemaURI();
			this.schemaVersion = addAttributeModifier.getSchemaVersion();
			this.elementID = addAttributeModifier.getElementID();
			this.attributeID = addAttributeModifier.getAttributeID();
			this.name = addAttributeModifier.getName();
			this.dataType = addAttributeModifier.getDataType();
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
