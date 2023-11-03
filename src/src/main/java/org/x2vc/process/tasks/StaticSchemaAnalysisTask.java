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
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.evolution.IStaticStylesheetAnalyzer;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to prepare the schema updates that can be determined by examining the stylesheet structure
 * statically.
 */
public class StaticSchemaAnalysisTask extends AbstractTask implements IStaticSchemaAnalysisTask {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;
	private ISchemaManager schemaManager;
	private IStaticStylesheetAnalyzer staticStylesheetAnalyzer;
	private Consumer<ISchemaModifier> modifierCollector;
	private BiConsumer<UUID, Boolean> callback;

	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	StaticSchemaAnalysisTask(IStylesheetManager stylesheetManager, ISchemaManager schemaManager,
			IStaticStylesheetAnalyzer staticStylesheetAnalyzer,
			@Assisted File xsltFile,
			@Assisted Consumer<ISchemaModifier> modifierCollector,
			@Assisted BiConsumer<UUID, Boolean> callback) {
		super(xsltFile);
		this.stylesheetManager = stylesheetManager;
		this.schemaManager = schemaManager;
		this.staticStylesheetAnalyzer = staticStylesheetAnalyzer;
		this.modifierCollector = modifierCollector;
		this.callback = callback;
	}

	@Override
	public void execute() {
		logger.traceEntry("for task ID {}", this.getTaskID());
		try {
			final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(this.getXSLTFile().toURI());
			final IXMLSchema schema = this.schemaManager.getSchema(stylesheetInfo.getURI());
			logger.debug("checking stylesheet and schema for necessary modifications");
			this.staticStylesheetAnalyzer.analyze(getTaskID(), stylesheetInfo.getStructure(), schema,
					this.modifierCollector);
			this.callback.accept(this.getTaskID(), true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in schema analysis task", ex);
			this.callback.accept(this.getTaskID(), false);
		}
		logger.traceExit();
	}
}
