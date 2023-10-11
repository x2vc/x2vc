package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.instruct.NumberInstruction;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link NumberInstruction}.
 */
public class NumberInstructionItem extends AbstractEvaluationTreeItem<NumberInstruction> {

	private IEvaluationTreeItem selectItem;

	NumberInstructionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, NumberInstruction target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, NumberInstruction target) {
		this.selectItem = itemFactory.createItemForExpression(target.getSelect());
		// TODO check whether patterns need to be taken into account as well
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			NumberInstruction target) {
		this.selectItem.evaluate(contextItem);
		return ImmutableSet.of(contextItem);
	}

}
