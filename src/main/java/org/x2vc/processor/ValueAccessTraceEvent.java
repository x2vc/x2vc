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
import java.util.UUID;

import javax.xml.transform.SourceLocator;

import org.x2vc.utilities.xml.PolymorphLocation;

import net.sf.saxon.expr.Expression;

/**
 * Standard implementation of {@link IValueAccessTraceEvent}.
 */
public final class ValueAccessTraceEvent implements IValueAccessTraceEvent {

	private final PolymorphLocation location;
	private final Expression expression;
	private final UUID contextElementID;

	private ValueAccessTraceEvent(Builder builder) {
		this.location = builder.location;
		this.expression = builder.expression;
		this.contextElementID = builder.contextElementID;
	}

	@Override
	public PolymorphLocation getLocation() {
		return this.location;
	}

	@Override
	public Expression getExpression() {
		return this.expression;
	}

	@Override
	public Optional<UUID> getContextElementID() {
		return Optional.ofNullable(this.contextElementID);
	}

	/**
	 * Creates builder to build {@link ValueAccessTraceEvent}.
	 *
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link ValueAccessTraceEvent}.
	 */
	public static final class Builder {
		private PolymorphLocation location;
		private Expression expression;
		private UUID contextElementID;

		private Builder() {
		}

		/**
		 * Builder method for location parameter.
		 *
		 * @param location field to set
		 * @return builder
		 */
		public Builder withLocation(PolymorphLocation location) {
			this.location = location;
			return this;
		}

		/**
		 * Builder method for location parameter.
		 *
		 * @param locator field to set
		 * @return builder
		 */
		public Builder withLocation(SourceLocator locator) {
			this.location = PolymorphLocation.from(locator);
			return this;
		}

		/**
		 * Builder method for expression parameter.
		 *
		 * @param expression field to set
		 * @return builder
		 */
		public Builder withExpression(Expression expression) {
			this.expression = expression;
			return this;
		}

		/**
		 * Builder method for contextElementID parameter.
		 *
		 * @param contextElementID field to set
		 * @return builder
		 */
		public Builder withContextElementID(UUID contextElementID) {
			this.contextElementID = contextElementID;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public ValueAccessTraceEvent build() {
			return new ValueAccessTraceEvent(this);
		}
	}

	@Override
	public String toString() {
		return String.format("Value access trace: %s of element %s at [%s]",
				this.expression, this.contextElementID == null ? "unknown" : this.contextElementID,
				this.location.toString().replace("\n", ", "));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.contextElementID, this.expression, this.location);
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
		final ValueAccessTraceEvent other = (ValueAccessTraceEvent) obj;
		return Objects.equals(this.contextElementID, other.contextElementID)
				&& Objects.equals(this.expression, other.expression)
				&& Objects.equals(this.location, other.location);
	}

}
