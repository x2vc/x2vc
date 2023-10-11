package org.x2vc.schema.evolution.items;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.expr.Assignation;

/**
 * {@link IEvaluationTreeItemFactory} to represent a {@link Assignation}.
 *
 * @param <T>
 *
 */
public class AssignationItem<T extends Assignation> extends AbstractEvaluationTreeItem<T> {

	private static final Logger logger = LogManager.getLogger();

	private IEvaluationTreeItem sequenceItem;
	private IEvaluationTreeItem actionItem;

	AssignationItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, T target) {
		this.sequenceItem = itemFactory.createItemForExpression(target.getSequence());
		this.actionItem = itemFactory.createItemForExpression(target.getAction());
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			T target) {
		final ImmutableCollection<ISchemaElementProxy> sequenceResults = this.sequenceItem.evaluate(contextItem);
		logger.debug("evaluation of sequence yielded {} context items", sequenceResults.size());
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		for (final ISchemaElementProxy newContext : sequenceResults) {
			logger.trace("evaluating action for result item {}", newContext);
			result.addAll(this.actionItem.evaluate(newContext));
		}
		return ImmutableSet.copyOf(result);
	}

}
