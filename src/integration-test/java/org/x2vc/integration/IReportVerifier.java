package org.x2vc.integration;

import org.x2vc.report.IVulnerabilityReport;

/**
 * Interface of an object that is able to verify the contents of an {@link IVulnerabilityReport}.
 */
public interface IReportVerifier {

	/**
	 * Verify that the report conforms to the expectations.
	 *
	 * @param report
	 */
	void verify(IVulnerabilityReport report);

}
