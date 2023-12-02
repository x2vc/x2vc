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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import net.sf.saxon.om.NamespaceUri;
import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class XSLTSortNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTSortNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.XSLTSortNode#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(XSLTSortNode.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.withPrefabValues(QName.class, new QName("foo"), new QName("bar"))
			.withPrefabValues(NamespaceUri.class, NamespaceUri.of("http://foo"), NamespaceUri.of("bar"))
			.verify();
	}

}
