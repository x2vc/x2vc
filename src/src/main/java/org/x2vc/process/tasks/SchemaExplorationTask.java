package org.x2vc.process.tasks;

import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.evolution.IValueTraceAnalyzer;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.IDebugObjectWriter;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to process a single {@link IDocumentRequest} and collect the relevant results for schema evolution.
 */
public class SchemaExplorationTask implements ISchemaExplorationTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IDocumentGenerator documentGenerator;
	private IXSLTProcessor processor;
	private IValueTraceAnalyzer valueTraceAnalyuer;
	private IRequestGenerator requestGenerator;
	private IDebugObjectWriter debugObjectWriter;
	private File xsltFile;
	private Consumer<ISchemaModifier> modifierCollector;
	private BiConsumer<UUID, Boolean> callback;

	private UUID taskID = UUID.randomUUID();

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	SchemaExplorationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IDocumentGenerator documentGenerator, IXSLTProcessor processor, IValueTraceAnalyzer valueTraceAnalyuer,
			IRequestGenerator requestGenerator, IDebugObjectWriter debugObjectWriter,
			@Assisted File xsltFile,
			@Assisted Consumer<ISchemaModifier> modifierCollector,
			@Assisted BiConsumer<UUID, Boolean> callback) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.documentGenerator = documentGenerator;
		this.processor = processor;
		this.valueTraceAnalyuer = valueTraceAnalyuer;
		this.requestGenerator = requestGenerator;
		this.debugObjectWriter = debugObjectWriter;
		this.xsltFile = xsltFile;
		this.modifierCollector = modifierCollector;
		this.callback = callback;
	}

	@Override
	public void run() {
		logger.traceEntry("for task ID {}", this.taskID);
		try {
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.xsltFile.toURI());
			final IXMLSchema schema = this.schemaManager.getSchema(stylesheetInfo.getURI());

			logger.debug("generating new request to explore schema usage of stylesheet {}", stylesheetInfo.getURI());
			final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema);
			this.debugObjectWriter.writeRequest(this.taskID, request);

			logger.debug("generating XML document");
			final IXMLDocumentContainer xmlDocument = this.documentGenerator.generateDocument(request);
			this.debugObjectWriter.writeXMLDocument(this.taskID, xmlDocument);

			logger.debug("processing XML to HTML");
			final IHTMLDocumentContainer htmlDocument = this.processor.processDocument(xmlDocument);
			this.debugObjectWriter.writeHTMLDocument(this.taskID, htmlDocument);

			if (!htmlDocument.isFailed()) {
				this.valueTraceAnalyuer.analyzeDocument(this.taskID, htmlDocument, this.modifierCollector);
			}
			this.callback.accept(this.taskID, true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in schema exploration task", ex);
			this.callback.accept(this.taskID, false);
		}
		logger.traceExit();
	}

	@Override
	public UUID getTaskID() {
		return this.taskID;
	}

}
