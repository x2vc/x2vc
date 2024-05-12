package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case510_S2Attribute.
 */
public class Case510_S2Attribute extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case510_S2Attribute";

	/**
	 * Default constructor.
	 */
	public Case510_S2Attribute() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 1);
		assertTotalDirectives(report, 65);
		assertTotalLines(report, 202);
		assertDirectiveCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 25, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "S.2");
		final IVulnerabilityReportIssue issue = checkAndGetSingleIssue(section);

		assertArrayEquals(new String[] { "/purchaseOrder/@background" },
				issue.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/head/style",
				issue.getAffectedOutputElement(),
				"affected outputElement");
	}

}
