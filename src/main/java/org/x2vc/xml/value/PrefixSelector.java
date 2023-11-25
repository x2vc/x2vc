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
package org.x2vc.xml.value;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IPrefixSelector}.
 */
public class PrefixSelector implements IPrefixSelector {

	/**
	 * The length of the prefix to generate.
	 */
	public static final Integer PREFIX_LENGTH = 4;

	/**
	 * The total value of the length to suggest.
	 */
	public static final Integer VALUE_LENGTH = 8;

	private static final Logger logger = LogManager.getLogger();
	private IStylesheetManager stylesheetManager;

	private Random rng;

	@Inject
	PrefixSelector(Random rng, IStylesheetManager stylesheetManager) {
		this.rng = rng;
		this.stylesheetManager = stylesheetManager;
	}

	@Override
	public PrefixData selectPrefix(URI stylesheetURI) {
		logger.traceEntry("for stylesheet {}", stylesheetURI);

		// prepare a list of possible prefixes
		final HashSet<String> possiblePrefixes = generatePossiblePrefixes();

		// get the source code of the stylesheet
		final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(stylesheetURI);
		String source = stylesheetInfo.getPreparedStylesheet();

		// convert to local case and replace everything except for letters a-z with a
		// space
		source = source.toLowerCase().replaceAll("[^a-z]", " ");

		// split into words
		Splitter.on(" ").trimResults().omitEmptyStrings().splitToStream(source)
			// remove every entry that is shorter than the prefix length
			.filter(word -> word.length() >= PREFIX_LENGTH).forEach(word -> {
				// extract all possible substrings of PREFIX_LENGTH
				int start = 0;
				int end = start + PREFIX_LENGTH;
				while (end <= word.length()) {
					final String substring = word.substring(start, end);
					// and remove the substring from the list of candidates
					possiblePrefixes.remove(substring);
					start++;
					end = start + PREFIX_LENGTH;
				}
			});

		final int index = this.rng.nextInt(possiblePrefixes.size());
		logger.debug("selecting {} out of {} possible prefixes left after filtering", index, possiblePrefixes.size());
		final String prefix = possiblePrefixes.toArray(new String[0])[index];
		return logger.traceExit(new PrefixData(prefix, VALUE_LENGTH));
	}

	/**
	 * @return a {@link HashSet} of all of the permutations possible for the set prefix length
	 */
	private HashSet<String> generatePossiblePrefixes() {
		List<String> values = Lists.newArrayList("");
		for (int length = 0; length < PrefixSelector.PREFIX_LENGTH; length++) {
			final List<String> newValues = Lists.newArrayListWithExpectedSize(values.size() * 26);
			values.forEach(value -> {
				for (char letter = 'a'; letter <= 'z'; letter++) {
					newValues.add(value + letter);
				}
			});
			values = newValues;
		}
		return Sets.newHashSet(values);

	}

}
