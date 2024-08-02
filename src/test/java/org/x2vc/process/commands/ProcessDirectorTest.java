/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 - 2024 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.process.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.*;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
import org.x2vc.xml.request.IDocumentRequest;

@ExtendWith(MockitoExtension.class)
class ProcessDirectorTest {

	@Mock
	private IInitializationTaskFactory initializationTaskFactory;
	@Mock
	private IInitializationTask initializationTask;
	private UUID initializationTaskID;
	private Consumer<Boolean> initializationTaskCallback;

	@Mock
	private IStaticSchemaAnalysisTaskFactory staticSchemaAnalysisTaskFactory;
	@Mock
	private IStaticSchemaAnalysisTask staticSchemaAnalysisTask;
	private UUID staticSchemaAnalysisTaskID;
	@SuppressWarnings("unused")
	private Consumer<ISchemaModifier> staticSchemaAnalysisTaskModifierCollector;
	private BiConsumer<UUID, Boolean> staticSchemaAnalysisTaskCallback;

	@Mock
	private ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory;
	@Mock
	private ISchemaEvolutionTask schemaEvolutionTask;
	private UUID schemaEvolutionTaskID;
	private Consumer<Boolean> schemaEvolutionTaskCallback;

	@Mock
	private ISchemaExplorationTaskFactory schemaExplorationTaskFactory;
	@Mock
	private ISchemaExplorationTask schemaExplorationTask;
	private UUID schemaExplorationTaskID;
	@SuppressWarnings("unused")
	private Consumer<ISchemaModifier> schemaExplorationTaskModifierCollector;
	private BiConsumer<UUID, Boolean> schemaExplorationTaskCallback;

	@Mock
	private IVulnerabilityCheckTaskFactory vulnerabilityCheckTaskFactory;
	@Mock
	private IVulnerabilityCheckTask vulnerabilityCheckTask;
	private UUID vulnerabilityCheckTaskID;
	@SuppressWarnings("unused")
	private Consumer<IDocumentRequest> vulnerabilityCheckTaskRequestCollector;
	private BiConsumer<UUID, Boolean> vulnerabilityCheckTaskCallback;

	@Mock
	private IReportGeneratorTaskFactory reportGeneratorTaskFactory;
	@Mock
	private IReportGeneratorTask reportGeneratorTask;
	private UUID reportGeneratorTaskID;
	private Consumer<Boolean> reportGeneratorTaskCallback;

	@Mock
	private IWorkerProcessManager workerProcessManager;
	@Mock
	private ISchemaModifierCollector schemaModifierCollector;

	@Mock
	private File xsltFile;

	private ProcessingMode processingMode;

	private static final Integer SCHEMA_EXPLORATION_DOCUMENT_COUNT = 1;
	private static final Integer SCHEMA_EVOLUTION_PASS_LIMIT = 1;
	private static final Integer XSS_INITIAL_DOCUMENT_COUNT = 1;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() {
		this.initializationTaskID = UUID.randomUUID();
		lenient().when(this.initializationTask.getTaskID()).thenReturn(this.initializationTaskID);
		lenient().when(this.initializationTaskFactory.create(any(), any(), any()))
			.thenReturn(this.initializationTask);

		this.staticSchemaAnalysisTaskID = UUID.randomUUID();
		lenient().when(this.staticSchemaAnalysisTask.getTaskID()).thenReturn(this.staticSchemaAnalysisTaskID);
		lenient().when(this.staticSchemaAnalysisTaskFactory.create(any(), any(), any()))
			.thenReturn(this.staticSchemaAnalysisTask);

		this.schemaEvolutionTaskID = UUID.randomUUID();
		lenient().when(this.schemaEvolutionTask.getTaskID()).thenReturn(this.schemaEvolutionTaskID);
		lenient().when(this.schemaEvolutionTaskFactory.create(any(), any(), any()))
			.thenReturn(this.schemaEvolutionTask);

		this.schemaExplorationTaskID = UUID.randomUUID();
		lenient().when(this.schemaExplorationTask.getTaskID()).thenReturn(this.schemaExplorationTaskID);
		lenient().when(this.schemaExplorationTaskFactory.create(any(), any(), any()))
			.thenReturn(this.schemaExplorationTask);

		this.vulnerabilityCheckTaskID = UUID.randomUUID();
		lenient().when(this.vulnerabilityCheckTask.getTaskID()).thenReturn(this.vulnerabilityCheckTaskID);
		lenient().when(this.vulnerabilityCheckTaskFactory.create(any(), any(), any()))
			.thenReturn(this.vulnerabilityCheckTask);

		this.reportGeneratorTaskID = UUID.randomUUID();
		lenient().when(this.reportGeneratorTask.getTaskID()).thenReturn(this.reportGeneratorTaskID);
		lenient().when(this.reportGeneratorTaskFactory.create(any(), any()))
			.thenReturn(this.reportGeneratorTask);
	}

