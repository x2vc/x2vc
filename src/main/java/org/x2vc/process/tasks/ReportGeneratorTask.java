package org.x2vc.process.tasks;

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
import java.net.URI;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.report.IReportWriter;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.IVulnerabilityCandidateCollector;
import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.stylesheet.coverage.ICoverageStatistics;
import org.x2vc.stylesheet.coverage.ICoverageTraceAnalyzer;
import org.x2vc.stylesheet.coverage.ILineCoverage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to generate the final report for a stylesheet..
 */
public class ReportGeneratorTask extends AbstractTask implements IReportGeneratorTask {

	private static final Logger logger = LogManager.getLogger();

	private IDocumentAnalyzer analyzer;
	private IVulnerabilityCandidateCollector vulnerabilityCandidateCollector;
	private ICoverageTraceAnalyzer coverageTraceAnalyzer;
	private IReportWriter reportWriter;
	private Consumer<Boolean> callback;

	/**
	 */
	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	ReportGeneratorTask(IDocumentAnalyzer analyzer, IVulnerabilityCandidateCollector vulnerabilityCandidateCollector,
			ICoverageTraceAnalyzer coverageTraceAnalyzer, IReportWriter reportWriter,
			@Assisted File xsltFile,
			@Assisted Consumer<Boolean> callback) {
		super(xsltFile);
		this.analyzer = analyzer;
		this.vulnerabilityCandidateCollector = vulnerabilityCandidateCollector;
		this.coverageTraceAnalyzer = coverageTraceAnalyzer;
		this.reportWriter = reportWriter;
		this.callback = callback;
	}

	@Override
	public void execute() {
		logger.traceEntry("for task ID {}", this.getTaskID());
		try {
			final URI stylesheetURI = this.getXSLTFile().toURI();

			final ImmutableSet<IVulnerabilityCandidate> vulnerabilityCandidates = this.vulnerabilityCandidateCollector
				.get(stylesheetURI);
			final ICoverageStatistics coverageStatistics = this.coverageTraceAnalyzer.getStatistics(stylesheetURI);
			final ImmutableList<ILineCoverage> codeCoverage = this.coverageTraceAnalyzer.getCodeCoverage(stylesheetURI);

			final Object basename = Files.getNameWithoutExtension(this.getXSLTFile().getName());
			final File outputFile = new File(this.getXSLTFile().getParentFile(),
					String.format("%s_x2vc_report.html", basename));

			logger.info("Consolidating {} vulnerability candidates for stylesheet {} into report file {}",
					vulnerabilityCandidates.size(),
					this.getXSLTFile(), outputFile);

			final IVulnerabilityReport report = this.analyzer.consolidateResults(stylesheetURI,
					vulnerabilityCandidates, coverageStatistics, codeCoverage);

			this.reportWriter.write(report, outputFile);
			this.callback.accept(true);
		} catch (final Exception ex) {
			logger.error("unhandled exception in report generator task", ex);
			this.callback.accept(false);
		}
		logger.traceExit();
	}

}
