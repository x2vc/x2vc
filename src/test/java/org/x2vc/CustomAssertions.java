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
package org.x2vc;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.w3c.dom.Node;
import org.x2vc.process.CheckerModule;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.util.Predicate;

import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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

	/**
	 * Checks to ensure that an implementation of the type specified can be obtained via Guice dependency injection.
	 *
	 * @param type
	 */
	public static void assertInjectionPossible(Class<?> type) {
		final Config config = ConfigFactory.load();
		final Injector injector = Guice.createInjector(new CheckerModule(config),
				TypesafeConfigModule.fromConfigWithPackage(config, "org.x2vc"));
		final Object instance = injector.getInstance(type);
		assertNotNull(instance);
	}

}
