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

import net.sf.saxon.expr.instruct.NumberInstruction;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link NumberInstruction}.
 */
public class NumberInstructionItem extends AbstractEvaluationTreeItem<NumberInstruction> {

	private IEvaluationTreeItem selectItem;

	NumberInstructionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, NumberInstruction target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, NumberInstruction target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
		// TODO #12 check whether patterns need to be taken into account as well
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			NumberInstruction target) {
		this.selectItem.evaluate(contextItem);
		return ImmutableSet.of(contextItem);
	}

}
