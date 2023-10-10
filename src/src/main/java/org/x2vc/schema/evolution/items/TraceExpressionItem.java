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
