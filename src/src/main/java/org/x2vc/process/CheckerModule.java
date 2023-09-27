package org.x2vc.process;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.DocumentAnalyzer;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.process.commands.IProcessDirector;
import org.x2vc.process.commands.ProcessDirector;
import org.x2vc.process.tasks.*;
import org.x2vc.processor.HTMLDocumentFactory;
import org.x2vc.processor.IHTMLDocumentFactory;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.processor.XSLTProcessor;
import org.x2vc.report.IReportWriter;
import org.x2vc.report.IVulnerabilityCandidateCollector;
import org.x2vc.report.ReportWriter;
import org.x2vc.report.VulnerabilityCandidateCollector;
import org.x2vc.schema.IInitialSchemaGenerator;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.InitialSchemaGenerator;
import org.x2vc.schema.SchemaManager;
import org.x2vc.schema.evolution.ISchemaEvolver;
import org.x2vc.schema.evolution.SchemaEvolver;
import org.x2vc.stylesheet.*;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.stylesheet.structure.StylesheetStructureExtractor;
import org.x2vc.utilities.DebugObjectWriter;
import org.x2vc.utilities.IDebugObjectWriter;
import org.x2vc.xml.document.DocumentGenerator;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.request.CompletedRequestRegistry;
import org.x2vc.xml.request.ICompletedRequestRegistry;
import org.x2vc.xml.request.IRequestGenerator;
import org.x2vc.xml.request.RequestGenerator;
import org.x2vc.xml.value.*;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;

import net.sf.saxon.s9api.Processor;

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

		// analysis
		bind(IDocumentAnalyzer.class).to(DocumentAnalyzer.class);

		// analysis rules
		configureAnalyzerRules();

		// process
		bind(IWorkerProcessManager.class).to(WorkerProcessManager.class);

		// process commands
		bind(IProcessDirector.class).to(ProcessDirector.class);

		// process tasks
		bind(IDebugObjectWriter.class).to(DebugObjectWriter.class);
		install(new FactoryModuleBuilder()
			.implement(IInitializationTask.class, InitializationTask.class)
			.build(IInitializationTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IInitialVulnerabilityCheckTask.class, InitialVulnerabilityCheckTask.class)
			.build(IInitialVulnerabilityCheckTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IFollowUpVulnerabilityCheckTask.class, FollowUpVulnerabilityCheckTask.class)
			.build(IFollowUpVulnerabilityCheckTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(IReportGeneratorTask.class, ReportGeneratorTask.class)
			.build(IReportGeneratorTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(ISchemaEvolutionTask.class, SchemaEvolutionTask.class)
			.build(ISchemaEvolutionTaskFactory.class));
		install(new FactoryModuleBuilder()
			.implement(ISchemaExplorationTask.class, SchemaExplorationTask.class)
			.build(ISchemaExplorationTaskFactory.class));

		// processor
		bind(IHTMLDocumentFactory.class).to(HTMLDocumentFactory.class);
		bind(IXSLTProcessor.class).to(XSLTProcessor.class);

		// report
		bind(IVulnerabilityCandidateCollector.class).to(VulnerabilityCandidateCollector.class);
		bind(IReportWriter.class).to(ReportWriter.class);

		// schema
		bind(ISchemaManager.class).to(SchemaManager.class);
		bind(IInitialSchemaGenerator.class).to(InitialSchemaGenerator.class);
		bind(ISchemaEvolver.class).to(SchemaEvolver.class);

		// stylesheet
		bind(IStylesheetManager.class).to(StylesheetManager.class);
		bind(IStylesheetPreprocessor.class).to(StylesheetPreprocessor.class);
		bind(INamespaceExtractor.class).to(NamespaceExtractor.class);

		// stylesheet coverage

		// stylesheet structure
		bind(IStylesheetStructureExtractor.class).to(StylesheetStructureExtractor.class);

		// xml document
		bind(IDocumentGenerator.class).to(DocumentGenerator.class);

		// xml request
		bind(IRequestGenerator.class).to(RequestGenerator.class);
		bind(ICompletedRequestRegistry.class).to(CompletedRequestRegistry.class);

		// xml value
		bind(IPrefixSelector.class).to(PrefixSelector.class);
		install(new FactoryModuleBuilder().implement(IValueGenerator.class, ValueGenerator.class)
			.build(IValueGeneratorFactory.class));

		logger.traceExit();
	}

	/**
	 * Configures the rules to be used.
	 */
	@SuppressWarnings("unchecked")
	protected void configureAnalyzerRules() {
		logger.traceEntry();
		// use a multibinder for the analyzer rules (plugin-like
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

	@Provides
	static Processor provideProcessor() {
		// TODO Infrastructure: supply XSLT processor configuration
		return new Processor();
	}

}
