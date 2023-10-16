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
