package org.x2vc.schema.evolution.items;

import java.util.Collection;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.pattern.AnyNodeTest;

/**
 * {@link IEvaluationTreeItem} to represent an {@link AnyNodeTest}.
 */
public class AnyNodeTestItem extends AbstractNodeTestTreeItem<AnyNodeTest> {

	AnyNodeTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, AnyNodeTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, AnyNodeTest target) {
		// no subordinate items to initialize
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem, AnyNodeTest target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			AnyNodeTest target) {
		// return the nodes without filtering
		return ImmutableSet.copyOf(candidateItems);
	}

}
