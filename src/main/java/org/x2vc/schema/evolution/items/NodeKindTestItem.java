package org.x2vc.schema.evolution.items;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.Collection;
import java.util.Set;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.Type;

/**
 * {@link IEvaluationTreeItem} to represent an {@link NodeKindTest}.
 */
public class NodeKindTestItem extends AbstractNodeTestTreeItem<NodeKindTest> {

	NodeKindTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, NodeKindTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, NodeKindTest target) {
		// this item does not require any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			NodeKindTest target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			NodeKindTest target) {
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		for (final ISchemaElementProxy item : candidateItems) {
			switch (target.getNodeKind()) {
			case Type.ELEMENT:
				if (item.isElement() || item.isElementModifier()) {
					result.add(item);
				}
				break;
			case Type.ATTRIBUTE:
				if (item.isAttribute() || item.isAttributeModifier()) {
					result.add(item);
				}
				break;
			default:
				// already warned in evaluate() - just pass through
				result.add(item);
			}
		}
		return ImmutableSet.copyOf(result);
	}

}
