package org.x2vc.process.tasks;

import java.io.File;
import java.util.Optional;
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
import org.x2vc.xml.request.MixedContentGenerationMode;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * This task is used to process a single {@link IDocumentRequest} and collect the relevant results for schema evolution.
 */
public class SchemaExplorationTask extends AbstractTask implements ISchemaExplorationTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IDocumentGenerator documentGenerator;
	private IXSLTProcessor processor;
	private IValueTraceAnalyzer valueTraceAnalyzer;
	private IRequestGenerator requestGenerator;
	private IDebugObjectWriter debugObjectWriter;
	private Consumer<ISchemaModifier> modifierCollector;
	private BiConsumer<UUID, Boolean> callback;

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	SchemaExplorationTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IDocumentGenerator documentGenerator, IXSLTProcessor processor, IValueTraceAnalyzer valueTraceAnalyzer,
			IRequestGenerator requestGenerator, IDebugObjectWriter debugObjectWriter,
			@Assisted File xsltFile,
			@Assisted Consumer<ISchemaModifier> modifierCollector,
			@Assisted BiConsumer<UUID, Boolean> callback) {
		super(xsltFile);
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.documentGenerator = documentGenerator;
		this.processor = processor;
		this.valueTraceAnalyzer = valueTraceAnalyzer;
		this.requestGenerator = requestGenerator;
		this.debugObjectWriter = debugObjectWriter;
		this.modifierCollector = modifierCollector;
		this.callback = callback;
	}

	@Override
	public void execute() {
		logger.traceEntry("for task ID {}", this.getTaskID());
		try {
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.getXSLTFile().toURI());
			final IXMLSchema schema = this.schemaManager.getSchema(stylesheetInfo.getURI());

			logger.debug("generating new request to explore schema usage of stylesheet {}", stylesheetInfo.getURI());
			final IDocumentRequest request = this.requestGenerator.generateNewRequest(schema,
					MixedContentGenerationMode.RESTRICTED);
			this.debugObjectWriter.writeRequest(this.getTaskID(), request);

			logger.debug("generating XML document");
			final IXMLDocumentContainer xmlDocument = this.documentGenerator.generateDocument(request);
			this.debugObjectWriter.writeXMLDocument(this.getTaskID(), xmlDocument);

			logger.debug("processing XML to HTML");
			final IHTMLDocumentContainer htmlDocument = this.processor.processDocument(xmlDocument);
			this.debugObjectWriter.writeHTMLDocument(this.getTaskID(), htmlDocument);

			if (!htmlDocument.isFailed()) {
				logger.debug("analyzing {} trace events produced by XSLT processor",
						htmlDocument.getTraceEvents().map(ev -> ev.size()).orElse(0));
				this.valueTraceAnalyzer.analyzeDocument(this.getTaskID(), htmlDocument, this.modifierCollector);
				this.callback.accept(this.getTaskID(), true);
			} else {
				final Optional<SaxonApiException> compilationError = htmlDocument.getCompilationError();
				final Optional<SaxonApiException> processingError = htmlDocument.getProcessingError();
				if (compilationError.isPresent()) {
					logger.debug("processing of XML to HMTL failed with compilation error: {}",
							compilationError.get().getMessage());
				} else if (processingError.isPresent()) {
					logger.debug("processing of XML to HMTL failed with processing error: {}",
							processingError.get().getMessage());
				} else {
					logger.debug("processing of XML to HMTL failed other unspecified error");
				}
				this.callback.accept(this.getTaskID(), false);
			}
		} catch (final Exception ex) {
			logger.error("unhandled exception in schema exploration task", ex);
			this.callback.accept(this.getTaskID(), false);
		}
		logger.traceExit();
	}

}
