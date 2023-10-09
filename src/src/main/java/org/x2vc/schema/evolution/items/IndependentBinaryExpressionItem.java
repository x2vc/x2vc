package org.x2vc.schema.evolution.items;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.BinaryExpression;

/**
 * {@link IEvaluationTreeItemFactory} to represent subclasses of {@link BinaryExpression} that do not perform any access
 * themselves, do not change the context element and evaluate the two sub-expressions independently of each other. This
 * is the case e.g. for comparison expressions. The processing is simply deferred to the base expressions.
 *
 * @param <T> the type of the object being evaluated
 */
public class IndependentBinaryExpressionItem<T extends BinaryExpression> extends AbstractEvaluationTreeItem<T> {

	private IEvaluationTreeItem lhsExpression;
	private IEvaluationTreeItem rhsExpression;

	IndependentBinaryExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, T target) {
		this.lhsExpression = itemFactory.createItemForExpression(target.getLhsExpression());
		this.rhsExpression = itemFactory.createItemForExpression(target.getRhsExpression());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			T target) {
		this.lhsExpression.evaluate(contextItem);
		this.rhsExpression.evaluate(contextItem);
		return ImmutableSet.of(contextItem);
	}

}
