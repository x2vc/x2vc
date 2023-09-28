package org.x2vc.process.commands;

import java.util.concurrent.Callable;

import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Command to perform only the schema generation.
 */
@Command(name = "schema", mixinStandardHelpOptions = true, description = "Only performs the schema generation.")
public class SchemaProcessCommand extends AbstractProcessCommand implements Callable<Integer> {

	@Inject
	SchemaProcessCommand(IProcessDirectorManager processDirector, IWorkerProcessManager workerProcessManager) {
		super(processDirector, workerProcessManager);
	}

	@Override
	protected ProcessingMode getProcessingMode() {
		return ProcessingMode.SCHEMA_ONLY;
	}

}
