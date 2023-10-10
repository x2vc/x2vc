package org.x2vc.schema.evolution.items;

import java.util.Collection;
import java.util.Set;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.type.UType;

/**
 * {@link IEvaluationTreeItem} to represent an {@link MultipleNodeKindTest}.
 */
public class MultipleNodeKindTestItem extends AbstractNodeTestTreeItem<MultipleNodeKindTest> {

	MultipleNodeKindTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, MultipleNodeKindTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, MultipleNodeKindTest target) {
		// this item does not require any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			MultipleNodeKindTest target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			MultipleNodeKindTest target) {
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		final UType type = target.getUType();
		for (final ISchemaElementProxy item : candidateItems) {
			switch (item.getType()) {
			case ATTRIBUTE:
				if (type.overlaps(UType.ATTRIBUTE)) {
					result.add(item);
				}
				break;
			case ATTRIBUTE_MODIFIER:
				if (type.overlaps(UType.ATTRIBUTE)) {
					result.add(item);
				}
				break;
			case DOCUMENT:
				if (type.overlaps(UType.DOCUMENT)) {
					result.add(item);
				}
				break;
			case ELEMENT:
				if (type.overlaps(UType.ELEMENT)) {
					result.add(item);
				}
				break;
			case ELEMENT_MODIFIER:
				if (type.overlaps(UType.ELEMENT)) {
					result.add(item);
				}
				break;
			}
		}
		return ImmutableSet.copyOf(result);
	}

}
