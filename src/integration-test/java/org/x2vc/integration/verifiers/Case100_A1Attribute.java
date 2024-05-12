package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case100_A1Attribute.
 */
public class Case100_A1Attribute extends InvariantVerifier {

	private static final String TEST_CASE_NAME = "Case100_A1Attribute";

	/**
	 * Default constructor.
	 */
	public Case100_A1Attribute() {
		super(TEST_CASE_NAME);
	}

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 1);
		assertTotalDirectives(report, 73);
		assertTotalLines(report, 220);
		assertDirectiveCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 30, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "A.1");
		final IVulnerabilityReportIssue issue = checkAndGetSingleIssue(section);

		assertArrayEquals(new String[] { "/purchaseOrder/items/item/product/productImage/@textPlacement" },
				issue.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/body/table/tbody/tr/td/img",
				issue.getAffectedOutputElement(),
				"affected outputElement");
	}

}
