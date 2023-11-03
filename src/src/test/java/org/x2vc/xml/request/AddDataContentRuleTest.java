package org.x2vc.xml.request;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class AddDataContentRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.AddDataContentRule#normalize()}.
	 */
	@Test
	void testNormalizeWithoutRequestedValue() {
		final UUID ruleID = UUID.randomUUID();
		final UUID elementID = UUID.randomUUID();

		final AddDataContentRule originalRule = new AddDataContentRule(ruleID, elementID);
		final AddDataContentRule normalizedRule = (AddDataContentRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(elementID, normalizedRule.getElementID());
		assertFalse(normalizedRule.getRequestedValue().isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.AddDataContentRule#normalize()}.
	 */
	@Test
	void testNormalizeWithRequestedValue() {
		final UUID ruleID = UUID.randomUUID();
		final UUID elementID = UUID.randomUUID();
		final IRequestedValue originalValue = mock();
		final IRequestedValue normalizedValue = mock();
		when(originalValue.normalize()).thenReturn(normalizedValue);

		final AddDataContentRule originalRule = new AddDataContentRule(ruleID, elementID, originalValue);
		final AddDataContentRule normalizedRule = (AddDataContentRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(elementID, normalizedRule.getElementID());
		assertSame(normalizedValue, normalizedRule.getRequestedValue().get());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.AddDataContentRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AddDataContentRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();

	}

}
