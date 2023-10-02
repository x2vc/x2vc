package org.x2vc.process.tasks;

import java.io.File;
import java.util.UUID;

import org.apache.logging.log4j.CloseableThreadContext;

/**
 * Base class of task implementations with some common functions.
 */
public abstract class AbstractTask implements ITask {

	private UUID taskID = UUID.randomUUID();
	private File xsltFile;

	protected AbstractTask(File xsltFile) {
		super();
		this.xsltFile = xsltFile;
	}

	@Override
	public UUID getTaskID() {
		return this.taskID;
	}

	/**
	 * @return the stylesheet file to be processed
	 */
	protected File getXSLTFile() {
		return this.xsltFile;
	}

	/**
	 * Prepares a {@link CloseableThreadContext} to annotate the log entries with the stylesheet name.
	 *
	 * @return
	 */
	protected CloseableThreadContext.Instance getThreadContext() {
		return CloseableThreadContext.put("stylesheet", this.xsltFile.toString());
	}

	@Override
	public final void run() {
		try (final CloseableThreadContext.Instance ctc = getThreadContext()) {
			execute();
		}
	}

	/**
	 * Executes the actual function of the task.
	 */
	protected abstract void execute();

}
