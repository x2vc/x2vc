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

import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.pattern.CombinedNodeTest;
import net.sf.saxon.pattern.NodeTest;

/**
 * {@link IEvaluationTreeItem} to represent an {@link CombinedNodeTest}.
 */
public class CombinedNodeTestItem extends AbstractNodeTestTreeItem<CombinedNodeTest> {

	private static final Logger logger = LogManager.getLogger();

	private INodeTestTreeItem componentItem1;
	private INodeTestTreeItem componentItem2;

	CombinedNodeTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, CombinedNodeTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, CombinedNodeTest target) {
		final NodeTest[] componentTests = target.getComponentNodeTests();
		this.componentItem1 = itemFactory.createItemForNodeTest(componentTests[0]);
		this.componentItem2 = itemFactory.createItemForNodeTest(componentTests[1]);
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			CombinedNodeTest target) {
		this.componentItem1.evaluate(contextItem);
		this.componentItem2.evaluate(contextItem);
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			CombinedNodeTest target) {
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		switch (target.getOperator()) {
		case Token.UNION:
			result.addAll(this.componentItem1.filter(candidateItems));
			result.addAll(this.componentItem2.filter(candidateItems));
			break;

		case Token.INTERSECT:
			result.addAll(this.componentItem2.filter(this.componentItem1.filter(candidateItems)));
			break;

		case Token.EXCEPT:
			result.addAll(this.componentItem1.filter(candidateItems));
			result.removeAll(this.componentItem2.filter(candidateItems));
			break;

		default:
			logger.warn("Unknwon CombinedNodeTest operator {}", target.getOperator());
			result.addAll(candidateItems);

		}

		return ImmutableSet.copyOf(result);
	}

}
