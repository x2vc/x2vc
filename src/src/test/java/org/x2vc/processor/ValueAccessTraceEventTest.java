package org.x2vc.processor;

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
