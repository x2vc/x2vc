package org.x2vc.report;

import com.google.common.collect.ImmutableSet;

/**
 * A message that was issued during the schema evolution, vulnerability checking or any other part of the process and
 * that signifies that some error condition or limitation was met that might influence the results of the check in a
 * negative way.
 *
 * Messages with the same severity and message text can be combined, in which case the details will be assembled in a
 * combined list.
 */
public interface IProcessingMessage {

	/**
	 * The severity classification of the message.
	 */
	public enum Severity {
		/**
		 * An informational message.
		 */
		INFO,

		/**
		 * A warning message: something has occurred that may have influenced the processing negatively.
		 */
		WARNING,

		/**
		 * An error message: something has gone wrong, the results are not to be trusted.
		 */
		ERROR,

		/**
		 * A fatal error message: the processing had to be aborted due to some error.
		 */
		FATAL
	}

	/**
	 * @return the severity classification of the message
	 */
	Severity getSeverity();

	/**
	 * @return the message text
	 */
	String getMessage();

	/**
	 * @return a list of additional information items
	 */
	ImmutableSet<String> getDetails();

	/**
	 * @param other
	 * @return <code>true</code> if the messages can be combined, i.e. only differ in the detail information
	 */
	boolean isSameMessage(IProcessingMessage other);

	/**
	 *
	 * @param other
	 * @return a message object containing the combined details if the messages can be merged
	 * @throws IllegalArgumentException if the messages can not be merged
	 * @see #isSameMessage(IProcessingMessage)
	 */
	IProcessingMessage combineWith(IProcessingMessage other);

}
