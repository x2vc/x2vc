package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link SortExpression}.
 */
public class SortExpressionItem extends AbstractEvaluationTreeItem<SortExpression> {

	private IEvaluationTreeItem selectItem;
	private IEvaluationTreeItem keyItems[];

	SortExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, SortExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, SortExpression target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
		final SortKeyDefinitionList keyDefinitionList = target.getSortKeyDefinitionList();
		this.keyItems = new IEvaluationTreeItem[keyDefinitionList.size()];
		for (int i = 0; i < this.keyItems.length; i++) {
			this.keyItems[i] = itemFactory.createItemForExpression(keyDefinitionList.getSortKeyDefinition(i));
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			SortExpression target) {
		final ImmutableCollection<ISchemaElementProxy> sortContexts = this.selectItem.evaluate(contextItem);
		for (final ISchemaElementProxy sortContext : sortContexts) {
			for (int i = 0; i < this.keyItems.length; i++) {
				this.keyItems[i].evaluate(sortContext);
			}
		}
		return ImmutableSet.of(contextItem);
	}

}
