package org.x2vc.process.tasks;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import org.x2vc.schema.evolution.ISchemaModifier;

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
	ISchemaEvolutionTask create(File xsltFile, List<ISchemaModifier> modifiers,
			Consumer<Boolean> callback);

}
