package org.x2vc.schema.evolution.items;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link SortExpression}.
 */
public class SortExpressionItem extends AbstractEvaluationTreeItem<SortExpression> {

	private IEvaluationTreeItem selectItem;
	private List<IEvaluationTreeItem> keyItems;

	SortExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, SortExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, SortExpression target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
		final SortKeyDefinitionList keyList = target.getSortKeyDefinitionList();
		final Iterator<SortKeyDefinition> keyListIterator = keyList.iterator();
		this.keyItems = Stream.generate(keyListIterator::next)
			.map(key -> itemFactory.createItemForExpression(key))
			.toList();
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			SortExpression target) {
		final ImmutableCollection<ISchemaElementProxy> sortContexts = this.selectItem.evaluate(contextItem);
		for (final ISchemaElementProxy sortContext : sortContexts) {
			this.keyItems.forEach(item -> item.evaluate(sortContext));
		}
		return ImmutableSet.of(contextItem);
	}

}
