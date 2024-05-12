package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;

/**
 * Verifier for test case Case001_KnownGood.
 */
public class Case001_KnownGood extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case001_KnownGood";

	/**
	 * Default constructor.
	 */
	public Case001_KnownGood() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 0);
		assertTotalDirectives(report, 69);
		assertTotalLines(report, 213);
		assertDirectiveCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes

		// no section may contain any issues
		report.getSections().forEach(section -> {
			assertEquals(0, section.getIssues().size(), String.format("section %s not empty", section.getRuleID()));
		});

	}

}
