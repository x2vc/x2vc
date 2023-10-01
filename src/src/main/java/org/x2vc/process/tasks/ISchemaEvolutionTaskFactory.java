package org.x2vc.process.tasks;

import java.io.File;
import java.util.function.Consumer;

import org.x2vc.schema.evolution.ISchemaModifierCollector;

/**
 * Factory to obtain instances of {@link ISchemaEvolutionTask}.
 */
public interface ISchemaEvolutionTaskFactory {

	/**
	 * Creates a new {@link ISchemaEvolutionTask}
	 *
	 * @param xsltFile
	 * @param modifierCollector
	 * @param callback
	 * @return the task
	 */
	ISchemaEvolutionTask create(File xsltFile, ISchemaModifierCollector modifierCollector,
			Consumer<Boolean> callback);

}
