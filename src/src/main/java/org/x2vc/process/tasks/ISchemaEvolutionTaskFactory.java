package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

import org.x2vc.schema.evolution.ISchemaModifier;

import com.google.common.collect.ImmutableSet;

/**
 * Factory to obtain instances of {@link ISchemaEvolutionTask}.
 */
public interface ISchemaEvolutionTaskFactory {

	/**
	 * Creates a new {@link ISchemaEvolutionTask}
	 *
	 * @param xsltFile
	 * @param modifiers
	 * @param callback
	 * @return the task
	 */
	ISchemaEvolutionTask create(File xsltFile, ImmutableSet<ISchemaModifier> modifiers,
			Consumer<Boolean> callback);

}
