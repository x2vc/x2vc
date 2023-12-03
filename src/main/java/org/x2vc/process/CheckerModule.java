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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.process.commands.IProcessDirector;
import org.x2vc.process.commands.IProcessDirectorFactory;
import org.x2vc.process.commands.ProcessDirector;
import org.x2vc.process.tasks.*;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.IModifierCreationCoordinatorFactory;
import org.x2vc.schema.evolution.ModifierCreationCoordinator;
import org.x2vc.schema.evolution.items.EvaluationTreeItemFactory;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactory;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactoryFactory;
import org.x2vc.utilities.xml.*;
import org.x2vc.xml.value.IValueGenerator;
import org.x2vc.xml.value.IValueGeneratorFactory;
import org.x2vc.xml.value.ValueGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;

/**
 * The Guice module to configure the checker application.
 */
public class CheckerModule extends AbstractModule {

	private static final Logger logger = LogManager.getLogger();
	private Config configuration;

	/**
	 * @param configuration
	 */
	public CheckerModule(Config configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void configure() {
		logger.traceEntry();
		configureAnalyzerRules();
		configureFactories();
		logger.traceExit();
	}

	/**
	 * Configures the rules to be used.
	 */
	@SuppressWarnings("unchecked")
	protected void configureAnalyzerRules() {
		logger.traceEntry();
		// use a multibinder for the analyzer rules (plugin-like structure)
		final Multibinder<IAnalyzerRule> ruleBinder = Multibinder.newSetBinder(binder(), IAnalyzerRule.class);
		final List<String> enabledRules = this.configuration.getStringList("x2vc.analysis.enabled_rules");
		enabledRules.forEach(ruleName -> {
			logger.debug("Loading rule implementation {}", ruleName);
			try {
				final Class<? extends IAnalyzerRule> implementingClass = (Class<? extends IAnalyzerRule>) getClass()
					.getClassLoader().loadClass(ruleName);
				ruleBinder.addBinding().to(implementingClass);
			} catch (final ClassNotFoundException e) {
				logger.error("Unable to load class {}.", ruleName, e);
			} catch (final ClassCastException e) {
				logger.error("Class {} does not implement IAnalyzerRule and cannot be used as a rule.", ruleName, e);
			}
		});
		logger.traceExit();
	}

	/**
	 * Configures the generated factories for assisted injection
	 */
	private void configureFactories() {
		logger.traceEntry();

		// process commands
		install(new FactoryModuleBuilder()
			.implement(IProcessDirector.class, ProcessDirector.class)
			.build(IProcessDirectorFactory.class));

		// process tasks
		install(new FactoryModuleBuilder()
			.implement(IInitializationTask.class, InitializationTask.class)
			.build(IInitializationTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IVulnerabilityCheckTask.class, VulnerabilityCheckTask.class)
			.build(IVulnerabilityCheckTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IReportGeneratorTask.class, ReportGeneratorTask.class)
			.build(IReportGeneratorTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(ISchemaEvolutionTask.class, SchemaEvolutionTask.class)
			.build(ISchemaEvolutionTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(ISchemaExplorationTask.class, SchemaExplorationTask.class)
			.build(ISchemaExplorationTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IStaticSchemaAnalysisTask.class, StaticSchemaAnalysisTask.class)
			.build(IStaticSchemaAnalysisTaskFactory.class));

		// schema evolution items
		install(new FactoryModuleBuilder()
			.implement(IEvaluationTreeItemFactory.class, EvaluationTreeItemFactory.class)
			.build(IEvaluationTreeItemFactoryFactory.class));

		// schema evolution
		install(new FactoryModuleBuilder()
			.implement(IModifierCreationCoordinator.class, ModifierCreationCoordinator.class)
			.build(IModifierCreationCoordinatorFactory.class));

		// utilites - XML
		install(new FactoryModuleBuilder()
			.implement(ILocationMap.class, LocationMap.class)
			.build(ILocationMapFactory.class));
		install(new FactoryModuleBuilder()
			.implement(ITagMap.class, TagMap.class)
			.build(ITagMapFactory.class));

		// xml value
		install(new FactoryModuleBuilder()
			.implement(IValueGenerator.class, ValueGenerator.class)
			.build(IValueGeneratorFactory.class));

		logger.traceExit();
	}

	/**
	 * @return the random number generator to use
	 */
	@Provides
	Random provideRandomNumberGenerator() {
		return ThreadLocalRandom.current();
	}

}
