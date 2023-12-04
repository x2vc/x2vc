package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;

/**
 * Verifier for test case Case001_KnownGood.
 */
public class Case001_KnownGood extends InvariantVerifier {

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 0);
		assertTotalDirectives(report, 69);
		assertTotalLines(report, 213);
		assertDirectiveCoverage(report, 60, 75); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 55, 80); // TODO #80 double-check coverage after fixes

		// no section may contain any issues
		report.getSections().forEach(section -> {
			assertEquals(0, section.getIssues().size(), String.format("section %s not empty", section.getRuleID()));
		});

	}

}
