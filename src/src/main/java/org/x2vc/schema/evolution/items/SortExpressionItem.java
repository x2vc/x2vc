package org.x2vc.schema.evolution.items;

import java.util.List;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link SortExpression}.
 */
public class SortExpressionItem extends AbstractEvaluationTreeItem<SortExpression> {

	private IEvaluationTreeItem selectItem;
	private List<IEvaluationTreeItem> keyOperandChildItems;

	SortExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, SortExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, SortExpression target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
		final SortKeyDefinitionList keyDefinitionList = target.getSortKeyDefinitionList();
		this.keyOperandChildItems = Lists.newArrayList();
		for (int i = 0; i < keyDefinitionList.size(); i++) {
			final SortKeyDefinition sortKeyDefinition = keyDefinitionList.getSortKeyDefinition(i);
			final Iterable<Operand> operands = sortKeyDefinition.operands();
			for (final Operand operand : operands) {
				this.keyOperandChildItems.add(itemFactory.createItemForExpression(operand.getChildExpression()));
			}
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			SortExpression target) {
		final ImmutableCollection<ISchemaElementProxy> sortContexts = this.selectItem.evaluate(contextItem);
		for (final ISchemaElementProxy sortContext : sortContexts) {
			for (final IEvaluationTreeItem keyItem : this.keyOperandChildItems) {
				keyItem.evaluate(sortContext);
			}
		}
		return ImmutableSet.of(contextItem);
	}

}
