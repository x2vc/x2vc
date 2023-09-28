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
