package org.x2vc.schema.evolution.items;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.evolution.SchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.pattern.NodeTest;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link AxisExpression}.
 */
public class AxisExpressionItem extends AbstractEvaluationTreeItem<AxisExpression> {

	private static final Logger logger = LogManager.getLogger();
	private INodeTestTreeItem nodeTest;

	AxisExpressionItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, AxisExpression target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, AxisExpression target) {
		final NodeTest targetNodeTest = target.getNodeTest();
		if (targetNodeTest != null) {
			this.nodeTest = itemFactory.createItemForNodeTest(target.getNodeTest());
		}
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			AxisExpression target) {
		final Collection<ISchemaElementProxy> candidateItems = selectCandidates(contextItem, target);
		if (this.nodeTest == null) {
			// "no node test" means "match any node"
			return ImmutableSet.copyOf(candidateItems);
		} else {
			this.nodeTest.evaluate(contextItem);
			return this.nodeTest.filter(candidateItems);
		}
	}

	/**
	 * Selects the nodes to be evaluated by the node test according to the axis.
	 *
	 * @param contextItem
	 * @param target
	 * @return
	 */
	private Collection<ISchemaElementProxy> selectCandidates(ISchemaElementProxy contextItem, AxisExpression target) {
		logger.traceEntry();
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		final int axis = target.getAxis();
		switch (axis) {
		// TODO support axis ANCESTOR
		// TODO support axis ANCESTOR_OR_SELF
		case AxisInfo.ATTRIBUTE:
			result.addAll(contextItem.getSubAttributes());
			break;
		case AxisInfo.CHILD:
			result.addAll(contextItem.getSubElements());
			break;
		case AxisInfo.DESCENDANT:
			result.addAll(collectDescendants(contextItem));
			break;
		case AxisInfo.DESCENDANT_OR_SELF:
			result.addAll(collectDescendants(contextItem));
			result.add(contextItem);
			break;
		// TODO support axis FOLLOWING
		// TODO support axis FOLLOWING_SIBLING
		// TODO support axis NAMESPACE
		case AxisInfo.PARENT:
			result.addAll(getParent(contextItem));
			break;
		// TODO support axis PRECEDING
		// TODO support axis PRECEDING_SIBLING
		case AxisInfo.SELF:
			result.add(contextItem);
			break;
		// TODO support axis PRECEDING_OR_ANCESTOR
		default:
			logger.warn("Unsupported axis {}: {}", AxisInfo.axisName[axis], target);
		}
		return logger.traceExit(result);
	}

	/**
	 * @param contextItem
	 * @return
	 */
	private Collection<? extends ISchemaElementProxy> collectDescendants(ISchemaElementProxy contextItem) {
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		final Deque<ISchemaElementProxy> remainingItems = Lists.newLinkedList();
		remainingItems.addAll(contextItem.getSubElements());
		while (!remainingItems.isEmpty()) {
			final ISchemaElementProxy nextItem = remainingItems.removeFirst();
			result.add(nextItem);
			remainingItems.addAll(nextItem.getSubElements());
		}
		return result;
	}

	/**
	 * @param contextItem
	 * @return
	 */
	private Collection<ISchemaElementProxy> getParent(ISchemaElementProxy contextItem) {
		logger.traceEntry();
		List<ISchemaElementProxy> result = Lists.newArrayList();
		if (contextItem.isElement()) {
			// get the possible referring elements
			final IXMLSchema schema = getSchema();
			result = schema.getElementTypes().stream()
				.filter(t -> t.hasElementContent())
				.filter(t -> t.getElements().stream()
					.anyMatch(r -> r.equals(contextItem.getElementReference().orElseThrow())))
				.flatMap(t -> schema.getReferencesUsing(t).stream())
				.map(SchemaElementProxy::new)
				.map(ISchemaElementProxy.class::cast)
				.toList();
		} else if (contextItem.isElementModifier()) {
			// TODO support retrieving parent of modifier
			logger.warn("Unsupported axis parent for element modifiers");
		} else {
			logger.warn("Unsupported axis parent for context item type {}", contextItem.getType());
		}
		return logger.traceExit(result);
	}

}
