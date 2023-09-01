package org.x2vc;

import org.x2vc.process.CheckerFactory;
import org.x2vc.process.LoggingMixin;
import org.x2vc.process.commands.FullProcessCommand;
import org.x2vc.process.commands.SchemaProcessCommand;
import org.x2vc.process.commands.XSSProcessCommand;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

/**
 * The main entry point of the command line application.
 */
@Command(name = "x2vc", subcommands = { FullProcessCommand.class, SchemaProcessCommand.class, XSSProcessCommand.class })
public class Checker {
	static {
		LoggingMixin.initializeLog4j(); // programmatic initialization; must be done before calling
										// LogManager.getLogger()
	}

	/**
	 * The global logging command line mixin.
	 */
	@Mixin
	public LoggingMixin loggingMixin;

	/**
	 * @param args the command line parameters
	 */
	public static void main(String[] args) {
		Thread.currentThread().setName("x2vc-main");
		final Config config = ConfigFactory.load();
		config.checkValid(ConfigFactory.defaultReference());
		final int exitCode = new CommandLine(Checker.class, new CheckerFactory(config))
			.setExecutionStrategy(LoggingMixin::executionStrategy).execute(args);
		System.exit(exitCode);
	}

}
