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


import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.expr.SlashExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent a {@link SlashExpression}.
 *
 */
public class SlashExpressionItem extends AbstractEvaluationTreeItem<SlashExpression> {

	private static final Logger logger = LogManager.getLogger();

	private IEvaluationTreeItem firstStepItem;
	private IEvaluationTreeItem remainingStepsItem;

	SlashExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, SlashExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, SlashExpression target) {
		this.firstStepItem = itemFactory.createItemForExpression(target.getFirstStep());
		this.remainingStepsItem = itemFactory.createItemForExpression(target.getRemainingSteps());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			SlashExpression target) {
		final ImmutableCollection<ISchemaElementProxy> firstStepResults = this.firstStepItem.evaluate(contextItem);
		logger.debug("evaluation of first step yielded {} context items for remaining steps", firstStepResults.size());
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		for (final ISchemaElementProxy newContext : firstStepResults) {
			logger.trace("evaluating remaining steps for result item {}", newContext);
			result.addAll(this.remainingStepsItem.evaluate(newContext));
		}
		return ImmutableSet.copyOf(result);
	}

}
