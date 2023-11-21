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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AbstractStructureTreeNodeTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.AbstractStructureTreeNode#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO #26 check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(AbstractStructureTreeNode.class).verify();
	}

}
