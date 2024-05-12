package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case520_S3Attribute.
 */
public class Case520_S3Attribute extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case520_S3Attribute";

	/**
	 * Default constructor.
	 */
	public Case520_S3Attribute() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 1);
		assertTotalDirectives(report, 69);
		assertTotalLines(report, 212);
		assertDirectiveCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "S.3");
		final IVulnerabilityReportIssue issue = checkAndGetSingleIssue(section);

		assertArrayEquals(new String[] { "/purchaseOrder/@system" },
				issue.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/head/link",
				issue.getAffectedOutputElement(),
				"affected outputElement");
	}

}
