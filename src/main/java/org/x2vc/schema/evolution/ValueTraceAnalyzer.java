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
package org.x2vc.schema.evolution;


import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.evolution.items.IEvaluationTreeItem;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactory;
import org.x2vc.schema.evolution.items.IEvaluationTreeItemFactoryFactory;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;

import net.sf.saxon.expr.Expression;

/**
 * Standard implementation of {@link IValueTraceAnalyzer}.
 */
public class ValueTraceAnalyzer implements IValueTraceAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;
	private IValueTracePreprocessor valueTracePreprocessor;
	private IEvaluationTreeItemFactoryFactory evaluationTreeItemFactoryFactory;
	private IModifierCreationCoordinatorFactory modifierCreationCoordinatorFactory;

	@Inject
	protected ValueTraceAnalyzer(ISchemaManager schemaManager, IValueTracePreprocessor valueTracePreprocessor,
			IEvaluationTreeItemFactoryFactory evaluationTreeItemFactoryFactory,
			IModifierCreationCoordinatorFactory modifierCreationCoordinatorFactory) {
		super();
		this.schemaManager = schemaManager;
		this.valueTracePreprocessor = valueTracePreprocessor;
		this.evaluationTreeItemFactoryFactory = evaluationTreeItemFactoryFactory;
		this.modifierCreationCoordinatorFactory = modifierCreationCoordinatorFactory;
	}

	@Override
	public void analyzeDocument(UUID taskID, IHTMLDocumentContainer htmlContainer,
			Consumer<ISchemaModifier> modifierCollector) {
		logger.traceEntry();

		// Resolve the schema reference
		final IXMLDocumentContainer xmlContainer = htmlContainer.getSource();
		final IXMLSchema schema = this.schemaManager.getSchema(
				xmlContainer.getStylesheeURI(),
				xmlContainer.getSchemaVersion());

		// Let the preprocessor re-order the events to a more usable form
		final ImmutableMultimap<ISchemaElementProxy, Expression> schemaTraceEvents = this.valueTracePreprocessor
			.prepareEvents(htmlContainer);

		if (schemaTraceEvents.isEmpty()) {
			logger.warn("No usable tracing information was found for this document");
		} else {
			// prepare an object to consolidate and forward all the modification requests
			final IModifierCreationCoordinator modifierCreationCoordinator = this.modifierCreationCoordinatorFactory
				.createCoordinator(schema, modifierCollector);

			// prepare a factory to create new evaluation tree items
			final IEvaluationTreeItemFactory factory = this.evaluationTreeItemFactoryFactory.createFactory(schema,
					modifierCreationCoordinator);

			// process the trace events grouped by schema request
			schemaTraceEvents.keySet().forEach(
					schemaObject -> processExpressions(
							schemaObject,
							schemaTraceEvents.get(schemaObject),
							factory));
			modifierCreationCoordinator.flush();
		}
		logger.traceExit();
	}

	/**
	 * Process the expressions associated to a schema element.
	 *
	 * @param contextItem
	 * @param expressions
	 * @param factory
	 */
	private void processExpressions(ISchemaElementProxy contextItem,
			ImmutableCollection<Expression> expressions, IEvaluationTreeItemFactory factory) {
		logger.traceEntry("for context item {}", contextItem);
		for (final Expression expression : expressions) {
			logger.debug("creating evaluation tree for expression {}", expression);
			final IEvaluationTreeItem topLevelItem = factory.createItemForExpression(expression);
			factory.initializeAllCreatedItems();
			logger.debug("processing evaluation tree for expression {}", expression);
			topLevelItem.evaluate(contextItem);
		}
		logger.traceExit();
	}

}
