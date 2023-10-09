package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.instruct.Block;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link Block}.
 */
public class BlockItem extends AbstractEvaluationTreeItem<Block> {

	private IEvaluationTreeItem[] operandItems;

	BlockItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, Block target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, Block target) {
		final Operand[] operands = target.getOperanda();
		this.operandItems = new IEvaluationTreeItem[operands.length];
		for (int i = 0; i < operands.length; i++) {
			this.operandItems[i] = itemFactory.createItemForExpression(operands[i].getChildExpression());
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			Block target) {
		// evaluate the subordinate expressions
		for (int i = 0; i < this.operandItems.length; i++) {
			this.operandItems[i].evaluate(contextItem);
		}
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
