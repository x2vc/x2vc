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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletedRequestRegistryTest {

	private CompletedRequestRegistry registry;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.registry = new CompletedRequestRegistry();
	}

	/**
	 * Test method for
	 * {@link org.x2vc.xml.request.CompletedRequestRegistry#register(org.x2vc.xml.request.IDocumentRequest)} and
	 * {@link org.x2vc.xml.request.CompletedRequestRegistry#contains(org.x2vc.xml.request.IDocumentRequest)}.
	 */
	@Test
	void testRegisterAndContains() {

		final IAddElementRule originalRootRule = mock();
		final IAddElementRule alternateRootRule = mock();
		final IAddElementRule normalizedRootRule = mock();
		when(originalRootRule.normalize()).thenReturn(normalizedRootRule);
		when(alternateRootRule.normalize()).thenReturn(normalizedRootRule);

		final URI schemaURI = URI.create("foo:bar");
		final int schemaVersion = 1;
		final URI stylesheetURI = URI.create("bar:foo");

		final IDocumentRequest originalRequest = DocumentRequest
			.builder(schemaURI, schemaVersion, stylesheetURI, originalRootRule)
			.build();

		this.registry.register(originalRequest);

		assertTrue(this.registry.contains(originalRequest));

		final IDocumentRequest alternateRequest = DocumentRequest
			.builder(schemaURI, schemaVersion, stylesheetURI, alternateRootRule)
			.build();

		assertTrue(this.registry.contains(alternateRequest));

		final IDocumentRequest differingRequest = DocumentRequest
			.builder(schemaURI, schemaVersion + 1, stylesheetURI, alternateRootRule)
			.build();

		assertFalse(this.registry.contains(differingRequest));
	}

}
