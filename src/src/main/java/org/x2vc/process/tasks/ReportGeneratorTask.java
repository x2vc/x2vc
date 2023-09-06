package org.x2vc.process.tasks;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.IDocumentAnalyzer;
import org.x2vc.report.IReportWriter;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.IVulnerabilityCandidateCollector;
import org.x2vc.report.IVulnerabilityReport;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * This task is used to generate the final report for a stylesheet..
 */
public class ReportGeneratorTask implements IReportGeneratorTask {

	private static final Logger logger = LogManager.getLogger();

	private IDocumentAnalyzer analyzer;
	private IVulnerabilityCandidateCollector vulnerabilityCandidateCollector;
	private IReportWriter reportWriter;
	private File xsltFile;

	private UUID taskID;

	int nextCandidateNumber = 1;

	/**
	 */
	@SuppressWarnings("java:S107") // large number of parameters due to dependency injection
	@Inject
	ReportGeneratorTask(IDocumentAnalyzer analyzer, IVulnerabilityCandidateCollector vulnerabilityCandidateCollector,
			IReportWriter reportWriter, @Assisted File xsltFile) {
		super();
		this.analyzer = analyzer;
		this.vulnerabilityCandidateCollector = vulnerabilityCandidateCollector;
		this.reportWriter = reportWriter;
		this.xsltFile = xsltFile;
		this.taskID = UUID.randomUUID();
	}

	@Override
	public void run() {
		logger.traceEntry("for task ID {}", this.taskID);
		try {
			final URI stylesheetURI = this.xsltFile.toURI();
			final ImmutableSet<IVulnerabilityCandidate> vulnerabilityCandidates = this.vulnerabilityCandidateCollector
				.get(stylesheetURI);
			logger.info("Consolidating {} vulnerability candidates for stylesheet {}", vulnerabilityCandidates.size(),
					this.xsltFile);
			final IVulnerabilityReport report = this.analyzer.consolidateResults(stylesheetURI,
					vulnerabilityCandidates);
			final Object basename = Files.getNameWithoutExtension(this.xsltFile.getName());
			final File outputFile = new File(this.xsltFile.getParentFile(),
					String.format("%s_x2vc_report.html", basename));
			logger.info("Writing report to output file {}", outputFile);
			this.reportWriter.write(report, outputFile);
		} catch (final Exception ex) {
			logger.error("unhandled exception in report generator task", ex);
		}
		logger.traceExit();
	}

}
