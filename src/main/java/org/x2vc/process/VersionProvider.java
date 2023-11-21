package org.x2vc.process;

import com.google.common.base.Strings;

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

import picocli.CommandLine.IVersionProvider;

/**
 * Auxiliary class to provide the command line version dynamically.
 */
public class VersionProvider implements IVersionProvider {

	@Override
	public String[] getVersion() throws Exception {
		String implementationVersion = getClass().getPackage().getImplementationVersion();
		if (Strings.isNullOrEmpty(implementationVersion)) {
			implementationVersion = "?.?.?-SNAPSHOT";
		}
		return new String[] {
				String.format("x2vc version %s", implementationVersion)
		};
	}

}
