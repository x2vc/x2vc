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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Default implementation of {@link ITagMapBuilder}.
 */
public class TagMapBuilder implements ITagMapBuilder {

	private static final Logger logger = LogManager.getLogger();

	private final ITagMapFactory tagMapFactory;

	@Inject
	TagMapBuilder(ITagMapFactory tagMapFactory) {
		this.tagMapFactory = tagMapFactory;
	}

	@Override
	public ITagMap buildTagMap(String xmlSource, ILocationMap locationMap) throws IllegalArgumentException {
		logger.traceEntry();
		final List<ITagInfo> tags = Lists.newArrayList();

		logger.trace("TMB in {}", xmlSource);

		// kill all CDATA sections to make life a bit easier
		String workingSource = xmlSource;
		workingSource = removeCDataSections(workingSource);
		logger.trace("TMB nc {}", workingSource);

		// pick out the empty-element tags
		workingSource = extractEmptyElementTags(workingSource, locationMap, tags);
		logger.trace("TMB ee {}", workingSource);

		// extract the remaining tags
		extractSplitTags(workingSource, locationMap, tags);

		// sort the list of tags for consistent results
		tags.sort((tag1, tag2) -> (tag1.getStartLocation().compareTo(tag2.getStartLocation())));

		return logger.traceExit(this.tagMapFactory.create(tags));
	}

	/**
	 * @param xmlSource
	 * @return the contents of xmlSource with all CDATA sections replaced by inactive characters
	 */
	private String removeCDataSections(String xmlSource) {
		final Pattern cDataPattern = Pattern.compile("<!\\[CDATA\\[.*?\\]\\]>");
		final Matcher cDataMatcher = cDataPattern.matcher(xmlSource);
		while (cDataMatcher.find()) {
			final int startIndex = cDataMatcher.start();
			final int endIndex = cDataMatcher.end();
			final int length = endIndex - startIndex;
			xmlSource = xmlSource.substring(0, startIndex)
					+ "X".repeat(length)
					+ xmlSource.substring(endIndex);
		}
		return xmlSource;
	}

	/**
	 * @param xmlSource
	 * @param locationMap
	 * @param tagList
	 * @return the contents of xmlSource with all empty-element tags replaced by inactive characters and added to the
	 *         tag list
	 * @throws IllegalArgumentException
	 */
	private String extractEmptyElementTags(String xmlSource, ILocationMap locationMap, final List<ITagInfo> tagList)
			throws IllegalArgumentException {
		final Pattern emptyElementPattern = Pattern.compile(
				"<([a-z][\\w:-]*)(?: [a-z][\\w:-]+=\"[^\"]*\")*\\s*/>",
				Pattern.CASE_INSENSITIVE);
		final Matcher emptyElementMatcher = emptyElementPattern.matcher(xmlSource);
		while (emptyElementMatcher.find()) {
			final int startIndex = emptyElementMatcher.start();
			final int endIndex = emptyElementMatcher.end();
			final int length = endIndex - startIndex;
			final ITagInfo tag = TagInfo.createEmptyTag(locationMap.getLocation(startIndex),
					locationMap.getLocation(endIndex));
			tagList.add(tag);
			xmlSource = xmlSource.substring(0, startIndex)
					+ "Y".repeat(length)
					+ xmlSource.substring(endIndex);
		}
		return xmlSource;
	}

	/**
	 * @param xmlSource
	 * @param locationMap
	 * @param tagList
	 */
	private void extractSplitTags(String xmlSource, ILocationMap locationMap, List<ITagInfo> tagList) {
		final Pattern tagPairPattern = Pattern.compile(
				"<([a-z][\\w:-]*)((?: [a-z][\\w:-]+=\"[^\"]*\")*)\\s*>.*?<(/\\1)>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		boolean matchFound = true;

		while (matchFound) {
			final Matcher tagPairMatcher = tagPairPattern.matcher(xmlSource);
			matchFound = tagPairMatcher.find();
			if (matchFound) {
				final int startTagLength = (tagPairMatcher.group(2) == null) ? (tagPairMatcher.group(1).length() + 2)
						: (tagPairMatcher.group(1).length() + tagPairMatcher.group(2).length() + 2);
				final int startTagStartPosition = tagPairMatcher.start();
				final int startTagEndPosition = +startTagStartPosition + startTagLength;
				final int endTagLength = tagPairMatcher.group(3).length() + 2;
				final int endTagEndPosition = tagPairMatcher.end();
				final int endTagStartPosition = endTagEndPosition - endTagLength;
				final ITagInfo.Pair tagPair = TagInfo.createTagPair(
						locationMap.getLocation(startTagStartPosition),
						locationMap.getLocation(startTagEndPosition),
						locationMap.getLocation(endTagStartPosition),
						locationMap.getLocation(endTagEndPosition));
				tagList.add(tagPair.start());
				tagList.add(tagPair.end());
				xmlSource = xmlSource.substring(0, startTagStartPosition)
						+ "Z".repeat(startTagLength)
						+ xmlSource.substring(startTagEndPosition, endTagStartPosition)
						+ "Z".repeat(endTagLength)
						+ xmlSource.substring(endTagEndPosition);
				logger.trace("TMB st {}", xmlSource);
			}
		}

	}

}
