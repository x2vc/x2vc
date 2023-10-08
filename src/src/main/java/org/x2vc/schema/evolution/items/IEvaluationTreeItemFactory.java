package org.x2vc.schema.evolution.items;

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
	IEvaluationTreeItem createItem(Expression expression);

	/**
	 * Creates a new item for a {@link NodeTest}.
	 *
	 * @param nodeTest
	 * @return the item
	 */
	IEvaluationTreeItem createItem(NodeTest nodeTest);

	/**
	 * Calls {@link IEvaluationTreeItem#initialize(IEvaluationTreeItemFactory)} for all items this factory has created
	 * so far. If more items are created during the initialization, these are initialized as well.
	 */
	void initializeAllCreatedItems();

}
