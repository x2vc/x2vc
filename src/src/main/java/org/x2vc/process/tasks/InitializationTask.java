package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Standard implementation of {@link IInitializationTask}.
 */
public class InitializationTask implements IInitializationTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private File xsltFile;
	private ProcessingMode mode;
	private Consumer<Boolean> callback;

	@Inject
	InitializationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			@Assisted File xsltFile, @Assisted ProcessingMode mode, @Assisted Consumer<Boolean> callback) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.xsltFile = xsltFile;
		this.mode = mode;
		this.callback = callback;
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

			// FIXME only generate new schema if mode is not XSS ONLY

			this.callback.accept(true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in initialization task", ex);
			this.callback.accept(false);
		}
		logger.traceExit();
	}

}
