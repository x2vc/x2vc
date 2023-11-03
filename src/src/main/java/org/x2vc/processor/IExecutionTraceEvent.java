package org.x2vc.processor;

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

import java.util.Optional;

import org.x2vc.utilities.PolymorphLocation;

/**
 * An event that tracks the execution (or evaluation) of an XSLT directive.
 */
public interface IExecutionTraceEvent extends ITraceEvent {

	/**
	 * Marks an event as the beginning (enter) or the end (leave) of the evaluation of a directive.
	 */
	enum ExecutionEventType {
		ENTER, LEAVE
	}

	/**
	 * Determines whether the event is the beginning (enter) or the end (leave) of the evaluation of a directive.
	 *
	 * @return the event type
	 */
	ExecutionEventType getEventType();

	/**
	 * @return <code>true</code> if the event is a beginning (enter) event
	 */
	boolean isEnterEvent();

	/**
	 * @return <code>true</code> if the event is a end (leave) event
	 */
	boolean isLeaveEvent();

	/**
	 * Determines the location of the element within the stylesheet. Note that even for LEAVE events, the location of
	 * the <b>starting</b> element in the stylesheet is recorded - that's just the way the XSLT processor works.
	 *
	 * @return the location of the element within the stylesheet
	 */
	PolymorphLocation getElementLocation();

	/**
	 * Determines the name of the directive as reported by the processor. Caution: This might not be the actual element
	 * contained in the stylesheet, e.g. <code>xsl:if</code> instructions are reported as <code>choose</code> elements.
	 *
	 * @return the name of the directive as reported by the processor
	 */
	Optional<String> getExecutedElement();

}
