package org.x2vc;

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

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.util.Predicate;

/**
 * Checks and assertions used throughout multiple tests.
 */
public final class CustomAssertions {

	protected CustomAssertions() {

	}

	/**
	 * Checks to ensure the two XML strings are equal.
	 *
	 * @param expected
	 * @param actual
	 */
	public static void assertXMLEquals(String expected, String actual) {
		assertNotNull(actual);
		final Diff d = DiffBuilder.compare(Input.fromString(expected))
			.ignoreWhitespace()
			.withTest(actual)
			.build();
		if (d.hasDifferences()) {
			assertionFailure()
				.message(d.fullDescription())
				.expected(expected)
				.actual(actual)
				.buildAndThrow();
		}
	}

	/**
	 * Checks to ensure the two XML strings are equal while excluding certain elements using a filter
	 *
	 * @param expected
	 * @param actual
	 * @param nodeFilter
	 */
	public static void assertXMLEquals(String expected, String actual, Predicate<Node> nodeFilter) {
		assertNotNull(actual);
		final Diff d = DiffBuilder.compare(Input.fromString(expected))
			.ignoreWhitespace()
			.withTest(actual)
			.withNodeFilter(nodeFilter)
			.build();
		if (d.hasDifferences()) {
			assertionFailure()
				.message(d.fullDescription())
				.expected(expected)
				.actual(actual)
				.buildAndThrow();
		}
	}
}
