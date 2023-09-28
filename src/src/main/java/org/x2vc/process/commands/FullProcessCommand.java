package org.x2vc.process.commands;

import java.util.concurrent.Callable;

import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Command to perform both the XSS scan and run the schema generation at the same time.
 */
@Command(name = "full", mixinStandardHelpOptions = true, description = "Performs both the XSS check and the schema generation.")
public class FullProcessCommand extends AbstractProcessCommand implements Callable<Integer> {

	@Inject
	FullProcessCommand(IProcessDirectorManager processDirector, IWorkerProcessManager workerProcessManager) {
		super(processDirector, workerProcessManager);
	}

	@Override
	protected ProcessingMode getProcessingMode() {
		return ProcessingMode.FULL;
	}

}
