package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.AttributeGetter;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link AttributeGetter}.
 */
public class AttributeGetterItem extends AbstractEvaluationTreeItem<AttributeGetter> {

	AttributeGetterItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, AttributeGetter target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, AttributeGetter target) {
		// this expression does not have any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			AttributeGetter target) {
		registerAttributeAccess(contextItem, target.getAttributeName().getStructuredQName());
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
