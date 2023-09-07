package org.x2vc.report;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import net.sf.saxon.s9api.*;

/**
 * Standard implementation of {@link IReportWriter}.
 */
@Singleton
public class ReportWriter implements IReportWriter {

	private static final Logger logger = LogManager.getLogger();

	private Processor processor;

	private boolean xmlOutputEnabled;

	@Inject
	ReportWriter(Processor processor,
			@TypesafeConfig("x2vc.report.source.write_to_file") boolean xmlOutputEnabled) {
		this.processor = processor;
		this.xmlOutputEnabled = xmlOutputEnabled;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<XsltExecutable> stylesheetSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final XsltCompiler compiler = this.processor.newXsltCompiler();
		final InputStream stylesheetStream = getClass().getClassLoader().getResourceAsStream("report/report.xslt");
		XsltExecutable result;
		try {
			result = compiler.compile(new StreamSource(stylesheetStream));
		} catch (final SaxonApiException e) {
			throw logger.throwing(new RuntimeException("Unable to compile report stylesheet", e));
		}
		return logger.traceExit(result);
	});

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> contextSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		try {
			return logger.traceExit(JAXBContext.newInstance(VulnerabilityReport.class));
		} catch (final JAXBException e) {
			throw logger.throwing(new RuntimeException("Unable to initialize report serializer context", e));
		}
	});

	/**
	 * @return
	 */
	private Marshaller createMarshaller() {
		logger.traceEntry();
		try {
			final Marshaller marshaller = this.contextSupplier.get().createMarshaller();
			marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
				@Override
				public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
					out.write(ch, start, length);
				}
			});
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			return logger.traceExit(marshaller);
		} catch (final JAXBException e) {
			throw logger.throwing(new RuntimeException("Unable to initialize report serializer", e));
		}

	}

	@Override
	public void write(IVulnerabilityReport report, File outputFile) {
		logger.traceEntry();
		try {
			final Marshaller marshaller = createMarshaller();
			final StringWriter xmlWriter = new StringWriter();
			marshaller.marshal(report, xmlWriter);
			final String xmlData = xmlWriter.toString();

			if (this.xmlOutputEnabled) {
				writeRawReportToFile(xmlData, outputFile);
			}

			final Serializer serializer = this.processor.newSerializer(outputFile);
			final Xslt30Transformer transformer = this.stylesheetSupplier.get().load30();
			transformer.transform(new StreamSource(new StringReader(xmlData)), serializer);

		} catch (final JAXBException | SaxonApiException e) {
			logger.error("Unable to create report", e);
		}
		logger.traceExit();
	}

	/**
	 * @param xmlData
	 * @param outputFile
	 */
	private void writeRawReportToFile(String xmlData, File outputFile) {
		logger.traceEntry();
		final String xmlFilename = outputFile.getName().replace(".html", ".xml");
		final File xmlOutputFile = new File(outputFile.getParentFile(), xmlFilename);
		logger.debug("Writing raw report data to {}", xmlOutputFile);
		try {
			Files.writeString(xmlOutputFile.toPath(), xmlData,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} catch (final IOException e) {
			logger.error("Unable to write raw report data", e);
		}
		logger.traceExit();
	}

}
