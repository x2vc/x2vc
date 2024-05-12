package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case200_E1Attribute.
 */
public class Case200_E1Attribute extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case200_E1Attribute";

	/**
	 * Default constructor.
	 */
	public Case200_E1Attribute() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 1);
		assertTotalDirectives(report, 81);
		assertTotalLines(report, 235);
		assertDirectiveCoverage(report, 25, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 25, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "E.1");
		final IVulnerabilityReportIssue issue = checkAndGetSingleIssue(section);

		assertArrayEquals(new String[] { "/purchaseOrder/items/@listType" },
				issue.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/body/xss-e1-element",
				issue.getAffectedOutputElement(),
				"affected outputElement");
	}

}