	/**
	 * @param mode
	 * @return a newly created {@link IProcessDirector} instance configured with the {@link ProcessingMode} supplied
	 */
	private ProcessDirector createDirector(ProcessingMode mode) {
		this.processingMode = mode;
		final var director = new ProcessDirector(this.initializationTaskFactory, this.staticSchemaAnalysisTaskFactory,
				this.schemaExplorationTaskFactory, this.schemaEvolutionTaskFactory, this.vulnerabilityCheckTaskFactory,
				this.reportGeneratorTaskFactory, this.workerProcessManager, this.schemaModifierCollector,
				ProcessDirectorTest.SCHEMA_EXPLORATION_DOCUMENT_COUNT, ProcessDirectorTest.SCHEMA_EVOLUTION_PASS_LIMIT,
				ProcessDirectorTest.XSS_INITIAL_DOCUMENT_COUNT, this.xsltFile, mode);
		assertEquals(ProcessState.NEW, director.getProcessState(), "wrong initial process state");
		return director;
	}

	/**
	 * @param director
	 * @param callbackResult
	 */
	private void simulateInitialization(final ProcessDirector director, boolean callbackResult) {
		director.initialize();
		assertEquals(ProcessState.INITIALIZE, director.getProcessState(),
				"wrong process state before initialization task callback");

		verify(this.workerProcessManager,
				description("initialization task must be submitted to worker process manager"))
			.submit(this.initializationTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<Boolean>> initializationTaskCallbackCaptor = ArgumentCaptor.captor();
		verify(this.initializationTaskFactory).create(same(this.xsltFile), eq(this.processingMode),
				initializationTaskCallbackCaptor.capture());
		assertNotNull(initializationTaskCallbackCaptor.getValue());
		this.initializationTaskCallback = initializationTaskCallbackCaptor.getValue();
		clearInvocations(this.initializationTaskFactory);

		this.initializationTaskCallback.accept(callbackResult);
	}

	/**
	 * @param director
	 * @param callbackResult
	 */
	private void simulateStaticSchemaAnalysis(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("static schema analysis task must be submitted to worker process manager"))
			.submit(this.staticSchemaAnalysisTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<ISchemaModifier>> staticSchemaAnalysisTaskModifierCollectorCaptor = ArgumentCaptor
			.captor();
		final ArgumentCaptor<BiConsumer<UUID, Boolean>> staticSchemaAnalysisTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.staticSchemaAnalysisTaskFactory).create(same(this.xsltFile),
				staticSchemaAnalysisTaskModifierCollectorCaptor.capture(),
				staticSchemaAnalysisTaskCallbackCaptor.capture());
		assertNotNull(staticSchemaAnalysisTaskModifierCollectorCaptor.getValue());
		this.staticSchemaAnalysisTaskModifierCollector = staticSchemaAnalysisTaskModifierCollectorCaptor.getValue();
		assertNotNull(staticSchemaAnalysisTaskCallbackCaptor.getValue());
		this.staticSchemaAnalysisTaskCallback = staticSchemaAnalysisTaskCallbackCaptor.getValue();
		clearInvocations(this.staticSchemaAnalysisTaskFactory);

		this.staticSchemaAnalysisTaskCallback.accept(this.staticSchemaAnalysisTaskID, callbackResult);
	}

	/**
	 * @param director
	 * @param callbackResult
	 */
	private void simulateSchemaAnalysisResultProcessing(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("analysis task must be submitted to worker process manager"))
			.submit(this.schemaEvolutionTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<Boolean>> schemaEvolutionTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.schemaEvolutionTaskFactory).create(same(this.xsltFile), same(this.schemaModifierCollector),
				schemaEvolutionTaskCallbackCaptor.capture());
		assertNotNull(schemaEvolutionTaskCallbackCaptor.getValue());
		this.schemaEvolutionTaskCallback = schemaEvolutionTaskCallbackCaptor.getValue();
		clearInvocations(this.schemaEvolutionTaskFactory);

