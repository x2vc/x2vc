package org.x2vc.schema.evolution.items;

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
