package org.x2vc.schema.evolution.items;

import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.pattern.NodeTest;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link AxisExpression}.
 */
public class AxisExpressionItem extends AbstractEvaluationTreeItem<AxisExpression> {

	private static final Logger logger = LogManager.getLogger();
	private IEvaluationTreeItem nodeTest;

	AxisExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, AxisExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, AxisExpression target) {
		final NodeTest targetNodeTest = target.getNodeTest();
		if (targetNodeTest != null) {
			this.nodeTest = itemFactory.createItem(target.getNodeTest());
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			AxisExpression target) {
		final Collection<ISchemaElementProxy> candidateItems = selectCandidates(contextItem, target.getAxis());
		if (this.nodeTest == null) {
			// "no node test" means "match any node"
			return ImmutableSet.copyOf(candidateItems);
		} else {
			final Set<ISchemaElementProxy> result = Sets.newHashSet();
			for (final ISchemaElementProxy candidate : candidateItems) {
				result.addAll(this.nodeTest.evaluate(candidate));
			}
			return ImmutableSet.copyOf(result);
		}
	}

	/**
	 * Selects the nodes to be evaluated by the node test according to the axis.
	 *
	 * @param contextItem
	 * @param axis
	 * @return
	 */
	private Collection<ISchemaElementProxy> selectCandidates(ISchemaElementProxy contextItem, int axis) {
		logger.traceEntry();
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		switch (axis) {
		// TODO support axis ANCESTOR
		// TODO support axis ANCESTOR_OR_SELF
		case AxisInfo.ATTRIBUTE:
			result.addAll(contextItem.getSubAttributes());
			break;
		case AxisInfo.CHILD:
			result.addAll(contextItem.getSubElements());
			break;
		// TODO support axis DESCENDANT
		// TODO support axis DESCENDANT_OR_SELF
		// TODO support axis FOLLOWING
		// TODO support axis FOLLOWING_SIBLING
		// TODO support axis NAMESPACE
		// TODO support axis PARENT
		// TODO support axis PRECEDING
		// TODO support axis PRECEDING_SIBLING
		case AxisInfo.SELF:
			result.add(contextItem);
			break;
		// TODO support axis PRECEDING_OR_ANCESTOR
		default:
			logger.warn("Unsupported axis {}", AxisInfo.axisName[axis]);
		}
		return logger.traceExit(result);
	}

}
