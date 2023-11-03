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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.functions.IntegratedFunctionCall;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link IntegratedFunctionCall}.
 */
public class IntegratedFunctionCallItem extends AbstractEvaluationTreeItem<IntegratedFunctionCall> {

	private static final Logger logger = LogManager.getLogger();

	private IEvaluationTreeItem[] argumentItems = null;

	IntegratedFunctionCallItem(IXMLSchema schema, IModifierCreationCoordinator coordinator,
			IntegratedFunctionCall target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, IntegratedFunctionCall target) {
		final Expression[] arguments = target.getArguments();
		if (arguments.length > 0) {
			this.argumentItems = new IEvaluationTreeItem[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				this.argumentItems[i] = itemFactory.createItemForExpression(arguments[i]);
			}
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			IntegratedFunctionCall target) {

		// evaluate every argument item
		if (this.argumentItems != null) {
			for (int i = 0; i < this.argumentItems.length; i++) {
				this.argumentItems[i].evaluate(contextItem);
			}
		}

		// return the context item unchanged
		return logger.traceExit(ImmutableSet.of(contextItem));
	}

}
