package org.x2vc.process.commands;

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

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.*;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.request.IDocumentRequest;

import com.github.racc.tscg.TypesafeConfig;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Standard implementation of {@link IProcessDirector}. Caution: Needs to be hyper-threadsafe, concurrent access
 * everywhere!
 */
public class ProcessDirector implements IProcessDirector {

	private static final Logger logger = LogManager.getLogger();

	private IInitializationTaskFactory initializationTaskFactory;
	private IStaticSchemaAnalysisTaskFactory staticSchemaAnalysisTaskFactory;
	private ISchemaExplorationTaskFactory schemaExplorationTaskFactory;
	private ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory;
	private IVulnerabilityCheckTaskFactory vulnerabilityCheckTaskFactory;
	private IReportGeneratorTaskFactory reportGeneratorTaskFactory;
	private IWorkerProcessManager workerProcessManager;
	private ISchemaModifierCollector schemaModifierCollector;

	private int schemaExplorationDocumentCount;
	private int schemaEvolutionPassLimit;
	private int xssInitialDocumentCount;
	private File xsltFile;
	private ProcessingMode processingMode;
	private ProcessState processState;
	private int currentSchemaIteration = 0;

	private Set<UUID> schemaExplorationTasks = new HashSet<>();
	private int failedExplorationTasks = 0;
	private Set<UUID> vulnerabilityCheckTasks = new HashSet<>();

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	ProcessDirector(IInitializationTaskFactory initializationTaskFactory,
			IStaticSchemaAnalysisTaskFactory staticSchemaAnalysisTaskFactory,
			ISchemaExplorationTaskFactory schemaExplorationTaskFactory,
			ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory,
			IVulnerabilityCheckTaskFactory vulnerabilityCheckTaskFactory,
			IReportGeneratorTaskFactory reportGeneratorTaskFactory,
			IWorkerProcessManager workerProcessManager,
			ISchemaModifierCollector schemaModifierCollector,
			@TypesafeConfig("x2vc.schema.evolve.document_count") Integer schemaExplorationDocumentCount,
			@TypesafeConfig("x2vc.schema.evolve.pass_limit") Integer schemaEvolutionPassLimit,
			@TypesafeConfig("x2vc.xml.initial_documents") Integer xssInitialDocumentCount,
			@Assisted File xsltFile,
			@Assisted ProcessingMode mode) {
		this.initializationTaskFactory = initializationTaskFactory;
		this.staticSchemaAnalysisTaskFactory = staticSchemaAnalysisTaskFactory;
		this.schemaExplorationTaskFactory = schemaExplorationTaskFactory;
		this.schemaEvolutionTaskFactory = schemaEvolutionTaskFactory;
		this.vulnerabilityCheckTaskFactory = vulnerabilityCheckTaskFactory;
		this.reportGeneratorTaskFactory = reportGeneratorTaskFactory;
		this.workerProcessManager = workerProcessManager;
		this.schemaModifierCollector = schemaModifierCollector;
		this.schemaExplorationDocumentCount = schemaExplorationDocumentCount;
		this.schemaEvolutionPassLimit = schemaEvolutionPassLimit;
		this.xssInitialDocumentCount = xssInitialDocumentCount;
		this.xsltFile = xsltFile;
		this.processingMode = mode;
		this.processState = ProcessState.NEW;
	}

	@Override
	public ProcessState getProcessState() {
		return this.processState;
	}

	/**
	 * Prepares a {@link CloseableThreadContext} to annotate the log entries with the stylesheet name.
	 *
	 * @return
	 */
	protected CloseableThreadContext.Instance getThreadContext() {
		return CloseableThreadContext.put("stylesheet", this.xsltFile.toString());
	}

