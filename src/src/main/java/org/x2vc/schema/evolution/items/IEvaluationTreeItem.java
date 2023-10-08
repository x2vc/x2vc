package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.ISchemaElementProxy;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.Expression;

/**
 * Common interface of all evaluation tree items. An item is basically a node in the "syntax tree" provided by Saxon for
 * an {@link Expression} or similar structures.
 *
 * Implementation note: Creating an item should only create the item itself and store the expression reference. Further
 * initialization (like creating subordinate items for partial expressions) should only be performed in
 * {@link #initialize(IEvaluationTreeItemFactory)}.
 *
 * <b>DO NOT</b> implement this interface directly - create a subclass of {@link AbstractEvaluationTreeItem} instead,
 */
public interface IEvaluationTreeItem {

	/**
	 * Initializes the item fully. Use this method to create all subordinate items using the factory supplied. <b>DO
	 * NOT</b> initialize the created items - this will be performed by an outside actor. (Main reason: avoiding a big
	 * tangled mess of untestable code).
	 *
	 * @param itemFactory
	 */
	void initialize(IEvaluationTreeItemFactory itemFactory);

	/**
	 * Performs the actual evaluation based on the context item provided. If applicable, this method is supposed to call
	 * {@link #evaluate(ISchemaElementProxy)} of any subordinate item in the appropriate order and with the context
	 * items as required.
	 *
	 * This method returns the context items selected by the item to the parent item. Since some expressions (e.g.
	 * AxisExpression) may yield multiple potential matches, the return value consists of a collection of items.
	 *
	 * @param contextItem the context item for which the item is evaluated
	 * @return the context items returned to the parent expression
	 */
	ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem);

}
