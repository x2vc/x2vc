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

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class XMLNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XMLNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.XMLNode#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(XMLNode.class)
			.withRedefinedSuperclass()
			.withPrefabValues(QName.class, new QName("foo"), new QName("bar"))
			.verify();
	}

}
