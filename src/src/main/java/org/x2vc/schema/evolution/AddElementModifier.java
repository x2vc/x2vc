package org.x2vc.schema.evolution;

import java.net.URI;
import java.util.*;

import org.x2vc.schema.structure.IXMLElementType.ContentType;

import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link IAddElementModifier}.
 */
public class AddElementModifier implements IAddElementModifier {

	private URI schemaURI;
	private int schemaVersion;
	private UUID elementID;
	private UUID referenceID;
	private String name;
	private UUID typeID;
	private Integer minOccurrence;
	private Integer maxOccurrence;
	private ContentType contentType;
	private String typeComment;
	private String referenceComment;

	private Set<IAddElementModifier> elementModifiers = new HashSet<>();
	private Set<IAddAttributeModifier> attributeModifiers = new HashSet<>();

	private AddElementModifier(Builder builder) {
		this.schemaURI = builder.schemaURI;
		this.schemaVersion = builder.schemaVersion;
		this.elementID = builder.elementID;
		this.referenceID = builder.referenceID;
		this.name = builder.name;
		this.typeID = builder.typeID;
		this.minOccurrence = builder.minOccurrence;
		this.maxOccurrence = builder.maxOccurrence;
		this.contentType = builder.contentType;
		this.typeComment = builder.typeComment;
		this.referenceComment = builder.referenceComment;
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
	public UUID getReferenceID() {
		return this.referenceID;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public UUID getTypeID() {
		return this.typeID;
	}

	@Override
	public Integer getMinOccurrence() {
		return this.minOccurrence;
	}

	@Override
	public Optional<Integer> getMaxOccurrence() {
		return Optional.ofNullable(this.maxOccurrence);
	}

	@Override
	public ContentType getContentType() {
		return this.contentType;
	}

	@Override
	public Optional<String> getTypeComment() {
		return Optional.ofNullable(this.typeComment);
	}

	@Override
	public Optional<String> getReferenceComment() {
		return Optional.ofNullable(this.referenceComment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				this.contentType,
				this.elementID,
				this.maxOccurrence,
				this.minOccurrence,
				this.name,
				this.referenceID,
				this.schemaURI,
				this.schemaVersion,
				this.typeID,
				this.typeComment,
				this.referenceComment);
	}

	@Override
	public int hashCodeIgnoringIDs() {
		return Objects.hash(
				this.contentType,
				this.elementID,
				this.maxOccurrence,
				this.minOccurrence,
				this.name,
				this.schemaURI,
				this.schemaVersion,
				this.typeComment,
				this.referenceComment);
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
		final AddElementModifier other = (AddElementModifier) obj;
		return this.contentType == other.contentType
				&& Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.maxOccurrence, other.maxOccurrence)
				&& Objects.equals(this.minOccurrence, other.minOccurrence)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.referenceID, other.referenceID)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.typeID, other.typeID)
				&& Objects.equals(this.typeComment, other.typeComment)
				&& Objects.equals(this.referenceComment, other.referenceComment);
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
		final AddElementModifier other = (AddElementModifier) otherModifier;
		return this.contentType == other.contentType
				&& Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.maxOccurrence, other.maxOccurrence)
				&& Objects.equals(this.minOccurrence, other.minOccurrence)
				&& Objects.equals(this.name, other.name)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& this.schemaVersion == other.schemaVersion
				&& Objects.equals(this.typeComment, other.typeComment)
				&& Objects.equals(this.referenceComment, other.referenceComment);
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
	 * Creates a builder to build {@link AddElementModifier} and initialize it with the given object. <b>CAUTION:</b>
	 * Performs a shallow copy, i.e. does NOT copy the contained attribute and element modifiers.
	 *
	 * @param addElementModifier to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IAddElementModifier addElementModifier) {
		return new Builder(addElementModifier);
	}

	/**
	 * Builder to build {@link AddElementModifier}.
	 */
	public static final class Builder {
		private URI schemaURI;
		private int schemaVersion;
		private UUID elementID;
		private UUID referenceID = UUID.randomUUID();
		private String name;
		private UUID typeID = UUID.randomUUID();
		private Integer minOccurrence;
		private Integer maxOccurrence;
		private ContentType contentType = ContentType.MIXED;
		private String typeComment;
		private String referenceComment;

		private Builder(URI schemaURI, int schemaVersion) {
			this.schemaURI = schemaURI;
			this.schemaVersion = schemaVersion;
		}

		private Builder(IAddElementModifier addElementModifier) {
			this.schemaURI = addElementModifier.getSchemaURI();
			this.schemaVersion = addElementModifier.getSchemaVersion();
			this.elementID = addElementModifier.getElementID();
			this.referenceID = addElementModifier.getReferenceID();
			this.name = addElementModifier.getName();
			this.typeID = addElementModifier.getTypeID();
			this.minOccurrence = addElementModifier.getMinOccurrence();
			this.maxOccurrence = addElementModifier.getMinOccurrence();
			this.contentType = addElementModifier.getContentType();
			this.typeComment = addElementModifier.getTypeComment().orElse(null);
			this.referenceComment = addElementModifier.getReferenceComment().orElse(null);
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
		 * Builder method for referenceID parameter.
		 *
		 * @param referenceID field to set
		 * @return builder
		 */
		public Builder withReferenceID(UUID referenceID) {
			this.referenceID = referenceID;
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
		 * Builder method for typeID parameter.
		 *
		 * @param typeID field to set
		 * @return builder
		 */
		public Builder withTypeID(UUID typeID) {
			this.typeID = typeID;
			return this;
		}

		/**
		 * Builder method for minOccurrence parameter.
		 *
		 * @param minOccurrence field to set
		 * @return builder
		 */
		public Builder withMinOccurrence(Integer minOccurrence) {
			this.minOccurrence = minOccurrence;
			return this;
		}

		/**
		 * Builder method for maxOccurrence parameter.
		 *
		 * @param maxOccurrence field to set
		 * @return builder
		 */
		public Builder withMaxOccurrence(Integer maxOccurrence) {
			this.maxOccurrence = maxOccurrence;
			return this;
		}

		/**
		 * Builder method for contentType parameter.
		 *
		 * @param contentType field to set
		 * @return builder
		 */
		public Builder withContentType(ContentType contentType) {
			this.contentType = contentType;
			return this;
		}

		/**
		 * Builder method for typeComment parameter.
		 *
		 * @param typeComment field to set
		 * @return builder
		 */
		public Builder withTypeComment(String typeComment) {
			this.typeComment = typeComment;
			return this;
		}

		/**
		 * Builder method for typeComment parameter.
		 *
		 * @param typeComment field to set
		 * @return builder
		 */
		public Builder withTypeComment(Optional<String> typeComment) {
			this.typeComment = typeComment.orElse(null);
			return this;
		}

		/**
		 * Builder method for referenceComment parameter.
		 *
		 * @param referenceComment field to set
		 * @return builder
		 */
		public Builder withReferenceComment(String referenceComment) {
			this.referenceComment = referenceComment;
			return this;
		}

		/**
		 * Builder method for referenceComment parameter.
		 *
		 * @param referenceComment field to set
		 * @return builder
		 */
		public Builder withReferenceComment(Optional<String> referenceComment) {
			this.referenceComment = referenceComment.orElse(null);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public AddElementModifier build() {
			return new AddElementModifier(this);
		}
	}

	@Override
	public String toString() {
		return "AddElementModifier [name=" + this.name + ", elementID=" + this.elementID + ", referenceID="
				+ this.referenceID + ", typeID=" + this.typeID + ", contentType=" + this.contentType + "]";
	}

	@Override
	public ImmutableSet<IAddAttributeModifier> getAttributes() {
		return ImmutableSet.copyOf(this.attributeModifiers);
	}

	@Override
	public void addAttribute(IAddAttributeModifier attributeModifier) {
		this.attributeModifiers.add(attributeModifier);
	}

	@Override
	public ImmutableSet<IAddElementModifier> getSubElements() {
		return ImmutableSet.copyOf(this.elementModifiers);
	}

	@Override
	public void addSubElement(IAddElementModifier elementModifier) {
		this.elementModifiers.add(elementModifier);
	}

	@Override
	public int count() {
		int result = 1;
		result += this.attributeModifiers.stream().mapToInt(IAddAttributeModifier::count).sum();
		result += this.elementModifiers.stream().mapToInt(IAddElementModifier::count).sum();
		return result;
	}

}
