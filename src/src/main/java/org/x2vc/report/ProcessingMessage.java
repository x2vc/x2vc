package org.x2vc.report;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link IProcessingMessage}.
 */
public final class ProcessingMessage implements IProcessingMessage {

	@XmlAttribute
	private final Severity severity;

	@XmlElement
	private final String message;

	@XmlElementWrapper(name = "details")
	@XmlElement(name = "info")
	private final ImmutableSet<String> details;

	private ProcessingMessage(Builder builder) {
		checkNotNull(builder.severity);
		checkNotNull(builder.message);
		this.severity = builder.severity;
		this.message = builder.message;
		this.details = ImmutableSet.copyOf(builder.details);
	}

	@Override
	public Severity getSeverity() {
		return this.severity;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public ImmutableSet<String> getDetails() {
		return this.details;
	}

	@Override
	public boolean isSameMessage(IProcessingMessage other) {
		return Objects.equals(this.message, other.getMessage())
				&& this.severity == other.getSeverity();
	}

	@Override
	public IProcessingMessage combineWith(IProcessingMessage other) {
		if (!isSameMessage(other)) {
			throw new IllegalArgumentException(
					"Only messages with the same stylesheet, severity and message text can be combined");
		}
		return builder(this.severity, this.message)
			.withDetails(this.details)
			.withDetails(other.getDetails())
			.build();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.details, this.message, this.severity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ProcessingMessage)) {
			return false;
		}
		final ProcessingMessage other = (ProcessingMessage) obj;
		return Objects.equals(this.details, other.details) && Objects.equals(this.message, other.message)
				&& this.severity == other.severity;
	}

	/**
	 * @param severity
	 * @param message
	 * @return a new builder
	 */
	public static Builder builder(Severity severity, String message) {
		return new Builder(severity, message);
	}

	/**
	 * Builder to build {@link ProcessingMessage}.
	 */
	public static final class Builder {
		private Severity severity;
		private String message;
		private Set<String> details = Sets.newHashSet();

		private Builder(Severity severity, String message) {
			this.severity = severity;
			this.message = message;
		}

		/**
		 * Builder method for details parameter.
		 *
		 * @param details field to set
		 * @return builder
		 */
		public Builder withDetails(ImmutableCollection<String> details) {
			this.details.addAll(details);
			return this;
		}

		/**
		 * Builder method for details parameter.
		 *
		 * @param detail field to set
		 * @return builder
		 */
		public Builder withDetail(String detail) {
			this.details.add(detail);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public ProcessingMessage build() {
			return new ProcessingMessage(this);
		}
	}

}
