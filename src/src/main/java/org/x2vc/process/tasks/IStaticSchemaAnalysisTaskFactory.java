package org.x2vc.process.tasks;

import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.x2vc.schema.evolution.ISchemaModifier;

/**
 * Factory to obtain instances of {@link IStaticSchemaAnalysisTask}.
 */
public interface IStaticSchemaAnalysisTaskFactory {

	/**
	 * Creates a new {@link IStaticSchemaAnalysisTask}
	 *
	 * @param xsltFile
	 * @param modifierCollector
	 * @param callback
	 * @return the task
	 */
	IStaticSchemaAnalysisTask create(File xsltFile, Consumer<ISchemaModifier> modifierCollector,
			BiConsumer<UUID, Boolean> callback);

}
