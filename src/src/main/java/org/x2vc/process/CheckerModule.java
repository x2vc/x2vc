package org.x2vc.process;

import org.x2vc.stylesheet.IStylesheetPreprocessor;
import org.x2vc.stylesheet.StylesheetPreprocessor;
import org.x2vc.stylesheet.extension.IStylesheetExtender;
import org.x2vc.stylesheet.extension.StylesheetExtender;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.stylesheet.structure.StylesheetStructureExtractor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import net.sf.saxon.s9api.Processor;

/**
 * The Guice module to configure the checker application.
 */
public class CheckerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IStylesheetPreprocessor.class).to(StylesheetPreprocessor.class);
		bind(IStylesheetExtender.class).to(StylesheetExtender.class);
		bind(IStylesheetStructureExtractor.class).to(StylesheetStructureExtractor.class);
	}

	@Provides
	static Processor provideProcessor() {
		// TODO supply XSLT processor configuration
		return new Processor();
	}

}
