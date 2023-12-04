package org.x2vc.integration.verifiers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.x2vc.report.IVulnerabilityReport;
import org.x2vc.report.IVulnerabilityReportIssue;
import org.x2vc.report.IVulnerabilityReportSection;

/**
 * Verifier for test case Case210_E2CopyOf.
 */
public class Case210_E2CopyOf extends InvariantVerifier {

	@Override
	public void verify(IVulnerabilityReport report) {
		super.verify(report);
		assertTotalIssues(report, 2);
		assertTotalDirectives(report, 66);
		assertTotalLines(report, 210);
		assertDirectiveCoverage(report, 40, 50); // TODO #80 double-check coverage after fixes
		assertLineCoverage(report, 40, 50); // TODO #80 double-check coverage after fixes

		final IVulnerabilityReportSection section = checkAndGetSingleSectionWithIssues(report, "E.2");

		final IVulnerabilityReportIssue issue0 = checkAndGetSingleIssue(section, 2, 0);
		final IVulnerabilityReportIssue issue1 = checkAndGetSingleIssue(section, 2, 1);

		assertArrayEquals(new String[] { "/purchaseOrder/items/item/comment" },
				issue0.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/body/table/tbody/tr/td/comment",
				issue0.getAffectedOutputElement(),
				"affected outputElement");

		assertArrayEquals(new String[] { "/purchaseOrder/comment" },
				issue1.getAffectingInputElements().toArray(new String[0]),
				"affecting input elements");

		assertEquals("/html/body/table/tbody/tr/td/table/tbody/tr/td/div/comment",
				issue1.getAffectedOutputElement(),
				"affected outputElement");

	}

}
