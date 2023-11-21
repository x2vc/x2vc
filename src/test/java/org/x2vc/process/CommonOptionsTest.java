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
package org.x2vc.process;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class CommonOptionsTest {

	/**
	 * Test method for {@link org.x2vc.process.CommonOptions#setSystemProperty(java.util.Map)}.
	 */
	@Test
	void testSetSystemProperty_KeyValue() {
		new CommandLine(new CommonOptions()).parseArgs(new String[] { "-Dfoo=bar" });
		assertEquals("bar", System.getProperty("foo"));
	}

	/**
	 * Test method for {@link org.x2vc.process.CommonOptions#setSystemProperty(java.util.Map)}.
	 */
	@Test
	void testSetSystemProperty_KeyOnly() {
		new CommandLine(new CommonOptions()).parseArgs(new String[] { "-Dfoo" });
		assertNotNull(System.getProperty("foo"));
	}

}
