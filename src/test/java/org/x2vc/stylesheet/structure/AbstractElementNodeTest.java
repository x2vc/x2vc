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
package org.x2vc.stylesheet.structure;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractElementNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractElementNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.AbstractElementNode#hashCode()}
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(AbstractElementNode.class)
			.withRedefinedSubclass(XMLNode.class)
			.usingGetClass()
			.verify();
	}

}
