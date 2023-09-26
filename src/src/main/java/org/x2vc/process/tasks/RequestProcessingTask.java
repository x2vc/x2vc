package org.x2vc.process.tasks;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.IVulnerabilityCandidateCollector;
import org.x2vc.schema.evolution.ISchemaEvolver;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.utilities.IDebugObjectWriter;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.ICompletedRequestRegistry;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to process a single {@link IDocumentRequest} and follow up on the results depending on the
 * {@link ProcessingMode}.
 */
public class RequestProcessingTask implements IRequestProcessingTask {

	private static final Logger logger = LogManager.getLogger();

	private IDocumentGenerator documentGenerator;
	private IXSLTProcessor processor;
	private IDocumentAnalyzer analyzer;
	private ISchemaEvolver evolver;
	private IRequestGenerator requestGenerator;
	private ICompletedRequestRegistry completedRequestRegistry;
	private IRequestProcessingTaskFactory requestProcessingTaskFactory;
	private IWorkerProcessManager workerProcessManager;
	private IDebugObjectWriter debugObjectWriter;
	private IVulnerabilityCandidateCollector vulnerabilityCandidateCollector;
	private IDocumentRequest request;
	private ProcessingMode mode;

	private UUID taskID;

	int nextCandidateNumber = 1;

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	RequestProcessingTask(IDocumentGenerator documentGenerator, IXSLTProcessor processor, IDocumentAnalyzer analyzer,
			ISchemaEvolver evolver, IRequestGenerator requestGenerator,
			ICompletedRequestRegistry completedRequestRegistry, IRequestProcessingTaskFactory taskFactory,
			IWorkerProcessManager workerProcessManager, IDebugObjectWriter debugObjectWriter,
			IVulnerabilityCandidateCollector vulnerabilityCandidateCollector,
			@Assisted IDocumentRequest request, @Assisted ProcessingMode mode) {
		super();
		this.documentGenerator = documentGenerator;
		this.processor = processor;
		this.analyzer = analyzer;
		this.evolver = evolver;
		this.requestGenerator = requestGenerator;
		this.completedRequestRegistry = completedRequestRegistry;
		this.requestProcessingTaskFactory = taskFactory;
		this.workerProcessManager = workerProcessManager;
		this.debugObjectWriter = debugObjectWriter;
		this.vulnerabilityCandidateCollector = vulnerabilityCandidateCollector;
		this.request = request;
		this.mode = mode;
		this.taskID = UUID.randomUUID();
	}

	@Override
	public void run() {
		logger.traceEntry("for task ID {}", this.taskID);
		try {
			if (this.completedRequestRegistry.contains(this.request)) {
				logger.debug("eliminating duplicate request");
			} else {
				this.debugObjectWriter.writeRequest(this.taskID, this.request);

				logger.debug("registering request as completed");
				this.completedRequestRegistry.register(this.request);

				logger.debug("generating XML document");
				final IXMLDocumentContainer xmlDocument = this.documentGenerator.generateDocument(this.request);
				this.debugObjectWriter.writeXMLDocument(this.taskID, xmlDocument);

				logger.debug("processing XML to HTML");
				final IHTMLDocumentContainer htmlDocument = this.processor.processDocument(xmlDocument);
				this.debugObjectWriter.writeHTMLDocument(this.taskID, htmlDocument);

				if (!htmlDocument.isFailed()) {
					if (this.mode == ProcessingMode.FULL || this.mode == ProcessingMode.XSS_ONLY) {
						// perform XSS analysis
						this.analyzer.analyzeDocument(this.taskID, htmlDocument,
								this::handleDocumentModificationRequest, this::handleVulnerabilityCandidate);
					}
					if (this.mode == ProcessingMode.FULL || this.mode == ProcessingMode.SCHEMA_ONLY) {
						// extend schema to cover more input data elements if possible
						this.evolver.analyzeDocument(this.taskID, htmlDocument, this::handleSchemaModificationRequest);
					}
				}
			}
		} catch (final Exception ex) {
			logger.error("unhandled exception in request processing task", ex);
		}
		logger.traceExit();
	}

	private void handleDocumentModificationRequest(IDocumentModifier modifier) {
		logger.traceEntry();
		final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(this.request, modifier);
		logger.debug("adding new task for modification request");
		final IRequestProcessingTask task = this.requestProcessingTaskFactory.create(modifiedRequest, this.mode);
		this.workerProcessManager.submit(task);
		logger.traceExit();
	}

	private void handleVulnerabilityCandidate(IVulnerabilityCandidate candidate) {
		logger.traceEntry();
		this.debugObjectWriter.writeVulnerabilityCandidate(this.taskID, this.nextCandidateNumber, candidate);
		this.nextCandidateNumber++;
		logger.debug("storing vulnerability candidate for later processing");
		this.vulnerabilityCandidateCollector.add(this.request.getStylesheeURI(), candidate);
		logger.traceExit();
	}

	private void handleSchemaModificationRequest(ISchemaModifier modifier) {
		logger.traceEntry();
		logger.info("received schema modifier {}", modifier);
		// FIXME implement this
		logger.traceExit();
	}

}
