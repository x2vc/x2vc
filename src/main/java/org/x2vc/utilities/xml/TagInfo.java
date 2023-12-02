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
package org.x2vc.utilities.xml;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Optional;

/**
 * Default implementation of {@link ITagInfo}.
 */
public final class TagInfo implements ITagInfo {

	private final PolymorphLocation startLocation;
	private final PolymorphLocation endLocation;
	private final TagType tagType;
	private ITagInfo correspondingTag;

	private TagInfo(TagType tagType, PolymorphLocation startLocation, PolymorphLocation endLocation) {
		checkArgument(startLocation.compareTo(endLocation) < 0, "start location must lie before end location");
		this.tagType = tagType;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}

	/**
	 * Creates a new emtpy-element tag.
	 *
	 * @param startLocation
	 * @param endLocation
	 * @return the empty-element tag
	 */
	public static ITagInfo createEmptyTag(PolymorphLocation startLocation, PolymorphLocation endLocation) {
		return new TagInfo(TagType.EMPTY, startLocation, endLocation);
	}

	/**
	 * Creates a pair of start and corresponding end tag.
	 *
	 * @param startTagStartLocation
	 * @param startTagEndLocation
	 * @param endTagStartLocation
	 * @param endTagEndLocation
	 * @return the pair of start and end tag
	 */
	public static ITagInfo.Pair createTagPair(PolymorphLocation startTagStartLocation,
			PolymorphLocation startTagEndLocation, PolymorphLocation endTagStartLocation,
			PolymorphLocation endTagEndLocation) {
		checkArgument(startTagStartLocation.compareTo(startTagEndLocation) < 0,
				String.format("start location of start tag (%s) must lie before end location of start tag (%s)",
						startTagStartLocation, startTagEndLocation));
		checkArgument(endTagStartLocation.compareTo(endTagEndLocation) < 0,
				String.format("start location of end tag (%s) must lie before end location of end tag (%s)",
						endTagStartLocation, endTagEndLocation));
		checkArgument(startTagEndLocation.compareTo(endTagStartLocation) <= 0,
				String.format("end location of end tag (%s) must lie before start location of end tag (%s)",
						startTagEndLocation, endTagStartLocation));
		final TagInfo startTag = new TagInfo(TagType.START, startTagStartLocation, startTagEndLocation);
		final TagInfo endTag = new TagInfo(TagType.END, endTagStartLocation, endTagEndLocation);
		startTag.correspondingTag = endTag;
		endTag.correspondingTag = startTag;
		return new ITagInfo.Pair(startTag, endTag);
	}

	@Override
	public PolymorphLocation getStartLocation() {
		return this.startLocation;
	}

	@Override
	public PolymorphLocation getEndLocation() {
		return this.endLocation;
	}

	@Override
	public TagType getType() {
		return this.tagType;
	}

	@Override
	public boolean isEmptyElement() {
		return this.tagType == TagType.EMPTY;
	}

	@Override
	public boolean isStartTag() {
		return this.tagType == TagType.START;
	}

	@Override
	public boolean isEndTag() {
		return this.tagType == TagType.END;
	}

	@Override
	public Optional<ITagInfo> getStartTag() {
		if (this.tagType == TagType.END) {
			return Optional.of(this.correspondingTag);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<ITagInfo> getEndTag() {
		if (this.tagType == TagType.START) {
			return Optional.of(this.correspondingTag);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.correspondingTag, this.endLocation, this.startLocation, this.tagType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TagInfo)) {
			return false;
		}
		final TagInfo other = (TagInfo) obj;
		return Objects.equals(this.correspondingTag, other.correspondingTag)
				&& Objects.equals(this.endLocation, other.endLocation)
				&& Objects.equals(this.startLocation, other.startLocation)
				&& this.tagType == other.tagType;
	}

	@Override
	public String toString() {
		return this.tagType + "tag from " + this.startLocation + " to " + this.endLocation;
	}

}
