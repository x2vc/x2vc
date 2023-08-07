package org.x2vc.process;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetManager;

import com.google.inject.Inject;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

/**
 * Temporary process class to test some components.
 */
@Command(name = "demo", mixinStandardHelpOptions = true, version = "demo 0.1", description = "Intermediary process to test some components.")
public class DemoProcess implements Callable<Integer> {

	private static final Logger logger = LogManager.getLogger();

	@Mixin
	LoggingMixin loggingMixin;

	@Parameters(description = "The XSLT file to check.", arity = "1")
	private File xsltFile;

	private IStylesheetManager stylesheetManager;

	@Inject
	DemoProcess(IStylesheetManager stylesheetManager) {
		this.stylesheetManager = stylesheetManager;
	}

	@Override
	public Integer call() throws Exception {
		/*
		 * final URI uri = this.xsltFile.toURI(); final String source =
		 * Files.readString(this.xsltFile.toPath());
		 * logger.info("{} characters read from file {}", source.length(), uri);
		 * IStylesheetInformation ssi = this.preprocessor.prepareStylesheet(uri,
		 * source);
		 */
		this.stylesheetManager.get(this.xsltFile.toURI());

		return 0;

	}

}
