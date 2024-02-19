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

	private int sectionIndex;
	private int issueIndex;

	@Override
	public void verify(IVulnerabilityReport report) {
		final ImmutableList<IVulnerabilityReportSection> sections = report.getSections();
		assertEquals(11, sections.size(), "number of sections must be fixed");

		this.sectionIndex = 0;
		sections.forEach(section -> {
			this.sectionIndex++;
			assertFalse(Strings.isNullOrEmpty(section.getRuleID()),
					String.format("rule ID of section %d must be present", this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getHeading()),
					String.format("heading of section %d must be present", this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getShortHeading()),
					String.format("short introduction of section %d must be present", this.sectionIndex));
			assertFalse(Strings.isNullOrEmpty(section.getIntroduction()),
					String.format("introduction of section %d must be present", this.sectionIndex));
			final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
			if (issues.size() != 0) {
				assertFalse(Strings.isNullOrEmpty(section.getDescription()),
						String.format("description of section %d must be present", this.sectionIndex));
				assertFalse(Strings.isNullOrEmpty(section.getCountermeasures()),
						String.format("countermeasures of section %d must be present", this.sectionIndex));

				this.issueIndex = 0;
				issues.forEach(issue -> {
					this.issueIndex++;
					assertNotEquals(0, issue.getAffectingInputElements().size(),
							String.format("affecting input elements of issue %d in section %d must be present",
									this.issueIndex, this.sectionIndex));
					assertFalse(Strings.isNullOrEmpty(issue.getAffectedOutputElement()),
							String.format("affected output element of issue %d in section %d must be present",
									this.issueIndex, this.sectionIndex));
					assertNotEquals(0, issue.getExamples().size(),
							String.format("examples for  issue %d in section %d must be present",
									this.issueIndex, this.sectionIndex));
				});
			}
		});

		assertEquals(report.getCoverageStatistics().getTotalLineCount(), report.getCodeCoverage().size(),
				"number of lines in coverage analysis must equal total number of lines");

		assertFalse(report.getMessages().isEmpty(), "some messages must have been recorded");
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
					String.format("no error messages should have been recorded (like \"%s\")",
							errorMessages.getFirst().getMessage()));
		}
	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalIssues(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getTotalNumberOfIssues(), "total number of issues");

	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalDirectives(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getCoverageStatistics().getTotalDirectiveCount(), "total number of directives");
	}

	/**
	 * @param report
	 * @param expected
	 */
	protected void assertTotalLines(IVulnerabilityReport report, int expected) {
		assertEquals(expected, report.getCoverageStatistics().getTotalLineCount(), "total number of Lines");
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
			fail(String.format("at least %.0f%% of directives must be fully covered (actual coverage: %.0f%%)",
					expectedFull, actualFull));
		}
		if (actualFullAndPartial < expectedFullAndPartial) {
			fail(String.format(
					"at least %.0f%% of directives must at least be partially covered (actual coverage: %.0f%%)",
					expectedFullAndPartial, actualFullAndPartial));
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
			fail(String.format("at least %.0f%% of lines must be fully covered (actual coverage: %.0f%%)",
					expectedFull, actualFull));
		}
		if (actualFullAndPartial < expectedFullAndPartial) {
			fail(String.format(
					"at least %.0f%% of lines must at least be partially covered (actual coverage: %.0f%%)",
					expectedFull, actualFullAndPartial));
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
						String.format("section %s not empty", section.getRuleID()));
			}
		}
		assertNotNull(targetSection, String.format("section %s not found", ruleID));
		return targetSection;
	}

	/**
	 * @param section
	 * @return
	 */
	protected IVulnerabilityReportIssue checkAndGetSingleIssue(IVulnerabilityReportSection section) {
		final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
		assertEquals(1, issues.size(), "number of issues found");
		return issues.get(0);
	}

	/**
	 * @param section
	 * @return
	 */
	protected IVulnerabilityReportIssue checkAndGetSingleIssue(IVulnerabilityReportSection section,
			int expectedNumIssues, int index) {
		final ImmutableList<IVulnerabilityReportIssue> issues = section.getIssues();
		assertEquals(expectedNumIssues, issues.size(), "number of issues found");
		return issues.get(index);
	}

}
