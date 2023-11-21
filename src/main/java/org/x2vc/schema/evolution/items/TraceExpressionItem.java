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


import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.instruct.TraceExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent a {@link TraceExpression}.
 */
public class TraceExpressionItem extends AbstractEvaluationTreeItem<TraceExpression> {

	private IEvaluationTreeItem bodyItem;

	TraceExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, TraceExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, TraceExpression target) {
		this.bodyItem = itemFactory.createItemForExpression(target.getBody());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			TraceExpression target) {
		return this.bodyItem.evaluate(contextItem);
	}

}
