package org.x2vc.process.commands;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.*;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.xml.request.IDocumentRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IProcessDirector}. Caution: Needs to be hyper-threadsafe, concurrent access
 * everywhere!
 */
@Singleton
public class ProcessDirector implements IProcessDirector {

	private static final Logger logger = LogManager.getLogger();

	private IInitializationTaskFactory initializationTaskFactory;
	private ISchemaExplorationTaskFactory schemaExplorationTaskFactory;
	private ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory;
	private IInitialVulnerabilityCheckTaskFactory initialVulnerabilityCheckTaskFactory;
	private IFollowUpVulnerabilityCheckTaskFactory followUpVulnerabilityCheckTaskFactory;
	private IReportGeneratorTaskFactory reportGeneratorTaskFactory;
	private IWorkerProcessManager workerProcessManager;

	private Map<File, SingleTargetDirector> directors = new ConcurrentHashMap<>();

	@Inject
	ProcessDirector(IInitializationTaskFactory initializationTaskFactory,
			ISchemaExplorationTaskFactory schemaExplorationTaskFactory,
			ISchemaEvolutionTaskFactory schemaEvolutionTaskFactory,
			IInitialVulnerabilityCheckTaskFactory vulnerabilityCheckTaskFactory,
			IFollowUpVulnerabilityCheckTaskFactory followUpVulnerabilityCheckTaskFactory,
			IReportGeneratorTaskFactory reportGeneratorTaskFactory,
			IWorkerProcessManager workerProcessManager) {
		this.initializationTaskFactory = initializationTaskFactory;
		this.schemaExplorationTaskFactory = schemaExplorationTaskFactory;
		this.schemaEvolutionTaskFactory = schemaEvolutionTaskFactory;
		this.initialVulnerabilityCheckTaskFactory = vulnerabilityCheckTaskFactory;
		this.followUpVulnerabilityCheckTaskFactory = followUpVulnerabilityCheckTaskFactory;
		this.reportGeneratorTaskFactory = reportGeneratorTaskFactory;
		this.workerProcessManager = workerProcessManager;
	}

	@Override
	public void startProcess(File xsltFile, ProcessingMode mode) {
		logger.traceEntry();
		logger.info("Starting processing of stylesheet {}", xsltFile);
		final SingleTargetDirector director = this.directors.computeIfAbsent(xsltFile,
				f -> new SingleTargetDirector(xsltFile, mode));
		director.initialize();
		logger.traceExit();
	}

	@Override
	public ProcessState getProcessState(File xsltFile) {
		if (!this.directors.containsKey(xsltFile)) {
			throw new IllegalArgumentException(
					String.format("Stylesheet %s not submitted for processing yet.", xsltFile));
		} else {
			return this.directors.get(xsltFile).getProcessState();
		}
	}

	@Override
	public boolean isCompleted() {
		return this.directors.values().stream().allMatch(director -> director.getProcessState() == ProcessState.DONE);
	}

