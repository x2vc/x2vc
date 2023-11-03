package org.x2vc.schema.evolution.items;

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

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.instruct.ValueOf;

/**
 * {@link IEvaluationTreeItemFactory} to represent a {@link ValueOf} expression.
 */
public class ValueOfItem extends AbstractEvaluationTreeItem<ValueOf> {

	private IEvaluationTreeItem selectItem;

	ValueOfItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, ValueOf target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, ValueOf target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			ValueOf target) {
		this.selectItem.evaluate(contextItem);
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
