package org.x2vc.process.tasks;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;

/**
 * This task is used to initialize the checking of a single XSLT file by
 * preparing the stylesheet, loading or generating the initial schema version
 * and generating a number of first-pass document requests.
 */
public class InitializationTask implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IRequestGenerator requestGenerator;
	private ITaskFactory taskFactory;
	private IWorkerProcessManager workerProcessManager;
	private File xsltFile;
	private ProcessingMode mode;
	private Integer initialDocumentCount;

	/**
	 * @param stylesheetManager
	 * @param xsltFile
	 */
	InitializationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IRequestGenerator requestGenerator, ITaskFactory taskFactory, IWorkerProcessManager workerProcessManager,
			File xsltFile, ProcessingMode mode, Integer initialDocumentCount) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.requestGenerator = requestGenerator;
		this.taskFactory = taskFactory;
		this.workerProcessManager = workerProcessManager;
		this.xsltFile = xsltFile;
		this.mode = mode;
		this.initialDocumentCount = initialDocumentCount;
	}

	@Override
	public void run() {
		logger.traceEntry();
		try {

			// load the stylesheet
			logger.debug("preparing stylesheet from file {}", this.xsltFile);
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.xsltFile.toURI());

			// get the schema (generate new or load existing)
			logger.debug("preparing schema for stylesheet {}", this.xsltFile);
			final IXMLSchema schema = this.schemaManager.getSchema(stylesheetInfo.getURI());

			// generate a number of initial document requests
			logger.debug("submitting {} initial document requests for stylesheet {}", this.initialDocumentCount,
					this.xsltFile);
			for (int i = 0; i < this.initialDocumentCount; i++) {
				final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
				final RequestProcessingTask task = this.taskFactory.createRequestProcessingTask(request, this.mode);
				this.workerProcessManager.submit(task);
			}
		} catch (final Exception ex) {
			logger.error("unhandled exception in initialization task", ex);
		}
		logger.traceExit();
	}

}
