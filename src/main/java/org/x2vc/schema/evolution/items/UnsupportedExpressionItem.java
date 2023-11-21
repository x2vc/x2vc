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
package org.x2vc.schema.evolution.items;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Expression;

/**
 * {@link IEvaluationTreeItem} to represent an {@link Expression} for which no specialized implementation is available
 * yet.
 */
public class UnsupportedExpressionItem extends AbstractEvaluationTreeItem<Expression> {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * @param schema
	 * @param coordinator
	 * @param target
	 */
	UnsupportedExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, Expression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, Expression target) {
		logger.warn("Unsupported expression type {}: {}", target.getClass().getSimpleName(), target);
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem, Expression target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
