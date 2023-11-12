package org.x2vc.process;

/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.time.Duration;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.tasks.ITask;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
	public void submit(ITask task) {
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

	@Override
	public synchronized void initialize() {
		logger.traceEntry();
		if (!isInitialized()) {
			initializeWorkerThreads();
			initializeWatcherThreads();
		}
		logger.traceExit();
	}

	/**
	 * Initializes the thread pool that performs the actual work.
	 */
	private void initializeWorkerThreads() {
		logger.info("Preparing pool of {}-{} worker threads with a timeout of {}", this.minThreadCount,
				this.maxThreadCount, this.threadTimeout);
		// see https://stackoverflow.com/a/24493856/218890 for this pattern
		this.taskQueue = new LinkedTransferQueue<Runnable>() {
			private static final long serialVersionUID = -7999280787101835452L;

			@Override
			public boolean offer(Runnable e) {
				return tryTransfer(e);
			}
		};
		final ThreadFactory workerThreadFactory = new ThreadFactoryBuilder().setNameFormat("worker-%02d").build();
		this.workerExecutor = new ThreadPoolExecutor(this.minThreadCount, this.maxThreadCount,
				this.threadTimeout.toMillis(), TimeUnit.MILLISECONDS, this.taskQueue, workerThreadFactory);
		this.workerExecutor.setRejectedExecutionHandler((r, executor) -> {
			try {
				executor.getQueue().put(r);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		this.exitingWorkerExecutorService = MoreExecutors.getExitingExecutorService(this.workerExecutor,
				this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Initializes the thread that reports on the queue state and progress.
	 */
	private void initializeWatcherThreads() {
		logger.debug("Will report on worker thread pool state every {}", this.reportInterval);
		final ThreadFactory watcherThreadFactory = new ThreadFactoryBuilder().setNameFormat("watcher-%01d").build();
		final ScheduledThreadPoolExecutor reportingExecutor = new ScheduledThreadPoolExecutor(1, watcherThreadFactory);
		this.reportExecutorService = MoreExecutors.getExitingScheduledExecutorService(reportingExecutor,
				this.shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
		this.reportExecutorService.scheduleWithFixedDelay(this::reportWorkerStatus, this.reportInterval.getNano(),
				this.reportInterval.toMillis(), TimeUnit.MILLISECONDS);
	}

	private void reportWorkerStatus() {
		logger.info(
				"Worker status: {} threads, tasks: {} queued --> {} executing --> {} completed of total {} scheduled ",
				this.workerExecutor.getPoolSize(), // threads
				this.taskQueue.size(), // queued
				this.workerExecutor.getActiveCount(), // executing
				this.workerExecutor.getCompletedTaskCount(), // completed
				this.workerExecutor.getTaskCount() // scheduled
		);
	}

	@Override
	public void awaitCompletion() throws InterruptedException {
		if (!isInitialized()) {
			throw new IllegalStateException("Worker process manager not yet initialized");
		}
		logger.debug("Waiting for all tasks to complete");
		while (this.workerExecutor.getActiveCount() > 0 || !this.taskQueue.isEmpty()) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				logger.warn("worker process manager interrupted while waiting for completion of tasks", e);
				throw e;
			}
		}
	}

	@Override
	public void shutdown() throws InterruptedException {
		awaitCompletion();
		logger.info("Shutting down worker threads");
		this.exitingWorkerExecutorService.shutdown();
		this.exitingWorkerExecutorService.awaitTermination(60, TimeUnit.SECONDS);
		reportWorkerStatus();

	}

}
