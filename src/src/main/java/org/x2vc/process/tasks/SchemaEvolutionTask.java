package org.x2vc.process.tasks;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.IDebugObjectWriter;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to consolidate the schema modifiers generated during the exploration phase and produce a new schema
 * version to be tested.
 */
public class SchemaEvolutionTask implements ISchemaEvolutionTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IDebugObjectWriter debugObjectWriter;
	private File xsltFile;
	private ImmutableSet<ISchemaModifier> modifiers;
	private Consumer<Boolean> callback;

	private UUID taskID = UUID.randomUUID();

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	SchemaEvolutionTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IDebugObjectWriter debugObjectWriter,
			@Assisted File xsltFile,
			@Assisted ImmutableSet<ISchemaModifier> modifiers,
			@Assisted Consumer<Boolean> callback) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.debugObjectWriter = debugObjectWriter;
		this.xsltFile = xsltFile;
		this.modifiers = modifiers;
		this.callback = callback;
	}

	@Override
	public void run() {
		logger.traceEntry();
		try {
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.xsltFile.toURI());
			final IXMLSchema oldSchema = this.schemaManager.getSchema(stylesheetInfo.getURI());
			final IXMLSchema newSchema = this.schemaManager.modifySchema(oldSchema, this.modifiers);
			this.debugObjectWriter.writeSchema(this.taskID, newSchema);
			this.callback.accept(true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in schema evolution task", ex);
			this.callback.accept(false);
		}
		logger.traceExit();
	}

	@Override
	public UUID getTaskID() {
		return this.taskID;
	}

}
