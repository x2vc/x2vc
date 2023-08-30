package org.x2vc.xml.request;

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
	 * {@link org.x2vc.xml.request.CompletedRequestRegistry#register(org.x2vc.xml.request.IDocumentRequest)}
	 * and
	 * {@link org.x2vc.xml.request.CompletedRequestRegistry#contains(org.x2vc.xml.request.IDocumentRequest)}.
	 */
	@Test
	void testRegisterAndContains() {

		final IAddElementRule originalRootRule = mock(IAddElementRule.class);
		final IAddElementRule alternateRootRule = mock(IAddElementRule.class);
		final IAddElementRule normalizedRootRule = mock(IAddElementRule.class);
		when(originalRootRule.normalize()).thenReturn(normalizedRootRule);
		when(alternateRootRule.normalize()).thenReturn(normalizedRootRule);

		final URI schemaURI = URI.create("foo:bar");
		final int schemaVersion = 1;
		final URI stylesheetURI = URI.create("bar:foo");

		final IDocumentRequest originalRequest = new DocumentRequest(schemaURI, schemaVersion, stylesheetURI,
				originalRootRule);

		this.registry.register(originalRequest);

		assertTrue(this.registry.contains(originalRequest));

		final IDocumentRequest alternateRequest = new DocumentRequest(schemaURI, schemaVersion, stylesheetURI,
				alternateRootRule);

		assertTrue(this.registry.contains(alternateRequest));

		final IDocumentRequest differingRequest = new DocumentRequest(schemaURI, schemaVersion + 1, stylesheetURI,
				alternateRootRule);

		assertFalse(this.registry.contains(differingRequest));
	}

}