		this.schemaEvolutionTaskCallback.accept(callbackResult);
	}

	/**
	 * @param director
	 * @param callbackResult
	 */
	private void simulateSchemaExploration(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("schema exploration task must be submitted to worker process manager"))
			.submit(this.schemaExplorationTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<ISchemaModifier>> schemaExplorationTaskModifierCollectorCaptor = ArgumentCaptor
			.captor();
		final ArgumentCaptor<BiConsumer<UUID, Boolean>> schemaExplorationTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.schemaExplorationTaskFactory).create(same(this.xsltFile),
				schemaExplorationTaskModifierCollectorCaptor.capture(),
				schemaExplorationTaskCallbackCaptor.capture());
		assertNotNull(schemaExplorationTaskModifierCollectorCaptor.getValue());
		this.schemaExplorationTaskModifierCollector = schemaExplorationTaskModifierCollectorCaptor.getValue();
		assertNotNull(schemaExplorationTaskCallbackCaptor.getValue());
		this.schemaExplorationTaskCallback = schemaExplorationTaskCallbackCaptor.getValue();
		clearInvocations(this.schemaExplorationTaskFactory);

		this.schemaExplorationTaskCallback.accept(this.schemaExplorationTaskID, callbackResult);
	}

	/**
	 * @param director
	 * @param callbackResult
	 */
	private void simulateSchemaEvolution(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("evolution task must be submitted to worker process manager"))
			.submit(this.schemaEvolutionTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<Boolean>> schemaEvolutionTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.schemaEvolutionTaskFactory).create(same(this.xsltFile), same(this.schemaModifierCollector),
				schemaEvolutionTaskCallbackCaptor.capture());
		assertNotNull(schemaEvolutionTaskCallbackCaptor.getValue());
		this.schemaEvolutionTaskCallback = schemaEvolutionTaskCallbackCaptor.getValue();
		clearInvocations(this.schemaEvolutionTaskFactory);

		this.schemaEvolutionTaskCallback.accept(callbackResult);
	}

	/**
	 * @param callbackResult
	 */
	private void simulateVulnerabilityCheck(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("vulnerability check task must be submitted to worker process manager"))
			.submit(this.vulnerabilityCheckTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<IDocumentRequest>> vulnerabilityCheckTaskRequestCollectorCaptor = ArgumentCaptor
			.captor();
		final ArgumentCaptor<BiConsumer<UUID, Boolean>> vulnerabilityCheckTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.vulnerabilityCheckTaskFactory).create(same(this.xsltFile),
				vulnerabilityCheckTaskRequestCollectorCaptor.capture(),
				vulnerabilityCheckTaskCallbackCaptor.capture());
		assertNotNull(vulnerabilityCheckTaskRequestCollectorCaptor.getValue());
		this.vulnerabilityCheckTaskRequestCollector = vulnerabilityCheckTaskRequestCollectorCaptor.getValue();
		assertNotNull(vulnerabilityCheckTaskCallbackCaptor.getValue());
		this.vulnerabilityCheckTaskCallback = vulnerabilityCheckTaskCallbackCaptor.getValue();
		clearInvocations(this.schemaExplorationTaskFactory);

		this.vulnerabilityCheckTaskCallback.accept(this.vulnerabilityCheckTaskID, callbackResult);
	}

	/**
	 * @param callbackResult
	 */
	private void simulateReportCompilation(boolean callbackResult) {
		verify(this.workerProcessManager,
				description("report compilation task must be submitted to worker process manager"))
			.submit(this.reportGeneratorTask);
		clearInvocations(this.workerProcessManager);

		final ArgumentCaptor<Consumer<Boolean>> reportGeneratorTaskCallbackCaptor = ArgumentCaptor
			.captor();
		verify(this.reportGeneratorTaskFactory).create(same(this.xsltFile),
				reportGeneratorTaskCallbackCaptor.capture());
		assertNotNull(reportGeneratorTaskCallbackCaptor.getValue());
		this.reportGeneratorTaskCallback = reportGeneratorTaskCallbackCaptor.getValue();
		clearInvocations(this.schemaEvolutionTaskFactory);

		this.reportGeneratorTaskCallback.accept(callbackResult);
	}

	/**
	 * Test method for {@link org.x2vc.process.commands.ProcessDirector}: Abort processing due to failed
	 * {@link IInitializationTask} execution.
	 */
	@ParameterizedTest
	@EnumSource(value = ProcessingMode.class)
	void testFailedInitialization(ProcessingMode mode) {
		final var director = createDirector(mode);
		director.initialize();
		simulateInitialization(director, false);
		assertEquals(ProcessState.DONE, director.getProcessState());
	}

	/**
	 * Test method for {@link org.x2vc.process.commands.ProcessDirector}: Abort processing due to failed
	 * {@link IStaticSchemaAnalysisTask} execution.
	 */
	@ParameterizedTest
	@EnumSource(value = ProcessingMode.class, names = { "FULL", "SCHEMA_ONLY" })
	void testFailedStaticSchemaAnalysis(ProcessingMode mode) {
		final var director = createDirector(mode);
		director.initialize();
		simulateInitialization(director, true);
		assertNotEquals(ProcessState.DONE, director.getProcessState());
		simulateStaticSchemaAnalysis(false);
		assertEquals(ProcessState.DONE, director.getProcessState());
	}

	/**
	 * Test method for {@link org.x2vc.process.commands.ProcessDirector}: Abort processing due to failed
	 * {@link ISchemaExplorationTask} execution.
	 */
	@ParameterizedTest
	@EnumSource(value = ProcessingMode.class, names = { "FULL", "SCHEMA_ONLY" })
	void testFailedSchemaExploration(ProcessingMode mode) {
		final var director = createDirector(mode);
		director.initialize();
		simulateInitialization(director, true);
		assertNotEquals(ProcessState.DONE, director.getProcessState());
		simulateStaticSchemaAnalysis(true);
		assertNotEquals(ProcessState.DONE, director.getProcessState());
		simulateSchemaAnalysisResultProcessing(true);
		assertNotEquals(ProcessState.DONE, director.getProcessState());
		simulateSchemaExploration(false);
		assertEquals(ProcessState.DONE, director.getProcessState());
	}

	/**
	 * Test method for {@link org.x2vc.process.commands.ProcessDirector}: Normal process flow
	 *
	 * @param mode
	 * @param staticSchemaModificationsCollected
	 * @param staticSchemaVersionCreated         (does not make a difference)
	 */
	@ParameterizedTest
	@CsvSource({
			"FULL, true, true, true",
			"FULL, true, true, false",
			"FULL, true, false, true",
			"FULL, true, false, false",
			"FULL, false, true, true",
			"FULL, false, true, false",
			"FULL, false, false, true",
			"FULL, false, false, false",
			"SCHEMA_ONLY, true, true, true",
			"SCHEMA_ONLY, true, true, false",
			"SCHEMA_ONLY, true, false, true",
			"SCHEMA_ONLY, true, false, false",
			"SCHEMA_ONLY, false, true, true",
			"SCHEMA_ONLY, false, true, false",
			"SCHEMA_ONLY, false, false, true",
			"SCHEMA_ONLY, false, false, false",
			"XSS_ONLY, false, false, false"
	})
	void testNoErrors(ProcessingMode mode, boolean staticSchemaModificationsCollected,
			boolean staticSchemaVersionCreated, boolean dynamicSchemaVersionCreated) {
		final var director = createDirector(mode);
		lenient().when(this.schemaModifierCollector.isEmpty())
			.thenReturn(!staticSchemaModificationsCollected)
			.thenReturn(!dynamicSchemaVersionCreated);
		switch (mode) {
		case FULL:
			simulateInitialization(director, true);
			assertEquals(ProcessState.STATIC_CHECK, director.getProcessState());
			simulateStaticSchemaAnalysis(true);
			if (staticSchemaModificationsCollected) {
				assertEquals(ProcessState.STATIC_RESULT_PROCESSING, director.getProcessState());
				simulateSchemaAnalysisResultProcessing(staticSchemaVersionCreated);
			}
			assertEquals(ProcessState.EXPLORE_SCHEMA, director.getProcessState());
			simulateSchemaExploration(true);
			if (dynamicSchemaVersionCreated) {
				assertEquals(ProcessState.EVOLVE_SCHEMA, director.getProcessState());
				simulateSchemaEvolution(false); // do not return to exploration phase
			}
			assertEquals(ProcessState.CHECK_XSS, director.getProcessState());
			simulateVulnerabilityCheck(true);
			assertEquals(ProcessState.COMPILE_REPORT, director.getProcessState());
			simulateReportCompilation(true);
			assertEquals(ProcessState.DONE, director.getProcessState());
			break;

		case SCHEMA_ONLY:
			simulateInitialization(director, true);
			assertEquals(ProcessState.STATIC_CHECK, director.getProcessState());
			simulateStaticSchemaAnalysis(true);
			if (staticSchemaModificationsCollected) {
				assertEquals(ProcessState.STATIC_RESULT_PROCESSING, director.getProcessState());
				simulateSchemaAnalysisResultProcessing(staticSchemaVersionCreated);
			}
			assertEquals(ProcessState.EXPLORE_SCHEMA, director.getProcessState());
			simulateSchemaExploration(true);
			if (dynamicSchemaVersionCreated) {
				assertEquals(ProcessState.EVOLVE_SCHEMA, director.getProcessState());
				simulateSchemaEvolution(false); // do not return to exploration phase
			}
			assertEquals(ProcessState.COMPILE_REPORT, director.getProcessState());
			simulateReportCompilation(true);
			assertEquals(ProcessState.DONE, director.getProcessState());
			break;

		case XSS_ONLY:
			simulateInitialization(director, true);
			assertEquals(ProcessState.CHECK_XSS, director.getProcessState());
			simulateVulnerabilityCheck(true);
			assertEquals(ProcessState.COMPILE_REPORT, director.getProcessState());
			simulateReportCompilation(true);
			assertEquals(ProcessState.DONE, director.getProcessState());
			break;
		}
	}

}
