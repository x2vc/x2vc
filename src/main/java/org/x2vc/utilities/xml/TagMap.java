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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Default implementation of {@link ITagMap}.
 */
public final class TagMap implements ITagMap {

	record MapEntry(Range<Integer> offsetRange, ITagInfo tag) {
	}

	private final List<MapEntry> entries;

	@Inject
	TagMap(@Assisted List<ITagInfo> tags) {
		this.entries = Lists.newArrayListWithCapacity(tags.size());
		tags.forEach(tag -> {
			final int startOffset = tag.getStartLocation().getCharacterOffset();
			final int endOffset = tag.getEndLocation().getCharacterOffset();
			final MapEntry entry = new MapEntry(Range.closedOpen(startOffset, endOffset), tag);
			this.entries.add(entry);
		});
	}

	@Override
	public Optional<ITagInfo> getTag(PolymorphLocation location) {
		final int offset = location.getCharacterOffset();
		return this.entries.stream().filter(e -> e.offsetRange.contains(offset)).map(e -> e.tag).findFirst();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.entries);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TagMap)) {
			return false;
		}
		final TagMap other = (TagMap) obj;
		return Objects.equals(this.entries, other.entries);
	}

}
