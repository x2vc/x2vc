package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.integration.IReportVerifier;
import org.x2vc.report.ILogMessage;
import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Check invariants that must be true for all reports, regardless of the actual contents.
 */
public abstract class InvariantVerifier implements IReportVerifier {

	private static final Logger logger = LogManager.getLogger();

	private String testCaseName;
	private int sectionIndex;
	private int issueIndex;

	/**
	 * @param name the name of the test case being verified
	 */
	public InvariantVerifier(String name) {
		this.testCaseName = name;
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		final ImmutableList<IVulnerabilityReportSection> sections = report.getSections();
		assertEquals(11, sections.size(), "number of sections must be fixed");

		// FIXME add test case name to improve output usability

		this.sectionIndex = 0;
		sections.forEach(section -> {
			this.sectionIndex++;
			assertFalse(Strings.isNullOrEmpty(section.getRuleID()),
					String.format("%s: rule ID of section %d must be present", this.testCaseName, this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getHeading()),
					String.format("%s: heading of section %d must be present", this.testCaseName, this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getShortHeading()),
					String.format("%s: short introduction of section %d must be present", this.testCaseName,
							this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getIntroduction()),
					String.format("%s: introduction of section %d must be present", this.testCaseName,
							this.sectionIndex));
			final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
			if (issues.size() != 0) {
				assertFalse(Strings.isNullOrEmpty(section.getDescription()),
						String.format("%s: description of section %d must be present", this.testCaseName,
								this.sectionIndex));
				assertFalse(Strings.isNullOrEmpty(section.getCountermeasures()),
						String.format("%s: countermeasures of section %d must be present", this.testCaseName,
								this.sectionIndex));

				this.issueIndex = 0;
				issues.forEach(issue -> {
					this.issueIndex++;
					assertNotEquals(0, issue.getAffectingInputElements().size(),
							String.format("%s: affecting input elements of issue %d in section %d must be present",
									this.testCaseName, this.issueIndex, this.sectionIndex));
					assertFalse(Strings.isNullOrEmpty(issue.getAffectedOutputElement()),
							String.format("%s: affected output element of issue %d in section %d must be present",
									this.testCaseName, this.issueIndex, this.sectionIndex));
					assertNotEquals(0, issue.getExamples().size(),
							String.format("%s: examples for  issue %d in section %d must be present",
									this.testCaseName, this.issueIndex, this.sectionIndex));
				});
			}
		});

		assertEquals(report.getCoverageStatistics().getTotalLineCount(), report.getCodeCoverage().size(),
				String.format("%s: number of lines in coverage analysis must equal total number of lines",
						this.testCaseName));

		assertFalse(report.getMessages().isEmpty(),
				String.format("%s: some messages must have been recorded", this.testCaseName));
		// With the exception of some expected error messages recorded by the XSLT processor, no
		// errors should have been recorded.
		final List<ILogMessage> errorMessages = report.getMessages().stream()
			.filter(msg -> msg.getLevel().equals(Level.ERROR))
			.filter(msg -> !(msg.getMessage().contains("XTDE0820")))
			.filter(msg -> !(msg.getMessage().contains("XTDE0850")))
			.filter(msg -> !(msg.getMessage().contains("invoked by")))
			.filter(msg -> !(msg.getMessage().contains("template rule with match")))
			.toList();
		if (!errorMessages.isEmpty()) {
			assertEquals(0, errorMessages.size(),
					String.format("%s: no error messages should have been recorded (like \"%s\")",
							this.testCaseName, errorMessages.getFirst().getMessage()));
		}
	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalIssues(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getTotalNumberOfIssues(),
				String.format("%s: total number of issues", this.testCaseName));

	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalDirectives(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getCoverageStatistics().getTotalDirectiveCount(),
				String.format("%s: total number of directives", this.testCaseName));
	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalLines(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getCoverageStatistics().getTotalLineCount(),
				String.format("%s: total number of Lines", this.testCaseName));
	}

	/**
	 * @param report
	 * @param expectedFull
	 * @param expectedPartial
	 */
	protected void assertDirectiveCoverage(IVulnerabilityReport report, double expectedFull,
			double expectedFullAndPartial) {
		final double actualFull = report.getCoverageStatistics().getDirectivePercentageWithFullCoverage();
		final double actualPartial = report.getCoverageStatistics().getDirectivePercentageWithPartialCoverage();
		final double actualFullAndPartial = actualFull + actualPartial;
		logger.info("directive coverage: {} full, {} partial, {} combined",
				actualFull, actualPartial, actualFullAndPartial);
		if (actualFull < expectedFull) {
			fail(String.format("%s: at least %.0f%% of directives must be fully covered (actual coverage: %.0f%%)",
					this.testCaseName, expectedFull, actualFull));
		}
		if (actualFullAndPartial < expectedFullAndPartial) {
			fail(String.format(
					"%s: at least %.0f%% of directives must at least be partially covered (actual coverage: %.0f%%)",
					this.testCaseName, expectedFullAndPartial, actualFullAndPartial));
		}
	}

	/**
	 * @param report
	 * @param expectedFull
	 * @param expectedPartial
	 */
	protected void assertLineCoverage(IVulnerabilityReport report, double expectedFull, double expectedFullAndPartial) {
		final double actualFull = report.getCoverageStatistics().getLinePercentageWithFullCoverage();
		final double actualPartial = report.getCoverageStatistics().getLinePercentageWithPartialCoverage();
		final double actualFullAndPartial = actualFull + actualPartial;
		logger.info("line coverage: {} full, {} partial, {} combined",
				actualFull, actualPartial, actualFullAndPartial);
		if (actualFull < expectedFull) {
			fail(String.format("%s: at least %.0f%% of lines must be fully covered (actual coverage: %.0f%%)",
					this.testCaseName, expectedFull, actualFull));
		}
		if (actualFullAndPartial < expectedFullAndPartial) {
			fail(String.format(
					"%s: at least %.0f%% of lines must at least be partially covered (actual coverage: %.0f%%)",
					this.testCaseName, expectedFull, actualFullAndPartial));
		}
	}

	/**
	 * @param ruleID
	 * @return
	 */
	protected IVulnerabilityReportSection checkAndGetSingleSectionWithIssues(IVulnerabilityReport report,
			String ruleID) {
		final ImmutableList<IVulnerabilityReportSection> sections = report.getSections();
		IVulnerabilityReportSection targetSection = null;
		for (final IVulnerabilityReportSection section : sections) {
			if (section.getRuleID().equals(ruleID)) {
				targetSection = section;
			} else {
				// no other section may contain any issues
				assertEquals(0, section.getIssues().size(),
						String.format("%s: section %s not empty", this.testCaseName, section.getRuleID()));
			}
		}
		assertNotNull(targetSection, String.format("%s: section %s not found", this.testCaseName, ruleID));
		return targetSection;
	}

	/**
	 * @param section
	 * @return
	 */
	protected IVulnerabilityReportIssue checkAndGetSingleIssue(IVulnerabilityReportSection section) {
		final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
		assertEquals(1, issues.size(), String.format("%s: number of issues found", this.testCaseName));
		return issues.get(0);
	}

	/**
	 * @param section
	 * @return
	 */
	protected IVulnerabilityReportIssue checkAndGetSingleIssue(IVulnerabilityReportSection section,
			int expectedNumIssues, int index) {
		final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
		assertEquals(expectedNumIssues, issues.size(), String.format("%s: number of issues found", this.testCaseName));
		return issues.get(index);
	}

}
