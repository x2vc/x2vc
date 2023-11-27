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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.process.commands.IProcessDirectorFactory;
import org.x2vc.process.commands.IProcessDirectorManager;
import org.x2vc.process.tasks.*;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.report.IReportWriter;
import org.x2vc.report.IVulnerabilityCandidateCollector;
import org.x2vc.schema.IInitialSchemaGenerator;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.*;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactoryFactory;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.INamespaceExtractor;
import org.x2vc.stylesheet.IStylesheetPreprocessor;
import org.x2vc.stylesheet.coverage.ICoverageTraceAnalyzer;
import org.x2vc.stylesheet.structure.IStylesheetStructureExtractor;
import org.x2vc.utilities.IDebugObjectWriter;
import org.x2vc.utilities.xml.ILocationMapFactory;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.request.ICompletedRequestRegistry;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;
import org.x2vc.xml.value.IPrefixSelector;
import org.x2vc.xml.value.IValueGeneratorFactory;

import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.thedeanda.lorem.LoremIpsum;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@ExtendWith(MockitoExtension.class)
class CheckerModuleTest {

	// see also https://stackoverflow.com/questions/26710191/how-to-test-implementations-of-guice-abstractmodule

	private Config config;

	@Inject
	private Provider<Set<IAnalyzerRule>> ruleProvider;
	@Inject
	private Provider<IDocumentAnalyzer> documentAnalyzerProvider;
	@Inject
	private Provider<IWorkerProcessManager> workerProcessManagerProvider;
	@Inject
	private Provider<IProcessDirectorManager> processDirectorManagerProvider;
	@Inject
	private Provider<IProcessDirectorFactory> processDirectorFactoryProvider;
	@Inject
	private Provider<IDebugObjectWriter> debugObjectWriterProvider;
	@Inject
	private Provider<IInitializationTaskFactory> initializationTaskFactoryProvider;
	@Inject
	private Provider<IVulnerabilityCheckTaskFactory> vulnerabilityCheckTaskFactoryProvider;
	@Inject
	private Provider<IReportGeneratorTaskFactory> reportGeneratorTaskFactoryProvider;
	@Inject
	private Provider<ISchemaEvolutionTaskFactory> schemaEvolutionTaskFactoryProvider;
	@Inject
	private Provider<ISchemaExplorationTaskFactory> schemaExplorationTaskFactoryProvider;
	@Inject
	private Provider<IStaticSchemaAnalysisTaskFactory> staticSchemaAnalysisTaskFactoryProvider;
	@Inject
	private Provider<IXSLTProcessor> xsltProcessorProvider;
	@Inject
	private Provider<IReportWriter> reportWriterProvider;
	@Inject
	private Provider<IVulnerabilityCandidateCollector> vulnerabilityCandidateCollectorProvider;
	@Inject
	private Provider<IEvaluationTreeItemFactoryFactory> evaluationTreeItemFactoryFactoryProvider;
	@Inject
	private Provider<IModifierCreationCoordinatorFactory> modifierCreationCoordinatorFactoryProvider;
	@Inject
	private Provider<ISchemaModificationProcessor> schemaModificationProcessorProvider;
	@Inject
	private Provider<ISchemaModifierCollector> schemaModifierCollectorProvider;
	@Inject
	private Provider<IStaticStylesheetAnalyzer> staticStylesheetAnalyzerProvider;
	@Inject
	private Provider<IValueTraceAnalyzer> valueTraceAnalyzerProvider;
	@Inject
	private Provider<IValueTracePreprocessor> valueTracePreprocessorProvider;
	@Inject
	private Provider<ISchemaManager> schemaManagerProvider;
	@Inject
	private Provider<IInitialSchemaGenerator> initialSchemaGeneratorProvider;
	@Inject
	private Provider<IStylesheetPreprocessor> stylesheetPreprocessorProvider;
	@Inject
	private Provider<INamespaceExtractor> namespaceExtractorProvider;
	@Inject
	private Provider<ICoverageTraceAnalyzer> coverageTraceAnalyzerProvider;
	@Inject
	private Provider<IStylesheetStructureExtractor> stylesheetStructureExtractorProvider;
	@Inject
	private Provider<ILocationMapFactory> locationMapFactoryProvider;
	@Inject
	private Provider<IDocumentGenerator> documentGeneratorProvider;
	@Inject
	private Provider<IRequestGenerator> requestGeneratorProvider;
	@Inject
	private Provider<ICompletedRequestRegistry> completedRequestRegistryProvider;
	@Inject
	private Provider<IPrefixSelector> prefixSelectorProvider;
	@Inject
	private Provider<IValueGeneratorFactory> valueGeneratorFactory;
	@Inject
	private Provider<Random> randomProvider;
	@Inject
	private Provider<LoremIpsum> loremIpsumProvider;

