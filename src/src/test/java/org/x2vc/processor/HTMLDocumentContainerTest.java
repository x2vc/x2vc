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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class HTMLDocumentContainerTest {

	/**
	 * Test method for {@link org.x2vc.processor.HTMLDocumentContainer#equals(Object)} and
	 * {@link org.x2vc.processor.HTMLDocumentContainer#hashCode()}.
	 */
	@Test
	@Disabled("produces an InaccessibleObjectException, see https://github.com/pinterest/ktlint/issues/1391")
	void testEqualsObject() {
		EqualsVerifier.forClass(HTMLDocumentContainer.class)
			.verify();
	}

}
