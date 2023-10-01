package org.x2vc.process.commands;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.*;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
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
	private ISchemaExplorationTaskFactory schemaExplorationTaskFactory;
	private ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory;
	private IInitialVulnerabilityCheckTaskFactory initialVulnerabilityCheckTaskFactory;
	private IFollowUpVulnerabilityCheckTaskFactory followUpVulnerabilityCheckTaskFactory;
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
			ISchemaExplorationTaskFactory schemaExplorationTaskFactory,
			ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory,
			IInitialVulnerabilityCheckTaskFactory initialVulnerabilityCheckTaskFactory,
			IFollowUpVulnerabilityCheckTaskFactory followUpVulnerabilityCheckTaskFactory,
			IReportGeneratorTaskFactory reportGeneratorTaskFactory,
			IWorkerProcessManager workerProcessManager,
			ISchemaModifierCollector schemaModifierCollector,
			@TypesafeConfig("x2vc.schema.evolve.document_count") Integer schemaExplorationDocumentCount,
			@TypesafeConfig("x2vc.schema.evolve.pass_limit") Integer schemaEvolutionPassLimit,
			@TypesafeConfig("x2vc.xml.initial_documents") Integer xssInitialDocumentCount,
			@Assisted File xsltFile,
			@Assisted ProcessingMode mode) {
		this.initializationTaskFactory = initializationTaskFactory;
		this.schemaExplorationTaskFactory = schemaExplorationTaskFactory;
		this.schemaEvolutionTaskFactory = schemaEvolutionTaskFactory;
		this.initialVulnerabilityCheckTaskFactory = initialVulnerabilityCheckTaskFactory;
		this.followUpVulnerabilityCheckTaskFactory = followUpVulnerabilityCheckTaskFactory;
		this.reportGeneratorTaskFactory = reportGeneratorTaskFactory;
		this.workerProcessManager = workerProcessManager;
		this.schemaModifierCollector = schemaModifierCollector;
		this.schemaExplorationDocumentCount = schemaExplorationDocumentCount;
		this.schemaEvolutionPassLimit = schemaEvolutionPassLimit;
		this.xssInitialDocumentCount = xssInitialDocumentCount;
		this.xsltFile = xsltFile;
		this.processingMode = mode;
		this.processState = ProcessState.NEW;
		initialize();
	}

	@Override
	public ProcessState getProcessState() {
		return this.processState;
	}

	/**
	 * Starts the initialization phase of the processing.
	 */
	private synchronized void initialize() {
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

	/**
	 * Called when the initialization phase of the processing has completed.
	 *
	 * @param success whether the initialization was successful
	 */
	private synchronized void handleInitializationComplete(Boolean success) {
		logger.traceEntry();
		if (Boolean.TRUE.equals(success)) {
			logger.debug("processing of stylesheet {} initialized successfully", this.xsltFile);
			if ((this.processingMode == ProcessingMode.FULL)
					|| (this.processingMode == ProcessingMode.SCHEMA_ONLY)) {
				startSchemaExploration();
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

	/**
	 * Starts the exploration phase of the schema evolution.
	 */
	private synchronized void startSchemaExploration() {
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

	/**
	 * Called when an {@link ISchemaExplorationTask} completes.
	 *
	 * @param taskID
	 * @param success
	 */
	private synchronized void handleExplorationComplete(UUID taskID, Boolean success) {
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

	/**
	 * Starts the evolution phase of the schema evolution.
	 */
	private synchronized void startSchemaEvolution() {
		logger.traceEntry();
		logger.debug("processing results of schema exploration of stylesheet {}", this.xsltFile);
		this.processState = ProcessState.EVOLVE_SCHEMA;
		final ISchemaEvolutionTask evolutionTask = this.schemaEvolutionTaskFactory
			.create(this.xsltFile, this.schemaModifierCollector, this::handleEvolutionComplete);
		this.workerProcessManager.submit(evolutionTask);
		logger.traceExit();
	}

	/**
	 * Called when an {@link ISchemaEvolutionTask} completes.
	 *
	 * @param result <code>true</code> if a new schema version was created, <code>false</code> otherwise
	 */
	private synchronized void handleEvolutionComplete(Boolean result) {
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

	/**
	 * Starts the vulnerability checks.
	 */
	private synchronized void startVulnerabilityChecks() {
		logger.traceEntry();
		this.processState = ProcessState.CHECK_XSS;
		logger.debug("submitting {} document requests for vulnerability analysis of stylesheet {}",
				this.xssInitialDocumentCount,
				this.xsltFile);
		for (int i = 0; i < this.xssInitialDocumentCount; i++) {
			final IInitialVulnerabilityCheckTask checkTask = this.initialVulnerabilityCheckTaskFactory
				.create(
						this.xsltFile, this::collectFollowupRequest, this::handleVulnerabilityCheckComplete);
			this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
			this.workerProcessManager.submit(checkTask);
		}

		logger.traceExit();
	}

	/**
	 * Handles a follow-up document request emitted by the XSS checks.
	 *
	 * @param request
	 */
	private synchronized void collectFollowupRequest(IDocumentRequest request) {
		logger.traceEntry();
		final IFollowUpVulnerabilityCheckTask checkTask = this.followUpVulnerabilityCheckTaskFactory
			.create(request, this::collectFollowupRequest, this::handleVulnerabilityCheckComplete);
		this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
		this.workerProcessManager.submit(checkTask);
		logger.traceExit();
	}

	/**
	 * Called when all of the XSS checks have been completed.
	 *
	 * @param taskID
	 * @param success
	 */
	private synchronized void handleVulnerabilityCheckComplete(UUID taskID, Boolean success) {
		logger.traceEntry();
		this.vulnerabilityCheckTasks.remove(taskID);
		logger.trace("{} vulnerability check tasks remaining", this.vulnerabilityCheckTasks.size());
		if (this.vulnerabilityCheckTasks.isEmpty()) {
			// when the last task has completed, move to the report compilation phase
			startReportCompilation();
		}
		logger.traceExit();
	}

	/**
	 * Starts the report collection phase
	 */
	private synchronized void startReportCompilation() {
		logger.traceEntry();
		logger.debug("compiling report for stylesheet {}", this.xsltFile);
		this.processState = ProcessState.COMPILE_REPORT;
		final IReportGeneratorTask reportTask = this.reportGeneratorTaskFactory
			.create(this.xsltFile, this::handleReportComplete);
		this.workerProcessManager.submit(reportTask);
		logger.traceExit();
	}

	/**
	 * Called when the report generation is complete.
	 *
	 * @param success
	 */
	private synchronized void handleReportComplete(Boolean success) {
		logger.traceEntry();
		this.processState = ProcessState.DONE;
		logger.traceExit();
	}

}
