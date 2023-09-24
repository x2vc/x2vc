package org.x2vc.processor;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.utilities.PolymorphLocation;

import net.sf.saxon.expr.Expression;

/**
 * An event that tracks an attempt to access a value in the source document.
 */
public interface IValueAccessTraceEvent extends ITraceEvent {

	/**
	 * @return the location of the instruction causing the access
	 */
	PolymorphLocation getLocation();

	/**
	 * @return the expression evaluated to access some values
	 */
	Expression getExpression();

	/**
	 * Determines the trace ID of the context element, if known. Not all events have a context element (e.g. variable
	 * access events usually don't).
	 *
	 * @return the trace ID of the context element, if known.
	 */
	Optional<UUID> getContextElementID();

}
