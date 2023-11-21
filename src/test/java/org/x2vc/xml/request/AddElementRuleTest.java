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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class AddElementRuleTest {

	/**
	 * Test method for {@link org.x2vc.xml.request.AddElementRule#normalize()}.
	 */
	@Test
	void testNormalize() {
		final ISetAttributeRule originalAttributeRule = mock();
		final ISetAttributeRule normalizedAttributeRule = mock();
		when(originalAttributeRule.normalize()).thenReturn(normalizedAttributeRule);

		// add two content rules - order must be preserved

		final IContentGenerationRule originalContentRule1 = mock();
		final IContentGenerationRule normalizedContentRule1 = mock();
		when(originalContentRule1.normalize()).thenReturn(normalizedContentRule1);

		final IContentGenerationRule originalContentRule2 = mock();
		final IContentGenerationRule normalizedContentRule2 = mock();
		when(originalContentRule2.normalize()).thenReturn(normalizedContentRule2);

		final UUID ruleID = UUID.randomUUID();
		final UUID elementReferenceID = UUID.randomUUID();

		final AddElementRule originalRule = AddElementRule.builder(elementReferenceID).withRuleID(ruleID)
			.addAttributeRule(originalAttributeRule).addContentRule(originalContentRule1)
			.addContentRule(originalContentRule2).build();
		final AddElementRule normalizedRule = (AddElementRule) originalRule.normalize();

		assertNotSame(originalRule, normalizedRule);
		assertEquals(UUID.fromString("0000-00-00-00-000000"), normalizedRule.getID());
		assertEquals(elementReferenceID, normalizedRule.getElementReferenceID());
		assertEquals(Set.of(normalizedAttributeRule), normalizedRule.getAttributeRules());
		assertEquals(List.of(normalizedContentRule1, normalizedContentRule2), normalizedRule.getContentRules());
	}

	/**
	 * Test method for {@link org.x2vc.xml.request.AddElementRule#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(AddElementRule.class)
			.withRedefinedSuperclass()
			.usingGetClass()
			.verify();
	}

}
