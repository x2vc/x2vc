package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.evolution.SchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.RootExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent a {@link RootExpression}.
 */
public class RootExpressionItem extends AbstractEvaluationTreeItem<RootExpression> {

	RootExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, RootExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, RootExpression target) {
		// this expression does not have any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			RootExpression target) {
		// return the document root proxy
		return ImmutableSet.of(new SchemaElementProxy(getSchema()));
	}

}
