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

import net.sf.saxon.expr.Expression;
import net.sf.saxon.pattern.NodeTest;

/**
 * Factory to obtain new {@link IEvaluationTreeItem} instances. The factory keeps track of all items it has created so
 * far and is able to perform a complete initialization of all items.
 *
 * @sse {@link IEvaluationTreeItem#initialize(IEvaluationTreeItemFactory)}
 */
public interface IEvaluationTreeItemFactory {

	/**
	 * Creates a new item for an {@link Expression}.
	 *
	 * @param expression
	 * @return the item
	 */
	IEvaluationTreeItem createItemForExpression(Expression expression);

	/**
	 * Creates a new item for a {@link NodeTest}.
	 *
	 * @param nodeTest
	 * @return the item
	 */
	INodeTestTreeItem createItemForNodeTest(NodeTest nodeTest);

	/**
	 * Calls {@link IEvaluationTreeItem#initialize(IEvaluationTreeItemFactory)} for all items this factory has created
	 * so far. If more items are created during the initialization, these are initialized as well.
	 */
	void initializeAllCreatedItems();

}
