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

import org.junit.jupiter.api.Test;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StringLiteral;
import nl.jqno.equalsverifier.EqualsVerifier;

class ValueAccessTraceEventTest {

	/**
	 * Test method for {@link org.x2vc.processor.ValueAccessTraceEvent#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(ValueAccessTraceEvent.class)
			.withPrefabValues(Expression.class, new StringLiteral("foo"), new StringLiteral("bar"))
			.verify();
	}

}
