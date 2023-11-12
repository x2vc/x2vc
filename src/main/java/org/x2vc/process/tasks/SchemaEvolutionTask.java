package org.x2vc.process.tasks;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.File;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.IDebugObjectWriter;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to consolidate the schema modifiers generated during the exploration phase and produce a new schema
 * version to be tested.
 */
public class SchemaEvolutionTask extends AbstractTask implements ISchemaEvolutionTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IDebugObjectWriter debugObjectWriter;
	private ISchemaModifierCollector modifierCollector;
	private Consumer<Boolean> callback;

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	SchemaEvolutionTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IDebugObjectWriter debugObjectWriter,
			@Assisted File xsltFile,
			@Assisted ISchemaModifierCollector modifierCollector,
			@Assisted Consumer<Boolean> callback) {
		super(xsltFile);
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.debugObjectWriter = debugObjectWriter;
		this.modifierCollector = modifierCollector;
		this.callback = callback;
	}

	@Override
	public void execute() {
		logger.traceEntry();
		try {
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.getXSLTFile().toURI());
			final IXMLSchema oldSchema = this.schemaManager.getSchema(stylesheetInfo.getURI());
			this.debugObjectWriter.writeSchemaModifiers(this.getTaskID(), this.modifierCollector);
			final IXMLSchema newSchema = this.schemaManager.modifySchema(oldSchema,
					this.modifierCollector.getConsolidatedModifiers());
			this.debugObjectWriter.writeSchema(this.getTaskID(), newSchema);
			this.callback.accept(true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in schema evolution task", ex);
			this.callback.accept(false);
		}
		logger.traceExit();
	}

}
