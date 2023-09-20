package org.x2vc.utilities;

import java.util.Objects;

/**
 * Implementation of various interfaces that all represent the same thing - a location in an XML or XSLT file.
 */
public class PolymorphLocation implements javax.xml.stream.Location, javax.xml.transform.SourceLocator {

	private int lineNumber;
	private int columnNumber;
	private int characterOffset;
	private String publicId;
	private String systemId;

	private PolymorphLocation(Builder builder) {
		this.lineNumber = builder.lineNumber;
		this.columnNumber = builder.columnNumber;
		this.characterOffset = builder.characterOffset;
		this.publicId = builder.publicId;
		this.systemId = builder.systemId;
	}

	/**
	 * Private constructor - use one of the factory methods or a builder to initialize.
	 *
	 * @see #fromStreamLocation(javax.xml.stream.Location)
	 * @param lineNumber
	 * @param columnNumber
	 * @param characterOffset
	 * @param publicId
	 * @param systemId
	 */
	private PolymorphLocation(int lineNumber, int columnNumber, int characterOffset, String publicId, String systemId) {
		super();
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.characterOffset = characterOffset;
		this.publicId = publicId;
		this.systemId = systemId;
	}

	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	@Override
	public int getColumnNumber() {
		return this.columnNumber;
	}

	@Override
	public int getCharacterOffset() {
		return this.characterOffset;
	}

	@Override
	public String getPublicId() {
		return this.publicId;
	}

	@Override
	public String getSystemId() {
		return this.systemId;
	}

	@Override
	public String toString() {
		return "Line number = " + getLineNumber() + "\n" +
				"Column number = " + getColumnNumber() + "\n" +
				"System Id = " + getSystemId() + "\n" +
				"Public Id = " + getPublicId() + "\n" +
				"CharacterOffset = " + getCharacterOffset() + "\n";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.characterOffset, this.columnNumber, this.lineNumber, this.publicId, this.systemId);
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
		final PolymorphLocation other = (PolymorphLocation) obj;
		return this.characterOffset == other.characterOffset && this.columnNumber == other.columnNumber
				&& this.lineNumber == other.lineNumber && Objects.equals(this.publicId, other.publicId)
				&& Objects.equals(this.systemId, other.systemId);
	}

	/**
	 * Creates a new instance based on a {@link javax.xml.stream.Location} object.
	 *
	 * @param location
	 * @return a copy of the location
	 */
	public static PolymorphLocation from(javax.xml.stream.Location location) {
		return new PolymorphLocation(location.getLineNumber(), location.getColumnNumber(),
				location.getCharacterOffset(), location.getPublicId(), location.getSystemId());
	}

	/**
	 * Creates a new instance based on a {@link javax.xml.transform.SourceLocator} object.
	 *
	 * @param locator
	 * @return a copy of the location
	 */
	public static PolymorphLocation from(javax.xml.transform.SourceLocator locator) {
		return new PolymorphLocation(locator.getLineNumber(), locator.getColumnNumber(),
				-1, locator.getPublicId(), locator.getSystemId());
	}

	/**
	 * Creates builder to build {@link PolymorphLocation}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link PolymorphLocation}.
	 */
	public static final class Builder {
		private int lineNumber;
		private int columnNumber;
		private int characterOffset;
		private String publicId;
		private String systemId;

		private Builder() {
		}

		/**
		 * Builder method for lineNumber parameter.
		 *
		 * @param lineNumber field to set
		 * @return builder
		 */
		public Builder withLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		/**
		 * Builder method for columnNumber parameter.
		 *
		 * @param columnNumber field to set
		 * @return builder
		 */
		public Builder withColumnNumber(int columnNumber) {
			this.columnNumber = columnNumber;
			return this;
		}

		/**
		 * Builder method for characterOffset parameter.
		 *
		 * @param characterOffset field to set
		 * @return builder
		 */
		public Builder withCharacterOffset(int characterOffset) {
			this.characterOffset = characterOffset;
			return this;
		}

		/**
		 * Builder method for publicId parameter.
		 *
		 * @param publicId field to set
		 * @return builder
		 */
		public Builder withPublicId(String publicId) {
			this.publicId = publicId;
			return this;
		}

		/**
		 * Builder method for systemId parameter.
		 *
		 * @param systemId field to set
		 * @return builder
		 */
		public Builder withSystemId(String systemId) {
			this.systemId = systemId;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public PolymorphLocation build() {
			return new PolymorphLocation(this);
		}
	}

}
