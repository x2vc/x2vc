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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.type.Type;

/**
 * {@link IEvaluationTreeItem} to represent an {@link NameTest}.
 */
public class NameTestItem extends AbstractNodeTestTreeItem<NameTest> {

	private static final Logger logger = LogManager.getLogger();

	NameTestItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, NameTest target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, NameTest target) {
		// this item does not require any subordinate items
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			NameTest target) {
		switch (target.getNodeKind()) {
		case Type.ELEMENT:
			registerElementAccess(contextItem, target.getMatchingNodeName());
			break;
		case Type.ATTRIBUTE:
			registerAttributeAccess(contextItem, target.getMatchingNodeName());
			break;
		// TODO #24 NameTest: support NodeKind PROCESSING_INSTRUCTION
		// TODO #19 NameTest: support NodeKind NAMESPACE
		default:
			logger.warn("Unsupported node kind {} in NameTest", target.getNodeKind());
		}

		// return the context item unchanged
		return ImmutableSet.of(contextItem);
	}

	@Override
	protected ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			NameTest target) {
		// TODO #19 NameTest: support full qualified names
		final String nameLocalPart = target.getMatchingNodeName().getLocalPart();
		final Set<ISchemaElementProxy> result = Sets.newHashSet();
		for (final ISchemaElementProxy item : candidateItems) {
			switch (target.getNodeKind()) {
			case Type.ELEMENT:
				if (item.getElementName().orElse("").equals(nameLocalPart)) {
					result.add(item);
				}
				break;
			case Type.ATTRIBUTE:
				if (item.getAttributeName().orElse("").equals(nameLocalPart)) {
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
