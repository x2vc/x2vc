package org.x2vc.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.analysis.results.IVulnerabilityCandidate;
import org.x2vc.analysis.results.VulnerabilityCandidate;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.DocumentRequest;
import org.x2vc.xml.request.IDocumentRequest;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IDebugObjectWriter}.
 */
@Singleton
public class DebugObjectWriter implements IDebugObjectWriter {

	private static final Logger logger = LogManager.getLogger();
	private boolean requestOutputEnabled;
	private boolean xmlOutputEnabled;
	private boolean htmlOutputEnabled;
	private boolean vulnerabilityCandidateOutputEnabled;
	private File outputPath;

	@Inject
	DebugObjectWriter(@TypesafeConfig("x2vc.xml.request.write_to_file") boolean requestOutputEnabled,
			@TypesafeConfig("x2vc.xml.document.write_to_file") boolean xmlOutputEnabled,
			@TypesafeConfig("x2vc.html.document.write_to_file") boolean htmlOutputEnabled,
			@TypesafeConfig("x2vc.analysis.candidates.write_to_file") boolean vulnerabilityCandidateOutputEnabled) {
		this.requestOutputEnabled = requestOutputEnabled;
		this.xmlOutputEnabled = xmlOutputEnabled;
		this.htmlOutputEnabled = htmlOutputEnabled;
		this.vulnerabilityCandidateOutputEnabled = vulnerabilityCandidateOutputEnabled;
		this.outputPath = new File("debugOutput");
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> requestContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(DocumentRequest.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for document requests", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeRequest(UUID taskID, IDocumentRequest request) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.requestOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, "request", "xml");
				final Marshaller marshaller = this.requestContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(request, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write document request to file", e);
			}
		}
		logger.traceExit();
	}

	@Override
	public void writeXMLDocument(UUID taskID, IXMLDocumentContainer xmlDocument) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.xmlOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, "input", "xml");
				Files.asCharSink(outputFile, Charset.defaultCharset()).write(xmlDocument.getDocument());
			} catch (final IOException e) {
				logger.error("Unable to write XML document to file", e);
			}
		}
		logger.traceExit();
	}

	@Override
	public void writeHTMLDocument(UUID taskID, IHTMLDocumentContainer htmlDocument) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.htmlOutputEnabled && !htmlDocument.isFailed()) {
			final Optional<String> doc = htmlDocument.getDocument();
			if (doc.isPresent()) {
				try {
					final File outputFile = provideOutputFile(taskID, "output", "html");
					Files.asCharSink(outputFile, Charset.defaultCharset()).write(doc.get());
				} catch (final IOException e) {
					logger.error("Unable to write XML document to file", e);
				}
			}
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> vulnerabilityCandidateContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(VulnerabilityCandidate.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for vulnerability candidates", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeVulnerabilityCandidate(UUID taskID, int candidateNumber,
			IVulnerabilityCandidate vulnerabilityCandidate) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.vulnerabilityCandidateOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, String.format("candidate-%06d", candidateNumber),
						"xml");
				final Marshaller marshaller = this.vulnerabilityCandidateContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(vulnerabilityCandidate, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write vulnerability candidate to file", e);
			}
		}
		logger.traceExit();
	}

	/**
	 * @param taskID
	 * @param nameTag
	 * @return
	 */
	private File provideOutputFile(UUID taskID, final String nameTag, final String extension) {
		if (!this.outputPath.exists()) {
			this.outputPath.mkdir();
		}
		return new File(this.outputPath, String.format("%s__%s.%s", taskID, nameTag, extension));
	}

	class DebugOutputError extends RuntimeException {

		private static final long serialVersionUID = -7132737615180636609L;

		/**
		 * @param message
		 * @param cause
		 */
		DebugOutputError(Throwable cause) {
			super(cause);
		}

	}

}
