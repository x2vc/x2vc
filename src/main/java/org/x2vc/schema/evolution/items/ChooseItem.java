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
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.instruct.Choose;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link Choose}.
 */
public class ChooseItem extends AbstractEvaluationTreeItem<Choose> {

	private int targetSize;
	private IEvaluationTreeItem[] conditionItems;
	private IEvaluationTreeItem[] actionItems;

	ChooseItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, Choose target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, Choose target) {
		this.targetSize = target.size();
		this.conditionItems = new IEvaluationTreeItem[this.targetSize];
		this.actionItems = new IEvaluationTreeItem[this.targetSize];
		for (int i = 0; i < this.targetSize; i++) {
			this.conditionItems[i] = itemFactory.createItemForExpression(target.getCondition(i));
			this.actionItems[i] = itemFactory.createItemForExpression(target.getAction(i));
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			Choose target) {
		// evaluate the subordinate expressions
		for (int i = 0; i < this.targetSize; i++) {
			this.conditionItems[i].evaluate(contextItem);
			this.actionItems[i].evaluate(contextItem);
		}
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
