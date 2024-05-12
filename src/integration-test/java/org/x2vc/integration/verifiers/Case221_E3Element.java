package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case221_E3Element.
 */
public class Case221_E3Element extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case221_E3Element";

	/**
	 * Default constructor.
	 */
	public Case221_E3Element() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 1);
		assertTotalDirectives(report, 69);
		assertTotalLines(report, 215);
		assertDirectiveCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "E.3");
		final IVulnerabilityReportIssue issue = checkAndGetSingleIssue(section);

		assertArrayEquals(new String[] { "/purchaseOrder/billTo/name", "/purchaseOrder/shipTo/name" },
				issue.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/body/table/tbody/tr/td/div",
				issue.getAffectedOutputElement(),
				"affected outputElement");
	}

}
