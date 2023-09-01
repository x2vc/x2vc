package org.x2vc.analysis.results;

import java.io.File;

/**
 * This component produces a formatted version of an
 * {@link IVulnerabilityReport} and writes it to an output file;
 */
public interface IReportWriter {

	/**
	 * Produces a formatted version of an {@link IVulnerabilityReport} and writes it
	 * to an output file;
	 *
	 * @param report
	 * @param outputFile
	 */
	void write(IVulnerabilityReport report, File outputFile);

}
