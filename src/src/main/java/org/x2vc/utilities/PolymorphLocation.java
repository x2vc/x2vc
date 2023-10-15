package org.x2vc.utilities;

import java.util.Objects;

import org.apache.logging.log4j.util.Strings;

/**
 * Implementation of various interfaces that all represent the same thing - a location in an XML or XSLT file.
 */
public final class PolymorphLocation
		implements javax.xml.stream.Location, javax.xml.transform.SourceLocator, Comparable<PolymorphLocation> {

	private static boolean useCompactToStringNotation = true;

	private final int lineNumber;
	private final int columnNumber;
	private final int characterOffset;
	private final String publicID;
	private final String systemID;

	private PolymorphLocation(Builder builder) {
		this.lineNumber = builder.lineNumber;
		this.columnNumber = builder.columnNumber;
		this.characterOffset = builder.characterOffset;
		this.publicID = builder.publicId;
		this.systemID = builder.systemId;
	}

	/**
	 * Private constructor - use one of the factory methods or a builder to initialize.
	 *
	 * @see #fromStreamLocation(javax.xml.stream.Location)
	 * @param lineNumber
	 * @param columnNumber
	 * @param characterOffset
	 * @param publicID
	 * @param systemID
	 */
	private PolymorphLocation(int lineNumber, int columnNumber, int characterOffset, String publicID, String systemID) {
		super();
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.characterOffset = characterOffset;
		this.publicID = publicID;
		this.systemID = systemID;
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
		return this.publicID;
	}

	@Override
	public String getSystemId() {
		return this.systemID;
	}

	@Override
	public int compareTo(PolymorphLocation other) {
		// -1: this < other
		// 0: this = other
		// 1: this > other
		if (this.getLineNumber() < other.getLineNumber()) {
			return -1;
		} else if (this.getLineNumber() > other.getLineNumber()) {
			return 1;
		} else {
			if (this.getColumnNumber() < other.getColumnNumber()) {
				return -1;
			} else if (this.getColumnNumber() > other.getColumnNumber()) {
				return 1;
			} else {
				return 0;
			}
		}
		// TODO Support comparison of locations from different sources
	}

	@Override
	public String toString() {
		if (useCompactToStringNotation) {
			final StringBuilder sb = new StringBuilder();
			if (!Strings.isBlank(this.publicID)) {
				sb.append("{P:" + this.publicID + "}");
			}
			if (!Strings.isBlank(this.systemID)) {
				sb.append("{S:" + this.systemID + "}");
			}
			sb.append(String.format("l%d/c%d=ch%d", this.lineNumber, this.columnNumber, this.characterOffset));
			return sb.toString();
		} else {
			return "Line number = " + getLineNumber() + "\n" +
					"Column number = " + getColumnNumber() + "\n" +
					"System Id = " + getSystemId() + "\n" +
					"Public Id = " + getPublicId() + "\n" +
					"CharacterOffset = " + getCharacterOffset() + "\n";
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.characterOffset, this.columnNumber, this.lineNumber, this.publicID, this.systemID);
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
				&& this.lineNumber == other.lineNumber && Objects.equals(this.publicID, other.publicID)
				&& Objects.equals(this.systemID, other.systemID);
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