	@BeforeEach
	void setUp() throws Exception {
		this.config = ConfigFactory.load();
		Guice.createInjector(new CheckerModule(this.config),
				TypesafeConfigModule.fromConfigWithPackage(this.config, "org.x2vc"))
			.injectMembers(this);
	}

	@Test
	void testAnalyzerRules() {
		final Set<IAnalyzerRule> rules = this.ruleProvider.get();
		assertNotNull(rules);
		assertFalse(rules.isEmpty());
		// check whether all rules are present
		final List<String> ruleClassNames = rules.stream()
			.map(rule -> rule.getClass().getSimpleName())
			.sorted()
			.toList();
		assertEquals(List.of(
				"CSSAttributeCheckRule",
				"CSSBlockCheckRule",
				"CSSURLCheckRule",
				"DirectAttributeCheckRule",
				"DirectElementCheckRule",
				"DisabledOutputEscapingCheckRule",
				"ElementCopyCheckRule",
				"GeneralURLCheckRule",
				"JavascriptBlockCheckRule",
				"JavascriptHandlerCheckRule",
				"JavascriptURLCheckRule"), ruleClassNames);
	}

	@Test
	void testDocumentAnalyzer() {
		assertNotNull(this.documentAnalyzerProvider.get());
	}

	@Test
	void testWorkerProcessManager() {
		assertNotNull(this.workerProcessManagerProvider.get());
	}

	@Test
	void testProcessDirectorManager() {
		assertNotNull(this.processDirectorManagerProvider.get());
	}

