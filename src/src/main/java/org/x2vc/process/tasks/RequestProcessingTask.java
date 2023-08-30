package org.x2vc.process.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.ICompletedRequestRegistry;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;

/**
 * This task is used to process a single {@link IDocumentRequest} and follow up
 * on the results depending on the {@link ProcessingMode}.
 */
public class RequestProcessingTask implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	private IDocumentGenerator documentGenerator;
	private IXSLTProcessor processor;
	private IDocumentAnalyzer analyzer;
	private IRequestGenerator requestGenerator;
	private ICompletedRequestRegistry completedRequestRegistry;
	private ITaskFactory taskFactory;
	private IWorkerProcessManager workerProcessManager;
	private IDocumentRequest request;
	private ProcessingMode mode;

	/**
	 * @param request
	 * @param mode
	 */
	RequestProcessingTask(IDocumentGenerator documentGenerator, IXSLTProcessor processor, IDocumentAnalyzer analyzer,
			IRequestGenerator requestGenerator, ICompletedRequestRegistry completedRequestRegistry,
			ITaskFactory taskFactory, IWorkerProcessManager workerProcessManager,

			IDocumentRequest request, ProcessingMode mode) {
		super();
		this.documentGenerator = documentGenerator;
		this.processor = processor;
		this.analyzer = analyzer;
		this.requestGenerator = requestGenerator;
		this.completedRequestRegistry = completedRequestRegistry;
		this.taskFactory = taskFactory;
		this.workerProcessManager = workerProcessManager;
		this.request = request;
		this.mode = mode;
	}

	@Override
	public void run() {
		logger.traceEntry();
		try {
			if (this.completedRequestRegistry.contains(this.request)) {
				logger.debug("eliminating duplicate request");
			} else {
				logger.debug("registering request as completed");
				this.completedRequestRegistry.register(this.request);

				logger.debug("generating XML document");
				final IXMLDocumentContainer xmlDocument = this.documentGenerator.generateDocument(this.request);

				logger.debug("processing XML to HTML");
				final IHTMLDocumentContainer htmlDocument = this.processor.processDocument(xmlDocument);

				if (!htmlDocument.isFailed()) {
					if (this.mode == ProcessingMode.FULL || this.mode == ProcessingMode.XSS_ONLY) {
						this.analyzer.analyzeDocument(htmlDocument, modifier -> {
							final IDocumentRequest modifiedRequest = this.requestGenerator.modifyRequest(this.request,
									modifier);
							logger.debug("adding new task for modification request");
							final RequestProcessingTask task = this.taskFactory
								.createRequestProcessingTask(modifiedRequest, this.mode);
							this.workerProcessManager.submit(task);
						}, report -> {
							// TODO XSS Vulnerability: handle vulnerability reports
						});
					}

					if (this.mode == ProcessingMode.FULL || this.mode == ProcessingMode.SCHEMA_ONLY) {
						// TODO XML Schema Evolution: implement
					}
				}
			}
		} catch (final Exception ex) {
			logger.error("unhandled exception in request processing task", ex);
		}
		logger.traceExit();
	}

}
