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

import org.apache.logging.log4j.core.config.Configurator;
import org.x2vc.process.CheckerFactory;
import org.x2vc.process.CommonOptions;
import org.x2vc.process.VersionProvider;
import org.x2vc.process.commands.FullProcessCommand;
import org.x2vc.process.commands.SchemaProcessCommand;
import org.x2vc.process.commands.XSSProcessCommand;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * The main entry point of the command line application.
 */
@Command(name = "x2vc", subcommands = { FullProcessCommand.class, SchemaProcessCommand.class,
		XSSProcessCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class Checker {

	static {
		// use the default logging config file provided with the application until other
		// settings are made
		Configurator.initialize(null, "default-log4j2.xml");
	}

	/**
	 * @param args the command line parameters
	 */
	public static void main(String[] args) {
		Thread.currentThread().setName("x2vc-main");

		// first process the global options
		final CommonOptions commonOptions = new CommonOptions();
		new CommandLine(commonOptions).parseArgs(args);
		commonOptions.configureLoggers();

		// then load and validate the configuration
		final Config config = ConfigFactory.load();
		config.checkValid(ConfigFactory.defaultReference());

		final int exitCode = new CommandLine(Checker.class, new CheckerFactory(config))
			.execute(args);
		System.exit(exitCode);
	}

}
