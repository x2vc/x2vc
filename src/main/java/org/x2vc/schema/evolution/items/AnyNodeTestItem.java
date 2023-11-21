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
package org.x2vc.schema.evolution.items;


import java.util.Collection;

import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.pattern.AnyNodeTest;

/**
 * {@link IEvaluationTreeItem} to represent an {@link AnyNodeTest}.
 */
public class AnyNodeTestItem extends AbstractNodeTestTreeItem<AnyNodeTest> {

	AnyNodeTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, AnyNodeTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, AnyNodeTest target) {
		// no subordinate items to initialize
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem, AnyNodeTest target) {
		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			AnyNodeTest target) {
		// return the nodes without filtering
		return ImmutableSet.copyOf(candidateItems);
	}

}
