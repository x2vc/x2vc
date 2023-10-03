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
		final IDocumentModifier normalizedModifier = mock(IDocumentModifier.class);
		final IDocumentModifier originalModifier = mock(IDocumentModifier.class);
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
