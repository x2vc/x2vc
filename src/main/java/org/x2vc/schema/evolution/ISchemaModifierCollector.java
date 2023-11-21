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
package org.x2vc.schema.evolution;


import com.google.common.collect.ImmutableSet;

/**
 * This component is used to collect and consolidate {@link ISchemaModifier}s produced by various instances of
 * {@link IValueTraceAnalyzer} examining documents referring to the same schema and stylesheet. The modifiers collected are
 * consolidated in a way that ensures no duplicate modifiers are kept. Modifiers creating the same object (i.e.
 * attributes with the same name), but specifying different attributes will <b>not</b> be consolidated.
 */
public interface ISchemaModifierCollector {

	/**
	 * Resets the collector to its initial state.
	 */
	void clear();

	/**
	 * Adds a modifier to the collector.
	 *
	 * @param modifier
	 * @throws IllegalArgumentException if the modifier does not belong to the same schema and schema version as the
	 *                                  ones already recorded
	 */
	void addModifier(ISchemaModifier modifier) throws IllegalArgumentException;

	/**
	 * @return the consolidated set of modifiers
	 */
	ImmutableSet<ISchemaModifier> getConsolidatedModifiers();

	/**
	 * @return <code>true</code> if the collector does not contain any modifiers
	 */
	boolean isEmpty();

}