	@Override
	public void awaitCompletion() throws InterruptedException {
		logger.debug("Waiting for all processes to complete");
		while (!isCompleted()) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				logger.warn("process director interrupted while waiting for completion of tasks", e);
				throw e;
			}
		}
	}

	private class SingleTargetDirector {

		private static final Logger logger = LogManager.getLogger();
		private File xsltFile;
		private ProcessingMode processingMode;
		private ProcessState processState;
		private int currentSchemaIteration = 0;

		private static final int SCHEMA_EXPLORATION_DOC_COUNT = 5; // TODO make this configurable
		private static final int NUM_SCHEMA_ITERATIONS = 5; // TODO make this configurable

		private static final int NUM_XSS_INITIAL_DOCUMENTS = 5; // TODO make this configurable
		// @TypesafeConfig("x2vc.xml.initial_documents") Integer initialDocumentCount

		private Set<UUID> schemaExplorationTasks = new HashSet<>();
		private List<ISchemaModifier> schemaModifiers = new ArrayList<>();
		private int failedExplorationTasks = 0;
		private Set<UUID> vulnerabilityCheckTasks = new HashSet<>();

		/**
		 * @param xsltFile
		 * @param mode
		 */
		public SingleTargetDirector(File xsltFile, ProcessingMode mode) {
			this.xsltFile = xsltFile;
			this.processingMode = mode;
			this.processState = ProcessState.NEW;
		}

		/**
		 * @return the current state of processing
		 */
		public ProcessState getProcessState() {
			return this.processState;
		}

		/**
		 * Starts the initialization phase of the processing.
		 */
		public synchronized void initialize() {
			logger.traceEntry();
			logger.debug("initializing processing of stylesheet {}", this.xsltFile);
			if (this.processState == ProcessState.NEW) {
				this.processState = ProcessState.INITIALIZE;
				final IInitializationTask initTask = ProcessDirector.this.initializationTaskFactory
					.create(this.xsltFile, this.processingMode, this::handleInitializationComplete);
				ProcessDirector.this.workerProcessManager.submit(initTask);
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
			this.schemaModifiers.clear();
			this.failedExplorationTasks = 0;
			this.schemaExplorationTasks.clear();
			this.currentSchemaIteration++;
			// generate a number of initial document requests
			logger.debug("submitting {} document requests for schema exploration of stylesheet {}",
					SCHEMA_EXPLORATION_DOC_COUNT,
					this.xsltFile);
			for (int i = 0; i < SCHEMA_EXPLORATION_DOC_COUNT; i++) {
				final ISchemaExplorationTask explorationTask = ProcessDirector.this.schemaExplorationTaskFactory.create(
						this.xsltFile, this::collectSchemaModifications, this::handleExplorationComplete);
				this.schemaExplorationTasks.add(explorationTask.getTaskID());
				ProcessDirector.this.workerProcessManager.submit(explorationTask);
			}
			logger.traceExit();
		}

		/**
		 * Collects a schema modification request prepared by the {@link ISchemaExplorationTask}.
		 *
		 * @param modifier
		 */
		private void collectSchemaModifications(ISchemaModifier modifier) {
			this.schemaModifiers.add(modifier);
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
				if (this.failedExplorationTasks == SCHEMA_EXPLORATION_DOC_COUNT) {
					// something has gone VERY wrong, abort
					logger.error("All {} attempts to explore the schema use of stylesheet {} have failed",
							this.failedExplorationTasks, this.xsltFile);
					this.processState = ProcessState.DONE;
				} else {
					if (this.schemaModifiers.isEmpty()) {
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
			final ISchemaEvolutionTask evolutionTask = ProcessDirector.this.schemaEvolutionTaskFactory
				.create(this.xsltFile, this.schemaModifiers, this::handleEvolutionComplete);
			ProcessDirector.this.workerProcessManager.submit(evolutionTask);
			logger.traceExit();
		}

		/**
		 * Called when an {@link ISchemaEvolutionTask} completes
		 *
		 * @param result <code>true</code> if a new schema version was created, <code>false</code> otherwise
		 */
		private synchronized void handleEvolutionComplete(Boolean result) {
			logger.traceEntry();
			logger.debug("round {} of {} of schema evolution of stylesheet {} completed, schema changes: {}",
					this.currentSchemaIteration, NUM_SCHEMA_ITERATIONS, this.xsltFile, result);
			if (Boolean.TRUE.equals(result) && (this.currentSchemaIteration < NUM_SCHEMA_ITERATIONS)) {
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
					NUM_XSS_INITIAL_DOCUMENTS,
					this.xsltFile);
			for (int i = 0; i < NUM_XSS_INITIAL_DOCUMENTS; i++) {
				final IInitialVulnerabilityCheckTask checkTask = ProcessDirector.this.initialVulnerabilityCheckTaskFactory
					.create(
							this.xsltFile, this::collectFollowupRequest, this::handleVulnerabilityCheckComplete);
				this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
				ProcessDirector.this.workerProcessManager.submit(checkTask);
			}

			logger.traceExit();
		}

		private synchronized void collectFollowupRequest(IDocumentRequest request) {
			logger.traceEntry();
			final IFollowUpVulnerabilityCheckTask checkTask = ProcessDirector.this.followUpVulnerabilityCheckTaskFactory
				.create(request, this::collectFollowupRequest, this::handleVulnerabilityCheckComplete);
			this.vulnerabilityCheckTasks.add(checkTask.getTaskID());
			ProcessDirector.this.workerProcessManager.submit(checkTask);
			logger.traceExit();
		}

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
			final IReportGeneratorTask reportTask = ProcessDirector.this.reportGeneratorTaskFactory
				.create(this.xsltFile, this::handleReportComplete);
			ProcessDirector.this.workerProcessManager.submit(reportTask);
			logger.traceExit();
		}

		private synchronized void handleReportComplete(Boolean success) {
			logger.traceEntry();
			this.processState = ProcessState.DONE;
			logger.traceExit();
		}

	}

}
