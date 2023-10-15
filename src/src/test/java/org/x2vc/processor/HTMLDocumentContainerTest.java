package org.x2vc.processor;

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
