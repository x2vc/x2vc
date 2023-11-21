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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.om.StructuredQName;

/**
 * Base implementation of {@link IEvaluationTreeItem}.
 *
 * @param <T> the type of the object being evaluated
 */
public abstract class AbstractNodeTestTreeItem<T> implements INodeTestTreeItem {

	private static final Logger logger = LogManager.getLogger();

	private final IXMLSchema schema;
	private final T target;
	private final IModifierCreationCoordinator coordinator;
	private boolean initialized;

	/**
	 * Creates a new item.
	 *
	 * @param target
	 */
	protected AbstractNodeTestTreeItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, T target) {
		this.schema = schema;
		this.coordinator = coordinator;
		this.target = target;
		this.initialized = false;
	}

	@Override
	public final void initialize(IEvaluationTreeItemFactory itemFactory) {
		logger.trace("initializing {} for {} {}", this.getClass().getSimpleName(),
				this.target.getClass().getSimpleName(), this.target);
		initialize(itemFactory, this.target);
		this.initialized = true;
	}

	/**
	 * Initializes the item fully. Use this method to create all subordinate items required to represent the target
	 * object using the factory supplied. <b>DO NOT</b> initialize the created items - this will be performed by an
	 * outside actor. (Main reason: avoiding a big tangled mess of untestable code).
	 *
	 * @param itemFactory a factory to create subordinate items
	 * @param target      the target object of the item
	 */
	protected abstract void initialize(IEvaluationTreeItemFactory itemFactory, T target);

	@Override
	public final ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem) {
		if (!this.initialized) {
			throw logger.throwing(new IllegalStateException("evaluate() called on uninitialized item"));
		}
		logger.trace("evaluating {} for {} {}", this.getClass().getSimpleName(), this.target.getClass().getSimpleName(),
				this.target);
		return evaluate(contextItem, this.target);
	}

	/**
	 * Performs the actual evaluation based on the context item provided. If applicable, this method is supposed to call
	 * {@link #evaluate(ISchemaElementProxy)} of any subordinate item in the appropriate order and with the context
	 * items as required.
	 *
	 * This method returns the context items selected by the item to the parent item. Since some expressions (e.g.
	 * AxisExpression) may yield multiple potential matches, the return value consists of a collection of items.
	 *
	 * @param contextItem the context item for which the item is evaluated
	 * @param target      the target object of the item
	 * @return the context items returned to the parent expression
	 */
	protected abstract ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem, T target);

	@Override
	public ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems) {
		if (!this.initialized) {
			throw logger.throwing(new IllegalStateException("evaluate() called on uninitialized item"));
		}
		logger.trace("filtering using {} for {} {}", this.getClass().getSimpleName(),
				this.target.getClass().getSimpleName(),
				this.target);
		return filter(candidateItems, this.target);
	}

	/**
	 * Performs the actual node test on every candidate item and only returns the ones that pass the node test.
	 *
	 * @param candidateItems the context items to check using the node test
	 * @param target         the target object of the item
	 * @return the context items that pass the node test
	 */
	protected abstract ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems,
			T target);

	/**
	 * @return the schema
	 */
	public IXMLSchema getSchema() {
		return this.schema;
	}

	/**
	 * Notify the coordinator that an element access was attempted.
	 *
	 * @param contextItem
	 * @param elementName
	 * @return the proxy representing the element that was accessed
	 */
	public ISchemaElementProxy registerElementAccess(ISchemaElementProxy contextItem, StructuredQName elementName) {
		logger.debug("registered access to element {} of item {}", elementName, contextItem);
		return this.coordinator.handleElementAccess(contextItem, elementName);
	}

	/**
	 * Notify the coordinator that an attribute access was attempted.
	 *
	 * @param contextItem
	 * @param attributeName
	 */
	public void registerAttributeAccess(ISchemaElementProxy contextItem, StructuredQName attributeName) {
		logger.debug("registered access to attribute {} of item {}", attributeName, contextItem);
		this.coordinator.handleAttributeAccess(contextItem, attributeName);
	}

}
