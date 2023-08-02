package org.x2vc;

import org.x2vc.process.CheckerFactory;
import org.x2vc.process.DemoProcess;
import org.x2vc.process.LoggingMixin;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

/**
 * The main entry point of the command line application.
 */
@Command(name = "x2vc", subcommands = { DemoProcess.class })
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
		final int exitCode = new CommandLine(Checker.class, new CheckerFactory())
				.setExecutionStrategy(LoggingMixin::executionStrategy).execute(args);
		System.exit(exitCode);
	}

}
