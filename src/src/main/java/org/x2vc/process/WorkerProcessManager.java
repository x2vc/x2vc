package org.x2vc.process;

import java.time.Duration;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IWorkerProcessManager}.
 */
@Singleton
public class WorkerProcessManager implements IWorkerProcessManager {

	private static final Logger logger = LogManager.getLogger();

	private Integer minThreadCount;
	private Integer maxThreadCount;
	private Duration threadTimeout;
	private Duration shutdownTimeout;
	private Duration reportInterval;

	private BlockingQueue<Runnable> taskQueue;
	private ThreadPoolExecutor workerExecutor;
	private ExecutorService exitingWorkerExecutorService;
	private ScheduledExecutorService reportExecutorService;

	@Inject
	WorkerProcessManager(@TypesafeConfig("x2vc.threads.min_count") Integer minThreadCount,
			@TypesafeConfig("x2vc.threads.max_count") Integer maxThreadCount,
			@TypesafeConfig("x2vc.threads.keepalive_time") Duration threadTimeout,
			@TypesafeConfig("x2vc.threads.shutdown_timeout") Duration shutdownTimeout,
			@TypesafeConfig("x2vc.threads.report_interval") Duration reportInterval) {
		this.minThreadCount = minThreadCount;
		this.maxThreadCount = maxThreadCount;
		this.threadTimeout = threadTimeout;
		this.shutdownTimeout = shutdownTimeout;
		this.reportInterval = reportInterval;
	}

	@Override
	public void submit(Runnable task) {
		logger.traceEntry();
		if (!isInitialized()) {
			initialize();
		}
		this.exitingWorkerExecutorService.submit(task);
		logger.traceExit();
	}

	/**
	 * @return
	 */
	private boolean isInitialized() {
		return this.taskQueue != null && this.workerExecutor != null && this.exitingWorkerExecutorService != null
				&& this.reportExecutorService != null;
	}

	/**
	 * Initializes the task handling components.
	 */
	private void initialize() {
		logger.traceEntry();

		logger.info("Preparing pool of {}-{} worker threads with a timeout of {}", this.minThreadCount,
				this.maxThreadCount, this.threadTimeout);

		// start the main task processor
		// see https://stackoverflow.com/a/24493856/218890 for this pattern
		this.taskQueue = new LinkedTransferQueue<Runnable>() {
			private static final long serialVersionUID = -7999280787101835452L;

			@Override
			public boolean offer(Runnable e) {
				return tryTransfer(e);
			}
		};
		this.workerExecutor = new ThreadPoolExecutor(this.minThreadCount, this.maxThreadCount,
				this.threadTimeout.toMillis(), TimeUnit.MILLISECONDS, this.taskQueue);
		this.workerExecutor.setRejectedExecutionHandler((r, executor) -> {
			try {
				executor.getQueue().put(r);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		this.exitingWorkerExecutorService = MoreExecutors.getExitingExecutorService(this.workerExecutor,
				this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);

		logger.debug("Will report on worker thread pool state every {}", this.reportInterval);
		final ScheduledThreadPoolExecutor reportingExecutor = new ScheduledThreadPoolExecutor(1);
		this.reportExecutorService = MoreExecutors.getExitingScheduledExecutorService(reportingExecutor,
				this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
		this.reportExecutorService.scheduleWithFixedDelay(this::reportWorkerStatus, this.reportInterval.getNano(),
				this.reportInterval.toMillis(), TimeUnit.MILLISECONDS);

		logger.traceExit();
	}

	private void reportWorkerStatus() {
		logger.info("worker status: {} threads, tasks: {} scheduled total, {} queued, {} executing, {} completed total",
				this.workerExecutor.getPoolSize(), // threads
				this.workerExecutor.getTaskCount(), // scheduled
				this.taskQueue.size(), // queued
				this.workerExecutor.getActiveCount(), // executing
				this.workerExecutor.getCompletedTaskCount() // completed
		);
	}

	@Override
	public void awaitTermination() throws InterruptedException {
		if (!isInitialized()) {
			throw new IllegalStateException("Worker process manager not yet initialized");
		}
		logger.info("Will await the completion of all tasks");
		while (this.workerExecutor.getActiveCount() > 0 || !this.taskQueue.isEmpty()) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				logger.warn("worker process manager interrupted while waiting for completion of tasks", e);
				throw e;
			}
		}
		this.exitingWorkerExecutorService.shutdown();
		this.exitingWorkerExecutorService.awaitTermination(60, TimeUnit.SECONDS);

	}

}
