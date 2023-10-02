package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.IDebugObjectWriter;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Standard implementation of {@link IInitializationTask}.
 */
public class InitializationTask extends AbstractTask implements IInitializationTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IDebugObjectWriter debugObjectWriter;
	private ProcessingMode mode;
	private Consumer<Boolean> callback;

	@Inject
	InitializationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IDebugObjectWriter debugObjectWriter,
			@Assisted File xsltFile, @Assisted ProcessingMode mode, @Assisted Consumer<Boolean> callback) {
		super(xsltFile);
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.debugObjectWriter = debugObjectWriter;
		this.mode = mode;
		this.callback = callback;
	}

	@Override
	public void execute() {
		logger.traceEntry();
		try {

			// load the stylesheet
			logger.debug("preparing stylesheet from file {}", this.getXSLTFile());
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.getXSLTFile().toURI());

			// get the schema (generate new or load existing)
			logger.debug("preparing schema for stylesheet {}", this.getXSLTFile());
			if ((this.mode == ProcessingMode.XSS_ONLY) && (!this.schemaManager.schemaExists(stylesheetInfo.getURI()))) {
				logger.error("Schema for stylesheet {} is missing and will not be generated in XSS-only mode.",
						this.getXSLTFile());
				this.callback.accept(false);
			} else {
				final IXMLSchema schema = this.schemaManager.getSchema(stylesheetInfo.getURI());
				this.debugObjectWriter.writeSchema(this.getTaskID(), schema);
				this.callback.accept(true);
			}

		} catch (final Exception ex) {
			logger.error("unhandled exception in initialization task", ex);
			this.callback.accept(false);
		}
		logger.traceExit();
	}

}