	@Test
	void testProcessDirectorFactory() {
		final IProcessDirectorFactory factory = this.processDirectorFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(ProcessingMode.class)));
	}

	@Test
	void testDebugObjectWriter() {
		assertNotNull(this.debugObjectWriterProvider.get());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testInitializationTaskFactory() {
		final IInitializationTaskFactory factory = this.initializationTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(ProcessingMode.class), mock(Consumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testVulnerabilityCheckTaskFactory_Initial() {
		final IVulnerabilityCheckTaskFactory factory = this.vulnerabilityCheckTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(Consumer.class), mock(BiConsumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testVulnerabilityCheckTaskFactory_FollowUp() {
		final IVulnerabilityCheckTaskFactory factory = this.vulnerabilityCheckTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(IDocumentRequest.class), mock(Consumer.class),
				mock(BiConsumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testReportGeneratorTaskFactory() {
		final IReportGeneratorTaskFactory factory = this.reportGeneratorTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(Consumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSchemaEvolutionTaskFactory() {
		final ISchemaEvolutionTaskFactory factory = this.schemaEvolutionTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(ISchemaModifierCollector.class), mock(Consumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSchemaExplorationTaskFactory() {
		final ISchemaExplorationTaskFactory factory = this.schemaExplorationTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(Consumer.class), mock(BiConsumer.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testStaticSchemaAnalysisTaskFactory() {
		final IStaticSchemaAnalysisTaskFactory factory = this.staticSchemaAnalysisTaskFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(mock(File.class), mock(Consumer.class), mock(BiConsumer.class)));
	}

	@Test
	void testXSLTProcessor() {
		assertNotNull(this.xsltProcessorProvider.get());
	}

	@Test
	void testReportWriter() {
		assertNotNull(this.reportWriterProvider.get());
	}

	@Test
	void testVulnerabilityCandidateCollector() {
		assertNotNull(this.vulnerabilityCandidateCollectorProvider.get());
	}

	@Test
	void testEvaluationTreeItemFactoryFactoryProvider() {
		final IEvaluationTreeItemFactoryFactory factory = this.evaluationTreeItemFactoryFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.createFactory(mock(IXMLSchema.class), mock(IModifierCreationCoordinator.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	void testIModifierCreationCoordinatorFactory() {
		final IModifierCreationCoordinatorFactory factory = this.modifierCreationCoordinatorFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.createCoordinator(mock(IXMLSchema.class), mock(Consumer.class)));
	}

	@Test
	void testSchemaModificationProcessor() {
		assertNotNull(this.schemaModificationProcessorProvider.get());
	}

	@Test
	void testSchemaModifierCollector() {
		assertNotNull(this.schemaModifierCollectorProvider.get());
	}

	@Test
	void testStaticStylesheetAnalyzer() {
		assertNotNull(this.staticStylesheetAnalyzerProvider.get());
	}

	@Test
	void testValueTraceAnalyzer() {
		assertNotNull(this.valueTraceAnalyzerProvider.get());
	}

	@Test
	void testValueTracePreprocessor() {
		assertNotNull(this.valueTracePreprocessorProvider.get());
	}

	@Test
	void testSchemaManager() {
		assertNotNull(this.schemaManagerProvider.get());
	}

	@Test
	void testInitialSchemaGenerator() {
		assertNotNull(this.initialSchemaGeneratorProvider.get());
	}

	@Test
	void testStylesheetPreprocessor() {
		assertNotNull(this.stylesheetPreprocessorProvider.get());
	}

	@Test
	void testNamespaceExtractor() {
		assertNotNull(this.namespaceExtractorProvider.get());
	}

	@Test
	void testCoverageTraceAnalyzer() {
		assertNotNull(this.coverageTraceAnalyzerProvider.get());
	}

	@Test
	void testStylesheetStructureExtractor() {
		assertNotNull(this.stylesheetStructureExtractorProvider.get());
	}

	@Test
	void testLocationMapFactory() {
		final ILocationMapFactory factory = this.locationMapFactoryProvider.get();
		assertNotNull(factory);
		assertNotNull(factory.create(42, new int[] { 1, 2, 3 }, new int[] { 0, 4, 5 }));
	}

	@Test
	void testDocumentGenerator() {
		assertNotNull(this.documentGeneratorProvider.get());
	}

	@Test
	void testRequestGenerator() {
		assertNotNull(this.requestGeneratorProvider.get());
	}

	@Test
	void testCompletedRequestRegistry() {
		assertNotNull(this.completedRequestRegistryProvider.get());
	}

	@Test
	void testPrefixSelector() {
		assertNotNull(this.prefixSelectorProvider.get());
	}

	@Test
	void testValueGeneratorFactory() {
		final IValueGeneratorFactory factory = this.valueGeneratorFactory.get();
		assertNotNull(factory);
		assertNotNull(factory.createValueGenerator(mock(IDocumentRequest.class)));
	}

	@Test
	void testRandomProvider() {
		final Random rng = this.randomProvider.get();
		assertNotNull(rng);
		assertInstanceOf(ThreadLocalRandom.class, rng);
	}

	@Test
	void testLoremIpsumProvider() {
		final LoremIpsum li = this.loremIpsumProvider.get();
		assertNotNull(li);
		assertInstanceOf(LoremIpsum.class, li);
	}

}
