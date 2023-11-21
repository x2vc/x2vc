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
package org.x2vc.xml.request;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.xml.document.IDocumentModifier;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class RequestedValueTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.RequestedValue#normalize()}.
	 */
	@Test
	void testNormalize() {
		final IDocumentModifier normalizedModifier = mock();
		final IDocumentModifier originalModifier = mock();
		when(originalModifier.normalize()).thenReturn(normalizedModifier);

		final RequestedValue originalValue = new RequestedValue("foobar", originalModifier);
		final IRequestedValue normalizedValue = originalValue.normalize();

		assertNotSame(originalValue, normalizedValue);
		assertEquals(normalizedValue.getValue(), originalValue.getValue());
		assertSame(normalizedModifier, normalizedValue.getModifier().get());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.RequestedValue#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(RequestedValue.class).verify();
	}
}
