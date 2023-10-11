package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.instruct.DocumentInstr;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link DocumentInstr}.
 */
public class DocumentInstrItem extends AbstractEvaluationTreeItem<DocumentInstr> {

	private IEvaluationTreeItem contentItem;

	DocumentInstrItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, DocumentInstr target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, DocumentInstr target) {
		this.contentItem = itemFactory.createItemForExpression(target.getContentExpression());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			DocumentInstr target) {
		this.contentItem.evaluate(contextItem);
		return ImmutableSet.of(contextItem);
	}

}
