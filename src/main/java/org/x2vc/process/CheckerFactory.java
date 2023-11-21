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


import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Custom {@link IFactory} to initialize command line processing via dependency injection.
 */
public class CheckerFactory implements IFactory {
	private Injector injector;

	/**
	 * Default constructor.
	 *
	 * @param configuration
	 */
	public CheckerFactory(Config configuration) {
		this.injector = Guice.createInjector(new CheckerModule(configuration),
				TypesafeConfigModule.fromConfigWithPackage(configuration, "org.x2vc"));
	}

	@Override
	public <K> K create(Class<K> aClass) throws Exception {
		try {
			return this.injector.getInstance(aClass);
		} catch (final ConfigurationException ex) {
			// no implementation found in Guice configuration: use fallback
			return CommandLine.defaultFactory().create(aClass);
		}
	}

}
