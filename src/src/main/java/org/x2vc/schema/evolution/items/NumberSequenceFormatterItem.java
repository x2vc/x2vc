package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import net.sf.saxon.expr.NumberSequenceFormatter;
import net.sf.saxon.expr.Operand;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link NumberSequenceFormatter}.
 */
public class NumberSequenceFormatterItem extends AbstractEvaluationTreeItem<NumberSequenceFormatter> {

	private IEvaluationTreeItem[] operandItems;

	NumberSequenceFormatterItem(IXMLSchema schema, IModifierCreationCoordinator coordinator,
			NumberSequenceFormatter target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, NumberSequenceFormatter target) {
		final Operand[] operands = Iterables.toArray(target.operands(), Operand.class);
		this.operandItems = new IEvaluationTreeItem[operands.length];
		for (int i = 0; i < operands.length; i++) {
			this.operandItems[i] = itemFactory.createItemForExpression(operands[i].getChildExpression());
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			NumberSequenceFormatter target) {
		for (int i = 0; i < this.operandItems.length; i++) {
			this.operandItems[i].evaluate(contextItem);
		}
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

}
