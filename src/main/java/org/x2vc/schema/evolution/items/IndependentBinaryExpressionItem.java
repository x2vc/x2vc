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

import net.sf.saxon.expr.BinaryExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent subclasses of {@link BinaryExpression} that do not perform any access
 * themselves, do not change the context element and evaluate the two sub-expressions independently of each other. This
 * is the case e.g. for comparison expressions. The processing is simply deferred to the base expressions.
 *
 * @param <T> the type of the object being evaluated
 */
public class IndependentBinaryExpressionItem<T extends BinaryExpression> extends AbstractEvaluationTreeItem<T> {

	private IEvaluationTreeItem lhsExpression;
	private IEvaluationTreeItem rhsExpression;

	IndependentBinaryExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, T target) {
		this.lhsExpression = itemFactory.createItemForExpression(target.getLhsExpression());
		this.rhsExpression = itemFactory.createItemForExpression(target.getRhsExpression());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			T target) {
		this.lhsExpression.evaluate(contextItem);
		this.rhsExpression.evaluate(contextItem);
		return ImmutableSet.of(contextItem);
	}

}
