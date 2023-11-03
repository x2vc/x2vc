package org.x2vc.process.commands;

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

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IProcessDirectorManager}. Caution: Needs to be hyper-threadsafe, concurrent access
 * everywhere!
 */
@Singleton
public class ProcessDirectorManager implements IProcessDirectorManager {

	private static final Logger logger = LogManager.getLogger();

	private IProcessDirectorFactory directorFactory;
	private Map<File, IProcessDirector> directors = new ConcurrentHashMap<>();

	@Inject
	ProcessDirectorManager(IProcessDirectorFactory directorFactory) {
		this.directorFactory = directorFactory;
	}

	@Override
	public synchronized void startProcess(File xsltFile, ProcessingMode mode) {
		logger.traceEntry();
		logger.info("Starting processing of stylesheet {}", xsltFile);
		final IProcessDirector director = this.directors.computeIfAbsent(xsltFile,
				f -> this.directorFactory.create(xsltFile, mode));
		if (director.getProcessState() == ProcessState.NEW) {
			director.initialize();
		}
		logger.traceExit();
	}

	@Override
	public ProcessState getProcessState(File xsltFile) {
		if (!this.directors.containsKey(xsltFile)) {
			throw new IllegalArgumentException(
					String.format("Stylesheet %s not submitted for processing yet.", xsltFile));
		} else {
			return this.directors.get(xsltFile).getProcessState();
		}
	}

	@Override
	public boolean isCompleted() {
		return this.directors.values().stream().allMatch(director -> director.getProcessState() == ProcessState.DONE);
	}

	@Override
	public void awaitCompletion() throws InterruptedException {
		logger.debug("Waiting for all processes to complete");
		while (!isCompleted()) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				logger.warn("process director interrupted while waiting for completion of tasks", e);
				throw e;
			}
		}
	}

}
