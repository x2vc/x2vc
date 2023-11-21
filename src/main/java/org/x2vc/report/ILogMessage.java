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
package org.x2vc.report;


import org.apache.logging.log4j.Level;
import org.x2vc.utilities.ReportCollectorAppender;

/**
 * A message that was collected from the {@link ReportCollectorAppender}.
 */
public interface ILogMessage {

	/**
	 * @return the severity classification of the message
	 */
	Level getLevel();

	/**
	 * @return the name of the string the message was produced in
	 */
	String getThreadName();

	/**
	 * @return the message text
	 */
	String getMessage();

}
