package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Expression;

/**
 * {@link IEvaluationTreeItemFactory} to represent any operation that does not perform any access, does not involve any
 * subordinate items and does not change the context element. This is the case e.g. for literal expressions.
 */
public class NoOperationItem<T extends Expression> extends AbstractEvaluationTreeItem<T> {

	NoOperationItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, T target) {
		// this expression does not have any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			T target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
