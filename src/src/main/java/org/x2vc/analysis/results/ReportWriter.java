package org.x2vc.analysis.results;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard implementation of {@link IReportWriter}.
 */
public class ReportWriter implements IReportWriter {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public void write(IVulnerabilityReport report, File outputFile) {
		logger.traceEntry();
		// TODO Report Output: replace XML output with proper HTML output
		try {
			final JAXBContext context = JAXBContext.newInstance(VulnerabilityReport.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(report, outputFile);
		} catch (final JAXBException e) {
			logger.error(e);
		}
		logger.traceExit();
	}

}
