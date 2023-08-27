package org.x2vc.process.tasks;

import java.io.File;

import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.processor.IXSLTProcessor;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.xml.document.IDocumentGenerator;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IRequestGenerator;

import com.github.racc.tscg.TypesafeConfig;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link ITaskFactory}.
 */
public class TaskFactory implements ITaskFactory {

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IRequestGenerator requestGenerator;
	private IWorkerProcessManager workerProcessManager;
	private IDocumentGenerator documentGenerator;
	private IXSLTProcessor processor;
	private IDocumentAnalyzer analyzer;

	private Integer initialDocumentCount;

	/**
	 * @param stylesheetManager
	 * @param schemaManager
	 * @param requestGenerator
	 * @param taskFactory
	 * @param workerProcessManager
	 */
	@Inject
	TaskFactory(IStylesheetManager stylesheetManager, ISchemaManager schemaManager, IRequestGenerator requestGenerator,
			ITaskFactory taskFactory, IWorkerProcessManager workerProcessManager, IDocumentGenerator documentGenerator,
			IXSLTProcessor processor, IDocumentAnalyzer analyzer,
			@TypesafeConfig("x2vc.xml.initial_documents") Integer initialDocumentCount) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.requestGenerator = requestGenerator;
		this.workerProcessManager = workerProcessManager;
		this.documentGenerator = documentGenerator;
		this.processor = processor;
		this.analyzer = analyzer;
		this.initialDocumentCount = initialDocumentCount;
	}

	@Override
	public InitializationTask createInitializationTask(File xsltFile, ProcessingMode mode) {
		return new InitializationTask(this.stylesheetManager, this.schemaManager, this.requestGenerator, this,
				this.workerProcessManager, xsltFile, mode, this.initialDocumentCount);
	}

	@Override
	public RequestProcessingTask createRequestProcessingTask(IDocumentRequest request, ProcessingMode mode) {
		return new RequestProcessingTask(this.documentGenerator, this.processor, this.analyzer, this.requestGenerator,
				this, this.workerProcessManager, request, mode);
	}

}
