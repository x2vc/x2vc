package org.x2vc.utilities;

import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.StandardErrorReporter;

/**
 * Adapter class to route the Saxon logger messages to "our" logger.
 */
public class SaxonLoggerAdapter extends net.sf.saxon.lib.Logger {

	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@Override
	public void println(String message, int severity) {
		// severity: info 0; warn 1; error 2; fatal 3
		final String reformattedMessage = message.replace("\n", " ").replace("\r", " ").replace("  ", " ");
		switch (severity) {
		case 0:
			logger.info(reformattedMessage);
			break;
		case 1:
			logger.warn(reformattedMessage);
			break;
		case 2:
			logger.error(reformattedMessage);
			break;
		default:
			logger.fatal(reformattedMessage);
		}
	}

	/**
	 * @return an {@link ErrorReporter} preconfigured with an {@link SaxonLoggerAdapter} instance
	 */
	public static ErrorReporter makeReporter() {
		final StandardErrorReporter reporter = new StandardErrorReporter();
		reporter.setLogger(new SaxonLoggerAdapter());
		return reporter;
	}

}
