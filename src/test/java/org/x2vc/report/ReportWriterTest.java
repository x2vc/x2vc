package org.x2vc.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.coverage.*;

import net.sf.saxon.s9api.Processor;

@ExtendWith(MockitoExtension.class)
class ReportWriterTest {

	private static final Logger logger = LogManager.getLogger();

	private URI stylesheetURI;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.stylesheetURI = URI.create("file:///foo/bar/foobar.xslt");
	}

	private IVulnerabilityReport buildTestReport() {
		// we have to use the "real" objects here to test the JAXB marshalling and unmarshalling
		final LocalDateTime checkDate = LocalDateTime.now().minusDays(7);
		final ICoverageStatistics coverageStatistics = CoverageStatistics.builder()
			.withTotalDirectiveCount(200)
			.withFullCoverageDirectiveCount(100)
			.withPartialCoverageDirectiveCount(75)
			.withNoCoverageDirectiveCount(25)
			.withTotalLineCount(400)
			.withEmptyLineCount(50)
			.withFullCoverageLineCount(250)
			.withPartialCoverageLineCount(75)
			.withNoCoverageLineCount(25)
			.build();
		final ILogMessage message1 = new LogMessage(Level.INFO, "thread", "message 1");
		final IVulnerabilityReportIssue issue1 = VulnerabilityReportIssue.builder()
			.withAffectedOutputElement("output")
			.addAffectingInputElement("input1")
			.addAffectingInputElement("input2")
			.addAffectingInputElements(List.of("input3", "input4"))
			.addExample(new VulnerabilityReportExample("is1", "os1"))
			.addExample("is2", "os2")
			.build();
		final IVulnerabilityReportSection section1 = VulnerabilityReportSection.builder()
			.withRuleID("ruleID1")
			.withHeading("head1")
			.withShortHeading("shortHeading1")
			.withIntroduction("intro1")
			.withDescription("desc1")
			.withCountermeasures("cm1")
			.addIssue(issue1)
			.build();
		final List<ILineCoverage> codeCoverage = List.of(
				new LineCoverage(1, "<foo>", CoverageStatus.EMPTY),
				new LineCoverage(2, "<bar/>", CoverageStatus.FULL),
				new LineCoverage(3, "'baz'", CoverageStatus.NONE),
				new LineCoverage(4, "\"bang\"", CoverageStatus.PARTIAL));
		return VulnerabilityReport.builder(this.stylesheetURI)
			.withCheckDate(checkDate)
			.withCoverageStatistics(coverageStatistics)
			.addMessage(message1)
			.addSection(section1)
			.withCodeCoverage(codeCoverage)
			.build();
	}

	/**
	 * Test method for {@link org.x2vc.report.ReportWriter#write(org.x2vc.report.IVulnerabilityReport, java.io.File)}.
	 */
	@Test
	void testWriteWithoutXML(@TempDir Path tempDir) {
		logger.debug("Using temporary path {}", tempDir);
		final var processor = new Processor();
		final var writer = new ReportWriter(processor, false);
		final var report = buildTestReport();
		final String reportFilename = "sample_report.html";
		final File outputFile = new File(tempDir.toFile(), reportFilename);
		writer.write(report, outputFile);

		assertFileExists(tempDir, reportFilename);
		assertFileExists(tempDir, ".x2vc", "css", "x2vc.css");
	}

	/**
	 * Test method for {@link org.x2vc.report.ReportWriter#write(org.x2vc.report.IVulnerabilityReport, java.io.File)}.
	 */
	@Test
	void testWriteWithXML(@TempDir Path tempDir) {
		logger.debug("Using temporary path {}", tempDir);
		final var processor = new Processor();
		final var writer = new ReportWriter(processor, true);
		final var report = buildTestReport();
		final String reportFilename = "sample_report.html";
		final File outputFile = new File(tempDir.toFile(), reportFilename);
		writer.write(report, outputFile);

		assertFileExists(tempDir, reportFilename);
		assertFileExists(tempDir, reportFilename.replace(".html", ".xml"));
		assertFileExists(tempDir, ".x2vc", "css", "x2vc.css");
	}

	private void assertFileExists(Path basePath, String... pathComponents) {
		Path p = basePath;
		for (final var c : pathComponents) {
			p = p.resolve(c);
		}
		final var file = p.toFile();
		assertTrue(file.exists(), String.format("File %s should exist", file.toString()));
	}
}
