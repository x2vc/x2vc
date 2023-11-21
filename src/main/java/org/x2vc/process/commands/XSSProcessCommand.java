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

import java.util.concurrent.Callable;

import org.x2vc.process.IWorkerProcessManager;
import org.x2vc.process.VersionProvider;
import org.x2vc.process.tasks.ProcessingMode;

import com.google.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Command to perform only the XSS scan.
 */
@Command(name = "xss", description = "Only performs the XSS check.", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class XSSProcessCommand extends AbstractProcessCommand implements Callable<Integer> {

	@Inject
	XSSProcessCommand(IProcessDirectorManager processDirector, IWorkerProcessManager workerProcessManager) {
		super(processDirector, workerProcessManager);
	}

	@Override
	protected ProcessingMode getProcessingMode() {
		return ProcessingMode.XSS_ONLY;
	}

}
