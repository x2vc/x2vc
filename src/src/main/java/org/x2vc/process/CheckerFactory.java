package org.x2vc.process;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * Custom {@link IFactory} to initialize dependency injection.
 */
public class CheckerFactory implements IFactory {
	private final Injector injector = Guice.createInjector(new CheckerModule());

	@Override
	public <K> K create(Class<K> aClass) throws Exception {
		try {
			return this.injector.getInstance(aClass);
		} catch (ConfigurationException ex) {
			// no implementation found in Guice configuration: use fallback
			return CommandLine.defaultFactory().create(aClass);
		}
	}

}