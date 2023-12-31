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
package org.x2vc.report;

import java.io.File;

import com.google.inject.ImplementedBy;

/**
 * This component produces a formatted version of an {@link IVulnerabilityReport} and writes it to an output file.
 */
@ImplementedBy(ReportWriter.class)
public interface IReportWriter {

	/**
	 * Produces a formatted version of an {@link IVulnerabilityReport} and writes it to an output file;
	 *
	 * @param report
	 * @param outputFile
	 */
	void write(IVulnerabilityReport report, File outputFile);

}
