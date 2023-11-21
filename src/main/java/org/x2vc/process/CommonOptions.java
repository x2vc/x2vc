package org.x2vc.process;

import java.io.File;

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

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.google.common.base.Strings;

import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

/**
 * This class is used to specify and handle the command line options that are common to all subcommands and that need to
 * be handled before the dependency injection framework is setup.
 */
public class CommonOptions {

	/**
	 * Allows setting of system properties even when using the wrapper script.
	 *
	 * @param propertyValues
	 */
	@Option(names = "-D", mapFallbackValue = "", description = "Set system property.") // allow -Dkey without value
	private void setSystemProperty(Map<String, String> propertyValues) {
		propertyValues.forEach((k, v) -> System.setProperty(k, Strings.isNullOrEmpty(v) ? "" : v));
	}

	/**
	 * Sets the specified verbosity.
	 */
	@Option(names = { "-v", "--verbose" }, description = { "Specify multiple -v options to increase verbosity.",
			"For example, `-v -v` or `-vv`" })
	private boolean[] verbosity;

	/**
	 * Applies the logging configuration file specified.
	 */
	@Option(names = "--logConfig", description = "Specify an alternate Log4j2 configuration file to use.")
	private File configFile;

	/**
	 * Catch all remaining options
	 */
	@Unmatched
	List<String> remainingOptions;

	/**
	 * Configures the Log4j2 console appender(s), using the alternate config file and/or the specified verbosity:
	 * <ul>
	 * <li>{@code -vv} : enable TRACE level</li>
	 * <li>{@code -v} : enable DEBUG level</li>
	 * <li>(not specified) : enable INFO level</li>
	 * </ul>
	 */
	public void configureLoggers() {
		final LoggerContext loggerContext = LoggerContext.getContext(false);

		// attempt to exchange config if alternate config file is provided
		if (this.configFile != null) {
			Configurator.reconfigure(this.configFile.toURI());
		}

		// adjust the verbosity if specified
		if (this.verbosity != null) {
			final Level level = calcLogLevel();
			final LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
			for (final Appender appender : rootConfig.getAppenders().values()) {
				if (appender instanceof ConsoleAppender) {
					rootConfig.removeAppender(appender.getName());
					rootConfig.addAppender(appender, level, null);
				}
			}
			if (rootConfig.getLevel().isMoreSpecificThan(level)) {
				rootConfig.setLevel(level);
			}
		}

		// apply the changes
		loggerContext.updateLoggers();
	}

	private Level calcLogLevel() {
		switch (this.verbosity.length) {
		case 0:
			return Level.INFO;
		case 1:
			return Level.DEBUG;
		default:
			return Level.TRACE;
		}
	}

}
