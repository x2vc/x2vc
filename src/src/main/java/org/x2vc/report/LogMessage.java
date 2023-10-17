package org.x2vc.report;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.apache.logging.log4j.Level;

/**
 * Standard implementation of {@link ILogMessage}.
 */
public final class LogMessage implements ILogMessage {

	@XmlTransient
	private final Level level;

	@XmlAttribute(name = "level")
	private final String levelString;

	@XmlAttribute(name = "thread")
	private final String threadName;

	@XmlValue
	private final String message;

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
		this.levelString = level.name();
		this.threadName = threadName;
		this.message = message
			.replace("&", "&amp;")
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
