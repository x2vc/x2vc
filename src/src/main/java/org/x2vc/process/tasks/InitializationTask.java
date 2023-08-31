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

import com.github.racc.tscg.TypesafeConfig;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Standard implementation of {@link IInitializationTask}.
 */
public class InitializationTask implements IInitializationTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IRequestGenerator requestGenerator;
	private IRequestProcessingTaskFactory requestProcessingTaskFactory;
	private IWorkerProcessManager workerProcessManager;
	private File xsltFile;
	private ProcessingMode mode;
	private Integer initialDocumentCount;

	/**
	 * @param stylesheetManager
	 * @param xsltFile
	 */
	@Inject
	InitializationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IRequestGenerator requestGenerator, IRequestProcessingTaskFactory taskFactory,
			IWorkerProcessManager workerProcessManager, @Assisted File xsltFile, @Assisted ProcessingMode mode,
			@TypesafeConfig("x2vc.xml.initial_documents") Integer initialDocumentCount) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.requestGenerator = requestGenerator;
		this.requestProcessingTaskFactory = taskFactory;
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
				final IRequestProcessingTask task = this.requestProcessingTaskFactory.create(request, this.mode);
				this.workerProcessManager.submit(task);
			}
		} catch (final Exception ex) {
			logger.error("unhandled exception in initialization task", ex);
		}
		logger.traceExit();
	}

}
