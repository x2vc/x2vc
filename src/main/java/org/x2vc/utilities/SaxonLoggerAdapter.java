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
