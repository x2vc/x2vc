package org.x2vc.process.commands;

import java.util.concurrent.Callable;

import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.tasks.ITaskFactory;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Command to perform only the XSS scan.
 */
@Command(name = "xss", mixinStandardHelpOptions = true, description = "Only performs the XSS check.")
public class XSSProcessCommand extends AbstractProcessCommand implements Callable<Integer> {

	/**
	 * @param taskFactory
	 * @param workerProcessManager
	 */
	@Inject
	XSSProcessCommand(ITaskFactory taskFactory, IWorkerProcessManager workerProcessManager) {
		super(taskFactory, workerProcessManager);
	}

	@Override
	protected ProcessingMode getProcessingMode() {
		return ProcessingMode.XSS_ONLY;
	}

}
