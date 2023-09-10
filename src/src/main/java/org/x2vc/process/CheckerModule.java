package org.x2vc.process;

import org.x2vc.analysis.DocumentAnalyzer;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.analysis.rules.DirectAttributeCheckRule;
import org.x2vc.analysis.rules.DirectElementCheckRule;
import org.x2vc.analysis.rules.ElementCopyCheckRule;
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
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.stylesheet.IStylesheetPreprocessor;
import org.x2vc.stylesheet.StylesheetManager;
import org.x2vc.stylesheet.StylesheetPreprocessor;
import org.x2vc.stylesheet.extension.IStylesheetExtender;
import org.x2vc.stylesheet.extension.StylesheetExtender;
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

import net.sf.saxon.s9api.Processor;

/**
 * The Guice module to configure the checker application.
 */
public class CheckerModule extends AbstractModule {

	@Override
	protected void configure() {

		// analysis
		bind(IDocumentAnalyzer.class).to(DocumentAnalyzer.class);

		// analysis rules: use a multibinder for the analyzer rules (plugin-like
		// structure)
		final Multibinder<IAnalyzerRule> ruleBinder = Multibinder.newSetBinder(binder(), IAnalyzerRule.class);
		ruleBinder.addBinding().to(DirectAttributeCheckRule.class);
		ruleBinder.addBinding().to(DirectElementCheckRule.class);
		ruleBinder.addBinding().to(ElementCopyCheckRule.class);

		// process
		bind(IWorkerProcessManager.class).to(WorkerProcessManager.class);

		// process commands

		// process tasks
		bind(IDebugObjectWriter.class).to(DebugObjectWriter.class);
		install(new FactoryModuleBuilder().implement(IInitializationTask.class, InitializationTask.class)
			.build(IInitializationTaskFactory.class));
		install(new FactoryModuleBuilder().implement(IRequestProcessingTask.class, RequestProcessingTask.class)
			.build(IRequestProcessingTaskFactory.class));
		install(new FactoryModuleBuilder().implement(IReportGeneratorTask.class, ReportGeneratorTask.class)
			.build(IReportGeneratorTaskFactory.class));

		// processor
		bind(IHTMLDocumentFactory.class).to(HTMLDocumentFactory.class);
		bind(IXSLTProcessor.class).to(XSLTProcessor.class);

		// report
		bind(IVulnerabilityCandidateCollector.class).to(VulnerabilityCandidateCollector.class);
		bind(IReportWriter.class).to(ReportWriter.class);

		// schema
		bind(ISchemaManager.class).to(SchemaManager.class);
		bind(IInitialSchemaGenerator.class).to(InitialSchemaGenerator.class);

		// stylesheet
		bind(IStylesheetManager.class).to(StylesheetManager.class);
		bind(IStylesheetPreprocessor.class).to(StylesheetPreprocessor.class);

		// stylesheet coverage

		// stylesheet extension
		bind(IStylesheetExtender.class).to(StylesheetExtender.class);

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

	}

	@Provides
	static Processor provideProcessor() {
		// TODO Infrastructure: supply XSLT processor configuration
		return new Processor();
	}

}
