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
package org.x2vc.utilities;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class PolymorphLocationTest {

	/**
	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(PolymorphLocation.class).verify();
	}

	@Test
	void testCompareTo() {

		final PolymorphLocation above = PolymorphLocation.builder().withLineNumber(1).withColumnNumber(42).build();
		final PolymorphLocation left = PolymorphLocation.builder().withLineNumber(10).withColumnNumber(5).build();
		final PolymorphLocation center = PolymorphLocation.builder().withLineNumber(10).withColumnNumber(10).build();
		final PolymorphLocation right = PolymorphLocation.builder().withLineNumber(10).withColumnNumber(20).build();
		final PolymorphLocation below = PolymorphLocation.builder().withLineNumber(20).withColumnNumber(1).build();

		assertEquals(-1, above.compareTo(center));
		assertEquals(1, center.compareTo(above));

		assertEquals(-1, left.compareTo(center));
		assertEquals(1, center.compareTo(left));

		assertEquals(0, center.compareTo(center));

		assertEquals(1, right.compareTo(center));
		assertEquals(-1, center.compareTo(right));

		assertEquals(1, below.compareTo(center));
		assertEquals(-1, center.compareTo(below));
	}

	/**
	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#from(javax.xml.stream.Location)}.
	 */
	@Test
	void testFromLocation() {
		final javax.xml.stream.Location source = mock();
		when(source.getPublicId()).thenReturn("pub");
		when(source.getSystemId()).thenReturn("sys");
		when(source.getLineNumber()).thenReturn(1);
		when(source.getColumnNumber()).thenReturn(2);
		when(source.getCharacterOffset()).thenReturn(3);

		final PolymorphLocation result = PolymorphLocation.from(source);
		assertEquals("pub", result.getPublicId());
		assertEquals("sys", result.getSystemId());
		assertEquals(1, result.getLineNumber());
		assertEquals(2, result.getColumnNumber());
		assertEquals(3, result.getCharacterOffset());
	}

	/**
	 * Test method for {@link org.x2vc.utilities.PolymorphLocation#from(javax.xml.transform.SourceLocator)}.
	 */
	@Test
	void testFromSourceLocator() {
		final javax.xml.transform.SourceLocator source = mock();
		when(source.getPublicId()).thenReturn("pub");
		when(source.getSystemId()).thenReturn("sys");
		when(source.getLineNumber()).thenReturn(1);
		when(source.getColumnNumber()).thenReturn(2);

		final PolymorphLocation result = PolymorphLocation.from(source);
		assertEquals("pub", result.getPublicId());
		assertEquals("sys", result.getSystemId());
		assertEquals(1, result.getLineNumber());
		assertEquals(2, result.getColumnNumber());
		assertEquals(-1, result.getCharacterOffset());
	}

}