	@Override
	public synchronized void initialize() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("initializing processing of stylesheet {}", this.xsltFile);
			if (this.processState == ProcessState.NEW) {
				this.processState = ProcessState.INITIALIZE;
				final IInitializationTask initTask = this.initializationTaskFactory
					.create(this.xsltFile, this.processingMode, this::handleInitializationComplete);
				this.workerProcessManager.submit(initTask);
			}
			logger.traceExit();
		}
	}

	/**
	 * Called when the initialization phase of the processing has completed.
	 *
	 * @param success whether the initialization was successful
	 */
	private synchronized void handleInitializationComplete(Boolean success) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			if (Boolean.TRUE.equals(success)) {
				logger.debug("processing of stylesheet {} initialized successfully", this.xsltFile);
				if ((this.processingMode == ProcessingMode.FULL)
						|| (this.processingMode == ProcessingMode.SCHEMA_ONLY)) {
					startStaticSchemaAnalysis();
				} else {
					startVulnerabilityChecks();
				}
			} else {
				logger.error("processing of stylesheet {} aborted", this.xsltFile);
				// abort further processing
				this.processState = ProcessState.DONE;
			}
			logger.traceExit();
		}
	}

	/**
	 * Starts the static analysis phase of the schema evolution.
	 */
	private synchronized void startStaticSchemaAnalysis() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			this.processState = ProcessState.STATIC_CHECK;
			this.schemaModifierCollector.clear();
			// this phase only consists of a single task
			logger.debug("scheduling static analysis of stylesheet {}",
					this.xsltFile);
			final IStaticSchemaAnalysisTask analysisTask = this.staticSchemaAnalysisTaskFactory
				.create(this.xsltFile, this.schemaModifierCollector::addModifier, this::handleStaticAnalysisComplete);
			this.workerProcessManager.submit(analysisTask);
			logger.traceExit();
		}
	}

	/**
	 * Called when an {@link ISchemaExplorationTask} completes.
	 *
	 * @param taskID
	 * @param success
	 */
	private synchronized void handleStaticAnalysisComplete(UUID taskID, Boolean success) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			if (Boolean.FALSE.equals(success)) {
				logger.error("processing of stylesheet {} aborted", this.xsltFile);
				// abort further processing
				this.processState = ProcessState.DONE;
			} else {
				logger.debug("static analysis of stylesheet {} completed", this.xsltFile);
				if (this.schemaModifierCollector.isEmpty()) {
					// skip directly to exploration phase
					startSchemaExploration();
				} else {
					// incorporate the results in the schema first
					startSchemaAnalysisResultProcessing();
				}
			}
			logger.traceExit();
		}
	}

	/**
	 * Starts the processing phase of the static analysis.
	 */
	private synchronized void startSchemaAnalysisResultProcessing() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("processing results of static analysis of stylesheet {}", this.xsltFile);
			this.processState = ProcessState.STATIC_RESULT_PROCESSING;
			// we can re-use the evolution task for this - maybe some day find a better name for it
			final ISchemaEvolutionTask evolutionTask = this.schemaEvolutionTaskFactory
				.create(this.xsltFile, this.schemaModifierCollector, this::handleAnalysisResultProcessingComplete);
			this.workerProcessManager.submit(evolutionTask);
			logger.traceExit();
		}
	}

	/**
	 * Called when an {@link ISchemaEvolutionTask} submitted by {@link #startSchemaAnalysisResultProcessing()}
	 * completes.
	 *
	 * @param result <code>true</code> if a new schema version was created, <code>false</code> otherwise
	 */
	private synchronized void handleAnalysisResultProcessingComplete(Boolean result) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("static analysis of stylesheet {} completed, schema changes: {}",
					this.xsltFile, result);
			startSchemaExploration();
			logger.traceExit();
		}
	}

	/**
	 * Starts the exploration phase of the schema evolution.
	 */
	private synchronized void startSchemaExploration() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			this.processState = ProcessState.EXPLORE_SCHEMA;
			this.schemaModifierCollector.clear();
			this.failedExplorationTasks = 0;
			this.schemaExplorationTasks.clear();
			this.currentSchemaIteration++;
			// generate a number of initial document requests
			logger.debug("submitting {} document requests for schema exploration of stylesheet {}",
					this.schemaExplorationDocumentCount,
					this.xsltFile);
			for (int i = 0; i < this.schemaExplorationDocumentCount; i++) {
				final ISchemaExplorationTask explorationTask = this.schemaExplorationTaskFactory
					.create(
							this.xsltFile, this.schemaModifierCollector::addModifier, this::handleExplorationComplete);
				this.schemaExplorationTasks.add(explorationTask.getTaskID());
				this.workerProcessManager.submit(explorationTask);
			}
			logger.traceExit();
		}
	}

	/**
	 * Called when an {@link ISchemaExplorationTask} submitted by {@link #startSchemaExploration()} completes.
	 *
	 * @param taskID
	 * @param success
	 */
	private synchronized void handleExplorationComplete(UUID taskID, Boolean success) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			if (Boolean.FALSE.equals(success)) {
				this.failedExplorationTasks++;
			}
			this.schemaExplorationTasks.remove(taskID);
			if (this.schemaExplorationTasks.isEmpty()) {
				// when the last task has completed, decide what to do
				if (this.failedExplorationTasks == this.schemaExplorationDocumentCount) {
					// something has gone VERY wrong, abort
					logger.error("All {} attempts to explore the schema use of stylesheet {} have failed",
							this.failedExplorationTasks, this.xsltFile);
					this.processState = ProcessState.DONE;
				} else {
					if (this.schemaModifierCollector.isEmpty()) {
						logger.debug("Exploration has not yielded any additional modification requests");
						if (this.processingMode != ProcessingMode.SCHEMA_ONLY) {
							startVulnerabilityChecks();
						} else {
							startReportCompilation();
						}
					} else {
						startSchemaEvolution();
					}
				}
			}
			logger.traceExit();
		}
	}

	/**
	 * Starts the evolution phase of the schema evolution.
	 */
	private synchronized void startSchemaEvolution() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("processing results of schema exploration of stylesheet {}", this.xsltFile);
			this.processState = ProcessState.EVOLVE_SCHEMA;
			final ISchemaEvolutionTask evolutionTask = this.schemaEvolutionTaskFactory
				.create(this.xsltFile, this.schemaModifierCollector, this::handleEvolutionComplete);
			this.workerProcessManager.submit(evolutionTask);
			logger.traceExit();
		}
	}

	/**
	 * Called when an {@link ISchemaEvolutionTask} completes.
	 *
	 * @param result <code>true</code> if a new schema version was created, <code>false</code> otherwise
	 */
	private synchronized void handleEvolutionComplete(Boolean result) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("round {} of {} of schema evolution of stylesheet {} completed, schema changes: {}",
					this.currentSchemaIteration, this.schemaEvolutionPassLimit, this.xsltFile, result);
			if (Boolean.TRUE.equals(result) && (this.currentSchemaIteration < this.schemaEvolutionPassLimit)) {
				// new schema version generated AND maximum count not yet reached - try again, sam
				startSchemaExploration();
			} else {
				if (this.processingMode != ProcessingMode.SCHEMA_ONLY) {
					startVulnerabilityChecks();
				} else {
					startReportCompilation();
				}
			}
			logger.traceExit();
		}
	}

	/**
	 * Starts the vulnerability checks.
	 */
	private synchronized void startVulnerabilityChecks() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			this.processState = ProcessState.CHECK_XSS;
			logger.debug("submitting {} document requests for vulnerability analysis of stylesheet {}",
					this.xssInitialDocumentCount,
					this.xsltFile);
			for (int i = 0; i < this.xssInitialDocumentCount; i++) {
				final IVulnerabilityCheckTask checkTask = this.vulnerabilityCheckTaskFactory
					.create(
							this.xsltFile, this::collectFollowupRequest,
							this::handleVulnerabilityCheckComplete);
				this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
				this.workerProcessManager.submit(checkTask);
			}
			logger.traceExit();
		}
	}

	/**
	 * Handles a follow-up document request emitted by the XSS checks.
	 *
	 * @param request
	 */
	private synchronized void collectFollowupRequest(IDocumentRequest request) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			String ruleName = "(no modifier)";
			final Optional<IDocumentModifier> oModifier = request.getModifier();
			if (oModifier.isPresent()) {
				ruleName = oModifier.get().getAnalyzerRuleID().orElse("(unknown)");
			}
			logger.debug("received follow-up request for rule {}", ruleName);
			final IVulnerabilityCheckTask checkTask = this.vulnerabilityCheckTaskFactory
				.create(this.xsltFile, request, this::collectFollowupRequest,
						this::handleVulnerabilityCheckComplete);
			this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
			this.workerProcessManager.submit(checkTask);
			logger.traceExit();
		}
	}

	/**
	 * Called when all of the XSS checks have been completed.
	 *
	 * @param taskID
	 * @param success
	 */
	private synchronized void handleVulnerabilityCheckComplete(UUID taskID, Boolean success) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			this.vulnerabilityCheckTasks.remove(taskID);
			logger.debug("vulnerability check tasks completed, {} tasks remaining",
					this.vulnerabilityCheckTasks.size());
			if (this.vulnerabilityCheckTasks.isEmpty()) {
				// when the last task has completed, move to the report compilation phase
				startReportCompilation();
			}
			logger.traceExit();
		}
	}

	/**
	 * Starts the report collection phase
	 */
	private synchronized void startReportCompilation() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			logger.debug("compiling report for stylesheet {}", this.xsltFile);
			this.processState = ProcessState.COMPILE_REPORT;
			final IReportGeneratorTask reportTask = this.reportGeneratorTaskFactory
				.create(this.xsltFile, this::handleReportComplete);
			this.workerProcessManager.submit(reportTask);
			logger.traceExit();
		}
	}

	/**
	 * Called when the report generation is complete.
	 *
	 * @param success
	 */
	private synchronized void handleReportComplete(Boolean success) {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			logger.traceEntry();
			this.processState = ProcessState.DONE;
			logger.traceExit();
		}
	}

}
