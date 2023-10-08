package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.expr.UnaryExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent subclasses of {@link UnaryExpression} that do not perform any access
 * themselves and do not change the context element themselves. The processing is simply deferred to the base
 * expression.
 *
 * @param <T> the type of the object being evaluated
 */
public class UnaryExpressionItem<T extends UnaryExpression> extends AbstractEvaluationTreeItem<T> {

	private IEvaluationTreeItem baseItem;

	UnaryExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, T target) {
		this.baseItem = itemFactory.createItemForExpression(target.getBaseExpression());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			T target) {
		return this.baseItem.evaluate(contextItem);
	}

}
