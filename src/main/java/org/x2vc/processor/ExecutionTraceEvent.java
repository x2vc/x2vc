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
package org.x2vc.processor;


import java.util.Objects;
import java.util.Optional;

import org.x2vc.utilities.xml.PolymorphLocation;

/**
 * Standard implementation of {@link IExecutionTraceEvent}.
 */
public final class ExecutionTraceEvent implements IExecutionTraceEvent {

	private final ExecutionEventType eventType;
	private final String executedElement;
	private final PolymorphLocation elementLocation;

	private ExecutionTraceEvent(Builder builder) {
		this.eventType = builder.eventType;
		this.executedElement = builder.executedElement;
		this.elementLocation = builder.elementLocation;
	}

	@Override
	public ExecutionEventType getEventType() {
		return this.eventType;
	}

	@Override
	public boolean isEnterEvent() {
		return this.eventType == ExecutionEventType.ENTER;
	}

	@Override
	public boolean isLeaveEvent() {
		return this.eventType == ExecutionEventType.LEAVE;
	}

	@Override
	public PolymorphLocation getElementLocation() {
		return this.elementLocation;
	}

	@Override
	public Optional<String> getExecutedElement() {
		return Optional.ofNullable(this.executedElement);
	}

	/**
	 * Creates builder to build {@link ExecutionTraceEvent}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link ExecutionTraceEvent}.
	 */
	public static final class Builder {
		private ExecutionEventType eventType;
		private String executedElement;
		private PolymorphLocation elementLocation;

		private Builder() {
		}

		/**
		 * Builder method for eventType parameter.
		 *
		 * @param eventType field to set
		 * @return builder
		 */
		public Builder withEventType(ExecutionEventType eventType) {
			this.eventType = eventType;
			return this;
		}

		/**
		 * Builder method for executedElement parameter.
		 *
		 * @param executedElement field to set
		 * @return builder
		 */
		public Builder withExecutedElement(String executedElement) {
			this.executedElement = executedElement;
			return this;
		}

		/**
		 * Builder method for elementLocation parameter.
		 *
		 * @param elementLocation field to set
		 * @return builder
		 */
		public Builder withElementLocation(PolymorphLocation elementLocation) {
			this.elementLocation = elementLocation;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public ExecutionTraceEvent build() {
			return new ExecutionTraceEvent(this);
		}
	}

	@Override
	public String toString() {
		return String.format("Execution trace: %s of %s at [%s]",
				this.eventType, this.executedElement,
				this.elementLocation.toString().replace("\n", ", "));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.elementLocation, this.eventType, this.executedElement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ExecutionTraceEvent other = (ExecutionTraceEvent) obj;
		return Objects.equals(this.elementLocation, other.elementLocation) && this.eventType == other.eventType
				&& Objects.equals(this.executedElement, other.executedElement);
	}

}
