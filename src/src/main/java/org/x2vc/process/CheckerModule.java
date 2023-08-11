package org.x2vc.process;

import org.x2vc.analysis.DocumentAnalyzer;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.analysis.rules.DirectAttributeCheckRule;
import org.x2vc.processor.HTMLDocumentFactory;
import org.x2vc.processor.IHTMLDocumentFactory;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.processor.XSLTProcessor;
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
import org.x2vc.xml.request.IRequestGenerator;
import org.x2vc.xml.request.RequestGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

import net.sf.saxon.s9api.Processor;

/**
 * The Guice module to configure the checker application.
 */
public class CheckerModule extends AbstractModule {

	@Override
	protected void configure() {

		// use a multibinder for the analyzer rules (plugin-like structure)
		final Multibinder<IAnalyzerRule> ruleBinder = Multibinder.newSetBinder(binder(), IAnalyzerRule.class);
		ruleBinder.addBinding().to(DirectAttributeCheckRule.class);

		// analysis
		bind(IDocumentAnalyzer.class).to(DocumentAnalyzer.class);

		// process

		// processor
		bind(IHTMLDocumentFactory.class).to(HTMLDocumentFactory.class);
		bind(IXSLTProcessor.class).to(XSLTProcessor.class);

		// schema
		bind(ISchemaManager.class).to(SchemaManager.class);
		bind(IInitialSchemaGenerator.class).to(InitialSchemaGenerator.class);

		// stylesheet
		bind(IStylesheetManager.class).to(StylesheetManager.class);
		bind(IStylesheetPreprocessor.class).to(StylesheetPreprocessor.class);
		bind(IStylesheetExtender.class).to(StylesheetExtender.class);
		bind(IStylesheetStructureExtractor.class).to(StylesheetStructureExtractor.class);

		// xmldoc
		bind(IRequestGenerator.class).to(RequestGenerator.class);

	}

	@Provides
	static Processor provideProcessor() {
		// TODO Infrastructure: supply XSLT processor configuration
		return new Processor();
	}

}
