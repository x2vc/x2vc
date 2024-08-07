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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.Level;
import org.x2vc.utilities.jaxb.Log4jLevelAdapter;

/**
 * Standard implementation of {@link ILogMessage}.
 */
public final class LogMessage implements ILogMessage {

	@XmlAttribute(name = "level")
	@XmlJavaTypeAdapter(Log4jLevelAdapter.class)
	private final Level level;

	@XmlAttribute(name = "thread")
	private final String threadName;

	@XmlValue
	private final String message;

	/**
	 * Default constructor required for JAXB operation.
	 */
	protected LogMessage() {
		this.level = Level.TRACE;
		this.threadName = "";
		this.message = "";
	}

	/**
	 * Create new log message.
	 *
	 * @param level
	 * @param threadName
	 * @param message
	 */
	public LogMessage(Level level, String threadName, String message) {
		super();
		this.level = level;
		this.threadName = threadName;
		this.message = message
			.replace("&", "&amp;")
			.replace("&amp;amp;", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("'", "&apos;")
			.replace("\"", "&quot;");
	}

	@Override
	public Level getLevel() {
		return this.level;
	}

	@Override
	public String getThreadName() {
		return this.threadName;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return "LogMessage [level=" + this.level + ", threadName=" + this.threadName + ", message=" + this.message
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.level, this.message, this.threadName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LogMessage)) {
			return false;
		}
		final LogMessage other = (LogMessage) obj;
		return Objects.equals(this.level, other.level) && Objects.equals(this.message, other.message)
				&& Objects.equals(this.threadName, other.threadName);
	}

}
