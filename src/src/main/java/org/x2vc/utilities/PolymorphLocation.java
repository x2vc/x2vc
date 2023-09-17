package org.x2vc.utilities;

import java.util.Objects;

/**
 * Implementation of various interfaces that all represent the same thing - a
 * location in an XML or XSLT file.
 */
public class PolymorphLocation implements javax.xml.stream.Location, javax.xml.transform.SourceLocator {

	private int lineNumber;
	private int columnNumber;
	private int characterOffset;
	private String publicId;
	private String systemId;

	/**
	 * Private constructor - use one of the factory methods to initialize.
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
	 * Creates a new instance based on a {@link javax.xml.transform.SourceLocator}
	 * object.
	 *
	 * @param locator
	 * @return a copy of the location
	 */
	public static PolymorphLocation from(javax.xml.transform.SourceLocator locator) {
		return new PolymorphLocation(locator.getLineNumber(), locator.getColumnNumber(),
				-1, locator.getPublicId(), locator.getSystemId());
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

}
